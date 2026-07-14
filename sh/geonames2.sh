#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
java -cp target/geonames.jar org.larsi.GeoNames2
