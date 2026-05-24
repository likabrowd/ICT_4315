#!/bin/bash
# ============================================================
# compile.sh  —  Compile all parking system source files
# ============================================================
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== DU Parking System — Compile ==="
echo "Working directory: $SCRIPT_DIR"

# Create output directory
mkdir -p out

# Compile everything
javac -cp lib/junit5.jar \
      -d out \
      $(find src -name "*.java")

echo ""
echo "✓ Compilation successful. Classes written to: out/"
echo ""
echo "Next steps:"
echo "  Run tests:  ./test.sh"
echo "  Run server: ./run_server.sh"
echo "  Run client: ./run_client.sh CUSTOMER firstname=YourName phone=303-000-0000"
