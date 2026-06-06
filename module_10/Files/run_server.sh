#!/bin/bash
# run_server.sh  —  Start the multithreaded JSON parking server

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== DU Parking Multithreaded JSON Server ==="
echo "Port: 5001  |  Thread pool: 10 workers"
echo "Press Ctrl+C to stop."
echo ""

java -cp out parking.Server
