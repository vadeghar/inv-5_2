#!/bin/bash

# StockSmart App - Clean Install Script
# This uninstalls the old app before installing new one

echo "🧹 StockSmart Clean Installation"
echo "================================="
echo ""

ADB="$HOME/Library/Android/sdk/platform-tools/adb"

# Check if ADB exists
if [ ! -f "$ADB" ]; then
    echo "❌ Error: ADB not found at $ADB"
    exit 1
fi

# Check if device is connected
DEVICE=$($ADB devices | grep -w "device" | wc -l)
if [ "$DEVICE" -eq 0 ]; then
    echo "❌ Error: No device/emulator connected."
    echo "   Please start an emulator or connect a device."
    exit 1
fi

echo "✅ Device/Emulator connected"
echo ""

PACKAGE_NAME="com.example.inv_5"
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

# Uninstall old version
echo "🗑️  Uninstalling old version..."
$ADB uninstall $PACKAGE_NAME 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✅ Old version uninstalled"
else
    echo "ℹ️  No previous version found (or already uninstalled)"
fi

echo ""

# Check if APK exists
if [ ! -f "$APK_PATH" ]; then
    echo "❌ Error: APK not found. Building..."
    ./gradlew assembleDebug
    
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
fi

# Install new version
echo "📦 Installing StockSmart..."
$ADB install "$APK_PATH"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Installation successful!"
    echo ""
    echo "🚀 Launching app..."
    
    # Clear app data and launch
    $ADB shell am force-stop $PACKAGE_NAME
    sleep 1
    $ADB shell am start -n $PACKAGE_NAME/.SplashActivity
    
    echo ""
    echo "✅ App launched!"
    echo ""
    echo "📱 To view logs, run: ./view-logs.sh"
else
    echo ""
    echo "❌ Installation failed!"
    echo ""
    echo "Possible issues:"
    echo "1. Check if APK is corrupted"
    echo "2. Check emulator/device storage"
    echo "3. Try rebuilding: ./gradlew clean assembleDebug"
    exit 1
fi
