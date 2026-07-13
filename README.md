# weather-tools

A small set of standalone Kotlin/Maven tools that harvest and monitor weather station data,
feeding a MySQL-backed weather website.

## What's here

Package root `org.larsi`:

- **`geonames/`** — polls the [GeoNames](https://www.geonames.org/) weather API for METAR-derived
  observations (temperature, dew point, humidity, clouds, pressure, wind) per weather station, and
  stores them in MySQL. This is the live, currently-deployed data source.
- **`ndfd/`** — supplements measured observations with [NOAA NDFD](https://graphical.weather.gov/)
  forecast data via SOAP. Written but not currently deployed.
- **`ish/`** — a manual backfill tool that downloads NOAA's historical "ISH" (Integrated Surface
  Hourly) archive and re-imports it, to patch gaps left by harvester downtime.
- **`zeus/`** — a monitoring job that tracks per-sensor data freshness and flags stations that have
  stopped reporting.
- **`dev/`** — assorted one-off developer utilities (station-list generation, data export/import,
  etc.).
- **`util/`** — shared helpers: config loading, a JDBC connection/query helper, and psychrometric
  calculations.

Each of `geonames`/`ndfd`/`zeus` also has a "CC" counterpart (`GeoNamesCC`, `NDFDCC`, `IshCC`)
that targets an alternate multi-tenant database schema. These are not currently used by the live
site.

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
| `target/geonames.jar` | `org.larsi.geonames.GeoNames` |
| `target/ndfd.jar` | `org.larsi.ndfd.NDFD` |
| `target/zeus.jar` | `org.larsi.zeus.Zeus` |

`ish/`'s two entry points (`org.larsi.ish.IshHarvester`, `org.larsi.ish.IshDatabase`) aren't
packaged into a standalone jar; run them via the classpath instead:

```sh
java -cp target/weather-tools-1.0-SNAPSHOT.jar:$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout) org.larsi.ish.IshHarvester
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
