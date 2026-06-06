#!/bin/bash

# run_multi_client.sh  —  Demonstrate concurrent client handling

# Fires 5 CUSTOMER registrations simultaneously, then waits for all of them to complete. Demonstrates that the thread-pool
# server handles concurrent connections correctly.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== Multi-Client Concurrency Test ==="
echo "Launching 5 simultaneous CUSTOMER registrations..."
echo ""

NAMES=("Joan" "Rick" "Madeline" "Dave" "Cindy")
PIDS=()

for NAME in "${NAMES[@]}"; do
    java -cp out parking.ServerClient CUSTOMER firstname="$NAME" phone="303-000-000${RANDOM}" &
    PIDS+=($!)
done

# Wait for all background jobs to finish!
for PID in "${PIDS[@]}"; do
    wait "$PID"
done

echo ""
echo "=== All 5 concurrent clients finished ==="
