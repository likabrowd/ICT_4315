#!/bin/bash
# run_client.sh  —  Send a JSON command to the parking server
#
# Usage:
#   ./run_client.sh CUSTOMER firstname=Kalika phone=303-123-4567
#   ./run_client.sh CAR license=KAL4CO customerid=<uuid> cartype=COMPACT

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

if [ $# -eq 0 ]; then
    echo "Usage: ./run_client.sh <COMMAND> [key=value ...]"
    echo ""
    echo "Examples:"
    echo "  ./run_client.sh CUSTOMER firstname=Kalika phone=303-123-4567"
    echo "  ./run_client.sh CAR license=KAL4CO customerid=<uuid> cartype=COMPACT"
    exit 1
fi

java -cp out parking.ServerClient "$@"
