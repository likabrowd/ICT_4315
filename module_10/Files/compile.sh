#!/bin/bash
# compile.sh  — Compile all source files

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "DU Parking System (Assignment 10 — Guice DI) — Compile"
echo "Working directory: $SCRIPT_DIR"

#Classpath: JUnit 5 + Guice + Guice dependencies
CLASSPATH="lib/junit5.jar:lib/guice-7.0.0.jar:lib/javax.inject-1.jar:lib/aopalliance-1.0.jar:lib/guava-32.1.3-jre.jar:lib/jakarta.inject-api-2.0.1.jar:lib/failureaccess-1.0.1.jar"

mkdir -p out

javac -cp "$CLASSPATH" \
      -d out \
      $(find parking -name "*.java")

echo ""
echo "✓ Compilation successful. Classes written to: out/"
echo ""
echo "Next steps:"
echo "  Run tests:          ./test.sh"
echo "  Run DI server:      ./run_server_guice.sh"
echo "  Run demo:           ./run_demo.sh"
echo "  Single client:      ./run_client.sh CUSTOMER firstname=Kalika"
echo "  Multi-client test:  ./run_multi_client.sh"
