#!/bin/bash

# StockSmart App - Log Viewer
# Usage: ./view-logs.sh

ADB="$HOME/Library/Android/sdk/platform-tools/adb"

echo "📱 StockSmart Logs"
echo "=================="
echo "Press Ctrl+C to stop"
echo ""

$ADB logcat | grep -E "inv_5|AndroidRuntime|System.err"
