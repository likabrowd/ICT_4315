#!/bin/bash
# ============================================================
# test.sh  —  Run all JUnit 5 tests
# ============================================================
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "=== DU Parking System — Test Suite ==="
echo ""

java -cp "out:lib/junit5.jar" \
     org.junit.platform.console.ConsoleLauncher \
     execute \
     --select-package=parking \
     --details=verbose

echo ""
echo "=== Tests complete ==="
