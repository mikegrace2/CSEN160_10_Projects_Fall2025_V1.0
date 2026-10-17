#!/bin/bash

# Create bin directory
mkdir -p bin

# Compile all Java files
echo "Compiling..."
javac -d bin *.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Running game..."
    echo ""
    cd bin
    java com.mygame.cubecollector.Game
else
    echo "Compilation failed!"
    exit 1
fi




