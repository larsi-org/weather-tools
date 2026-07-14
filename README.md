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

## Sensor IDs

Each stored quantity is identified by a numeric sensor ID, which differs between the live schema
and the CC schema, and between measured and predicted values:

| Quantity | Measured (GeoNamesCC/IshCC) | Predicted (NDFDCC) | Measured (GeoNames/Ish) | Predicted (NDFD) |
|---|---|---|---|---|
| Temperature | 0 | 16 | 1 | 2 |
| Dew Point | 1 | 17 | 4 | 5 |
| Humidity | 2 | 18 | 7 | 8 |
| Sea Level Pressure | 3 | — | 16 | — |
| Wind Direction | 4 | 20 | 19 | 20 |
| Wind Speed | 5 | 21 | 22 | 23 |
| Clouds | 6 | 22 | 10 | 11 |

Pressure has no predicted counterpart in either schema (NDFD doesn't forecast it). The live schema
derives predicted IDs as `measured + 1`; the CC schema uses `measured + 16`.

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
