#!/bin/bash
# test.sh  —  Run all JUnit 5 tests (including DI tests)

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "DU Parking System — Test Suite (Assignment 10 + Guice DI)"
echo ""

CLASSPATH="out:lib/junit5.jar:lib/guice-7.0.0.jar:lib/javax.inject-1.jar:lib/aopalliance-1.0.jar:lib/guava-32.1.3-jre.jar:lib/jakarta.inject-api-2.0.1.jar:lib/failureaccess-1.0.1.jar"

java -cp "$CLASSPATH" \
     org.junit.platform.console.ConsoleLauncher \
     execute \
     --select-package=parking \
     --details=verbose

echo ""
echo "Tests complete"
