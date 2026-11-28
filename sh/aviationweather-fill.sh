#!/bin/bash
cd "$(dirname "$0")/.." || exit 1
java -jar target/aviationweather.jar fill --days 30
