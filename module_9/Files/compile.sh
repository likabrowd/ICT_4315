#!/bin/bash
# ============================================================
# compile.sh  —  Compile all parking system source files
# ============================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== DU Parking System — Compile ==="
echo "Working directory: $SCRIPT_DIR"

mkdir -p out

javac -cp lib/junit5.jar \
      -d out \
      $(find parking -name "*.java")

echo ""
echo "✓ Compilation successful. Classes written to: out/"
echo ""
echo "Next steps:"
echo "  Run tests:         ./test.sh"
echo "  Run server:        ./run_server.sh"
echo "  Single client:     ./run_client.sh CUSTOMER firstname=Kalika phone=303-123-4567"
echo "  Multi-client test: ./run_multi_client.sh"
