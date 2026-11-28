# weather-tools

A small set of standalone Kotlin/Maven tools that harvest and monitor weather station data,
feeding a MySQL-backed weather website.

## What's here

Package root `org.larsi`. Most tools live directly in that package (one or two files each, not
worth their own subdirectory); `dev/` and `util/` are the exceptions since they hold several files
each:

- **`GeoNames2.kt`** — polls the [GeoNames](https://www.geonames.org/) weather API for
  METAR-derived observations (temperature, dew point, humidity, clouds, pressure, wind) per weather
  station, and stores them in MySQL (`larsi-weather2`). This is the live, currently-deployed data
  source. If it can't reach the database at all, it pushes its own alert (see `Zeus.kt` below)
  rather than failing silently.
- **`AviationWeather.kt`** — the same METAR-derived observations from NOAA's
  [Aviation Weather Center](https://aviationweather.gov/data/api/) data API, written to replace
  GeoNames2: a few requests per run for all stations, only the routine hourly METARs, and a
  catch-up that fills everything after each station's last reading, up to 30 days back, after a
  missed run (`java -jar aviationweather.jar [--dry-run]`; its header comment is the reference).
  `fill --days 30` finds and fills holes before the last reading too (meant as a daily job).
- **`NDFD2.kt`** — supplements measured observations with
  [NOAA NDFD](https://graphical.weather.gov/) forecast data via SOAP. Written but currently
  unsupported: the site's display layer (`larsi-org/html`'s `api/weather/sensors.php`,
  `api/weather/graphs.php`, `weather/report.php`) dropped its predicted-sensor-ID entries as of
  2026-08-30, since nothing had ever logged to them. Running NDFD2 today would insert predicted
  rows with nowhere to display them; re-enabling it needs those channel entries added back to
  the display layer first (see this repo's `CLAUDE.md` for the exact list).
- **`Ghcnh.kt`** — the manual backfill tool: downloads NOAA's GHCNh (Global Historical
  Climatology Network hourly) station-year files and replaces each station's rows with the
  archived METARs wherever the file has them, so a live reading stays after the archive's last
  one and in the archive's own gaps (`java -jar ghcnh.jar download <year | from-to> [prefix ...]`, then
  `java -jar ghcnh.jar load [--dry-run] [<year | from-to>] [prefix ...]`, 2025 to now by default;
  its header comment is the reference).
- **`Zeus.kt`** — a monitoring job that tracks data freshness for both `larsi-sensors` (per-device)
  and `larsi-weather2` (per-station), flagging anything that's stopped reporting, and separately
  alerting if it can't connect to either database at all.
- **`dev/`** — assorted one-off developer utilities (station-list generation, data export/import,
  etc.).
- **`util/`** — shared helpers: config loading, a JDBC connection/query helper, and
  push-notification alerting.

## Sensor IDs

Each stored quantity is identified by a numeric sensor ID:

| Quantity | Measured (GeoNames2/Ghcnh) |
|---|---|
| Temperature | 0 |
| Dew Point | 1 |
| Humidity | 2 |
| Sea Level Pressure | 3 |
| Wind Direction | 4 |
| Wind Speed | 5 |
| Clouds | 6 |

NDFD2 would write predicted IDs at `measured + 16` (no pressure counterpart, since NDFD doesn't
forecast it) -- see the NDFD2 note above; currently unsupported, so these IDs aren't in live use.

## Requirements

- JDK 11+
- Maven
- A MySQL server (schema not included in this repo)

## Building

```sh
mvn clean package
```

This produces five runnable jars in `target/`, each bundling the full project plus dependencies:

| Jar | Runs |
|---|---|
| `target/geonames.jar` | `org.larsi.GeoNames2` |
| `target/ndfd.jar` | `org.larsi.NDFD2` |
| `target/zeus.jar` | `org.larsi.Zeus` |
| `target/ghcnh.jar` | `org.larsi.Ghcnh` |
| `target/aviationweather.jar` | `org.larsi.AviationWeather` |

Run any of them with `java -jar target/<jar>` from a directory containing `credentials.json`
(`ghcnh.jar download` doesn't need one, since it only downloads files — no DB access; run it
before `ghcnh.jar load`, which imports the files on disk).

## Configuration

Every tool reads a `credentials.json` file from its working directory at startup. This file is
**not** included in the repo (it's gitignored) — create your own alongside whichever jar you run:

```json
{
	"database": {
		"host": "your-db-host",
		"username": "your-db-user",
		"password": "your-db-password"
	},
	"googleMaps": {
		"apiKey": "your-google-maps-static-api-key"
	}
}
```

`googleMaps` is only needed by `dev/GetIcons.kt`; every other tool only needs `database`.

## Testing

```sh
mvn test
```

Unit tests cover the pure-logic pieces of this codebase (METAR parsing, GeoNames2's and AWC's
responses, AWC's paging, GHCNh row parsing and runs, and database URL building). Code that talks to a real database, network,
or file system isn't covered by automated tests.

## License

MIT - see [LICENSE](LICENSE). It covers the original code in this repository. These
files are not mine to license and stay under their own terms:

- `doc/stations.txt` - station list compiled by Greg Thompson, NCAR/RAP
- `doc/isd-history.txt` - NOAA Integrated Surface Database station history (US government data)
