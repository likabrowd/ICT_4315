#!/bin/bash
# ============================================================
# run_server.sh  —  Start the JSON parking server on port 5001
# ============================================================
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== DU Parking JSON Server ==="
echo "Port: 5001"
echo "Protocol: single-line JSON request / response"
echo "Press Ctrl+C to stop."
echo ""

java -cp out parking.Server
