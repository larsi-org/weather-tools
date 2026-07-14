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
  source.
- **`NDFD2.kt`** — supplements measured observations with
  [NOAA NDFD](https://graphical.weather.gov/) forecast data via SOAP. Written but not currently
  deployed.
- **`Ish2.kt`** (plus `IshHarvester.kt`) — a manual backfill tool that downloads NOAA's
  historical "ISH" (Integrated Surface Hourly) archive and re-imports it, to patch gaps left by
  harvester downtime.
- **`Zeus.kt`** — a monitoring job that tracks per-sensor data freshness and flags stations that
  have stopped reporting.
- **`dev/`** — assorted one-off developer utilities (station-list generation, data export/import,
  etc.).
- **`util/`** — shared helpers: config loading, a JDBC connection/query helper, and psychrometric
  calculations.

The `2` suffix on `GeoNames2`/`NDFD2`/`Ish2` is a naming artifact from when `larsi-weather2` was a
prospective successor schema developed alongside the original, now-retired `GeoNames`/`NDFD`/`Ish`
tools (which targeted the old per-station-table `larsi-weather` schema). That migration is
complete — `larsi-weather2` is now the live schema and the old tools/schema have been removed.

## Sensor IDs

Each stored quantity is identified by a numeric sensor ID:

| Quantity | Measured (GeoNames2/Ish2) | Predicted (NDFD2) |
|---|---|---|
| Temperature | 0 | 16 |
| Dew Point | 1 | 17 |
| Humidity | 2 | 18 |
| Sea Level Pressure | 3 | — |
| Wind Direction | 4 | 20 |
| Wind Speed | 5 | 21 |
| Clouds | 6 | 22 |

Pressure has no predicted counterpart (NDFD doesn't forecast it). Predicted IDs are `measured + 16`.

## Requirements

- JDK 11+
- Maven
- A MySQL server (schema not included in this repo)

## Building

```sh
mvn clean package
```

This produces three runnable jars in `target/`, each bundling the full project plus dependencies:

| Jar | Runs |
|---|---|
| `target/geonames.jar` | `org.larsi.GeoNames2` |
| `target/ndfd.jar` | `org.larsi.NDFD2` |
| `target/zeus.jar` | `org.larsi.Zeus` |

`ish/`'s two entry points (`org.larsi.IshHarvester`, `org.larsi.Ish2`) aren't
packaged into a standalone jar; run them via the classpath instead:

```sh
java -cp target/weather-tools-1.0-SNAPSHOT.jar:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout) org.larsi.IshHarvester
```

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

Unit tests cover the pure-logic pieces of this codebase (psychrometric calculations, database
URL building, and NOAA fixed-width record parsing). Code that talks to a real database, network,
or file system isn't covered by automated tests.
