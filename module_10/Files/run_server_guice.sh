#!/bin/bash
# run_server_guice.sh  —  Start the Guice-injected parking server


SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== DU Parking Server — Guice Dependency Injection Edition ==="
echo "Port: 5001  |  Thread pool: 10 workers  |  DI: Google Guice 7.0.0"
echo "Press Ctrl+C to stop."
echo ""

CLASSPATH="out:lib/guice-7.0.0.jar:lib/javax.inject-1.jar:lib/aopalliance-1.0.jar:lib/guava-32.1.3-jre.jar:lib/jakarta.inject-api-2.0.1.jar:lib/failureaccess-1.0.1.jar"

java -cp "$CLASSPATH" parking.inject.ParkingSystemApp
