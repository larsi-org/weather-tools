#!/usr/bin/env bash
# reseed-local-db.sh - copy the larsi.org app databases from prod into the local MariaDB.
#
#   sh/reseed-local-db.sh                    all six databases
#   sh/reseed-local-db.sh larsi-sensors      just the ones named
#
# Every table is copied whole (schema and data), except the time-series log tables - every
# `*_log` table of larsi-sensors and the `log` table of larsi-weather2, millions of rows each -
# which get their schema plus the last DAYS days of each series (per channel; per prefix and
# channel for weather), counted back from that series' own newest reading, so a station that
# stopped logging years ago still has its final month. The default 31 days gives every report a
# full month (lines, daily bands, zoom, the hourly/daily toggle) at about 2.6M rows (2026-11);
# DAYS=0 keeps only the newest row of each series (about 2,200 rows, seconds to load).
# A local table that no longer exists on prod is dropped.
#
# Credentials: the larsi-admin role from the site's lib/db.php (the same users exist on prod and
# locally; locally larsi-admin has ALL on each of the six schemas, so no sudo is needed). The
# password goes through MYSQL_PWD, never the command line. Prod is read only: dumps run with
# --skip-lock-tables so the live writers (sensors/log, the harvester) are never blocked, and the
# log-table queries are capped with max_statement_time.
#
# Environment: DAYS (default 31), DB_PHP (default ~/Public/html/lib/db.php), PROD_HOST (default
# larsi.org).
set -euo pipefail

export DB_PHP=${DB_PHP:-$HOME/Public/html/lib/db.php}
PROD_HOST=${PROD_HOST:-larsi.org}
DAYS=${DAYS:-31}
DBS=(larsi-sensors larsi-weather2 larsi-epw larsi-drinks larsi-wine bbq-larsi)
[ $# -gt 0 ] && DBS=("$@")

read -r DB_USER MYSQL_PWD < <(php -r 'require getenv("DB_PHP");
  [$u, $p] = $db_credentials[DB_ROLE_ADMIN]; echo "$u $p\n";')
export MYSQL_PWD

prod()      { mariadb      --ssl=0 -h "$PROD_HOST" -u "$DB_USER" "$@"; }
prod_dump() { mariadb-dump --ssl=0 -h "$PROD_HOST" -u "$DB_USER" --skip-lock-tables "$@"; }
lcl()       { mariadb               -h localhost    -u "$DB_USER" "$@"; }

# the log tables of a database, and the columns that make one series in them
log_tables() {
  case "$1" in
    larsi-sensors)  prod -N -e "SHOW TABLES LIKE '%\\_log'" "$1" ;;
    larsi-weather2) echo log ;;
  esac
}
series_cols() { [ "$1" = larsi-weather2 ] && echo "prefix, channel" || echo "channel"; }

for db in "${DBS[@]}"; do
  echo "== $db"
  mapfile -t prod_tables  < <(prod -N -e "SHOW TABLES" "$db")
  mapfile -t local_tables < <(lcl  -N -e "SHOW TABLES" "$db")
  mapfile -t logs         < <(log_tables "$db")

  # drop local tables that prod no longer has
  for t in "${local_tables[@]}"; do
    if ! printf '%s\n' "${prod_tables[@]}" | grep -qxF "$t"; then
      echo "   dropping local-only table $t"
      lcl -e "DROP TABLE \`$t\`" "$db"
    fi
  done

  # everything but the log tables, whole (the dump carries DROP TABLE IF EXISTS + CREATE)
  ignore=()
  for t in "${logs[@]}"; do ignore+=("--ignore-table=$db.$t"); done
  prod_dump "${ignore[@]}" "$db" | lcl "$db"

  # the log tables: schema, then the last DAYS days of each series, in batches of 5000 rows
  cols=$(series_cols "$db")
  on=$(echo "$cols" | sed -E 's/([a-z]+)/l.\1 = m.\1/g; s/, / AND /g')
  for t in "${logs[@]}"; do
    prod_dump --no-data "$db" "$t" | lcl "$db"
    lcl -e "ALTER TABLE \`$t\` DISABLE KEYS" "$db"
    prod --quick -N -B "$db" -e "SET STATEMENT max_statement_time=600 FOR
      SELECT l.* FROM \`$t\` l
      JOIN (SELECT $cols, MAX(epoch) AS epoch FROM \`$t\` GROUP BY $cols) m
        ON $on AND l.epoch >= m.epoch - $DAYS * 86400" |
    awk -v t="$t" -F'\t' '
      { row = "("; for (i = 1; i <= NF; i++) row = row (i > 1 ? "," : "") "\x27" $i "\x27";
        printf "%s%s", (n % 5000 ? ",\n" : "INSERT INTO `" t "` VALUES\n"), row ")"
        if (++n % 5000 == 0) print ";" }
      END { if (n % 5000) print ";" }' |
    lcl "$db"
    lcl -e "ALTER TABLE \`$t\` ENABLE KEYS" "$db"
    echo "   $t: $(lcl -N -e "SELECT COUNT(*) FROM \`$t\`" "$db") rows (last $DAYS days of each series)"
  done

  # check: every whole-copied table has prod's row count
  for t in "${prod_tables[@]}"; do
    printf '%s\n' "${logs[@]}" | grep -qxF "$t" && continue
    p=$(prod -N -e "SELECT COUNT(*) FROM \`$t\`" "$db")
    l=$(lcl  -N -e "SELECT COUNT(*) FROM \`$t\`" "$db")
    [ "$p" = "$l" ] || echo "   MISMATCH $t: prod $p, local $l"
  done
  echo "   ${#prod_tables[@]} tables, row counts checked"
done
