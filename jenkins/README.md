# Jenkins JDK auto-restart

Keeps the `geonames` (and `zeus`) Jenkins jobs working across in-place OpenJDK upgrades.

## The problem

Unattended upgrades replace the OpenJDK packages in place (around 06:36 most days). Jenkins keeps
running on the now-deleted old JVM, and its `jspawnhelper` no longer matches, so every job that
runs a shell step fails with:

```
Failed to exec spawn helper: ... exit code: 1
  - Spawn helper ran into JDK version mismatch
```

until Jenkins is restarted. The tell is a `(deleted)` on `readlink /proc/<jenkins-pid>/exe`.

## The fix

`jenkins-jdk-restart` runs after every `dpkg` invocation (via the apt hook) and restarts Jenkins
only when the JDK it is running on was replaced on disk after Jenkins started. It compares the
**ctime** of the running JVM's `lib/jspawnhelper` against Jenkins's start time. ctime is used, not
mtime: dpkg copies the `.deb`'s build mtime onto the unpacked file, which can predate Jenkins's
start and hide a real upgrade (this bug let the 2026-11-20 upgrade through, failing every run until
2026-11-22). ctime is set when the file is renamed into place and cannot be backdated.

Idempotent: once Jenkins restarts, later runs see the file as older than the new process and do
nothing. `--check` is a dry run that prints what it would do.

## Install (run as root on the Jenkins host)

```sh
install -m 755 jenkins-jdk-restart /usr/local/sbin/jenkins-jdk-restart
install -m 644 99-jenkins-jdk-restart /etc/apt/apt.conf.d/99-jenkins-jdk-restart
/usr/local/sbin/jenkins-jdk-restart --check   # expect: "ok: ... unchanged since it started"
```

These are the authoritative copies; the installed files are not otherwise version-controlled, so
edit here and reinstall.

## Diagnose

```sh
sudo /usr/local/sbin/jenkins-jdk-restart --check   # "would restart" = Jenkins is on a stale JDK
journalctl -t jenkins-jdk-restart                  # restarts it has logged
readlink /proc/$(systemctl show -p MainPID --value jenkins)/exe   # "(deleted)" = stale
```

If the spawn-helper error ever returns, confirm the helper still tests `%Z` (ctime), not `%Y`.
