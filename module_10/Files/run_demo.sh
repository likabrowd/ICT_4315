#!/bin/bash

# run_demo.sh  —  Full demo: start server + run clients

# Opens the Guice DI server in the background, fires several client requests, then stops the server.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

CLASSPATH="out:lib/guice-7.0.0.jar:lib/javax.inject-1.jar:lib/aopalliance-1.0.jar:lib/guava-32.1.3-jre.jar:lib/jakarta.inject-api-2.0.1.jar:lib/failureaccess-1.0.1.jar"
CLIENT_CP="out:lib/guice-7.0.0.jar:lib/javax.inject-1.jar:lib/aopalliance-1.0.jar:lib/guava-32.1.3-jre.jar:lib/jakarta.inject-api-2.0.1.jar:lib/failureaccess-1.0.1.jar"

echo "============================================================"
echo " DU Parking System — Assignment 10 Demo"
echo " Dependency Injection with Google Guice"
echo "============================================================"
echo ""

# Start server in background
echo "[Demo] Starting Guice DI server on port 5001..."
java -cp "$CLASSPATH" parking.inject.ParkingSystemApp &
SERVER_PID=$!
echo "[Demo] Server PID: $SERVER_PID"

# Give the server a bit to start
sleep 2

echo ""
echo "[Demo] Registering customer Kalika..."
CUST_OUT=$(java -cp "$CLIENT_CP" parking.ServerClient CUSTOMER firstname=Kalika phone=303-123-4567)
echo "$CUST_OUT"

# Extract customer ID from output
CUST_ID=$(echo "$CUST_OUT" | grep "Message:" | sed 's/.*Customer registered: //' | sed 's/ |.*//')
echo ""
echo "[Demo] Customer ID: $CUST_ID"

echo ""
echo "[Demo] Registering compact car KAL4CO for Kalika..."
java -cp "$CLIENT_CP" parking.ServerClient CAR license=KAL4CO customerid="$CUST_ID" cartype=COMPACT

echo ""
echo "[Demo] Registering customer Bob..."
java -cp "$CLIENT_CP" parking.ServerClient CUSTOMER firstname=Bob phone=720-555-0001

echo ""
echo "[Demo] Sending concurrent requests..."
for NAME in Alice Charlie Dana; do
    java -cp "$CLIENT_CP" parking.ServerClient CUSTOMER firstname="$NAME" &
done
wait

echo ""
echo "============================================================"
echo " Demo complete! Stopping server..."
echo "============================================================"
kill $SERVER_PID 2>/dev/null
wait $SERVER_PID 2>/dev/null
echo "[Demo] Server stopped."
