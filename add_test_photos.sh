#!/bin/bash

# Script to add test photos to Android emulator
# Usage: ./add_test_photos.sh

echo "Adding test photos to Android emulator..."

# Create test photos directory
mkdir -p ~/Downloads/test_photos

# Download sample images
echo "Downloading sample images..."
curl -o ~/Downloads/test_photos/sample1.jpg "https://picsum.photos/400/400?random=1"
curl -o ~/Downloads/test_photos/sample2.jpg "https://picsum.photos/400/400?random=2"
curl -o ~/Downloads/test_photos/sample3.jpg "https://picsum.photos/400/400?random=3"
curl -o ~/Downloads/test_photos/profile1.jpg "https://picsum.photos/300/300?random=4"
curl -o ~/Downloads/test_photos/profile2.jpg "https://picsum.photos/300/300?random=5"

# Check if emulator is connected
if ! adb devices | grep -q "device$"; then
    echo "Error: No Android device/emulator connected"
    echo "Please start your emulator and try again"
    exit 1
fi

# Push images to emulator
echo "Pushing images to emulator..."
adb push ~/Downloads/test_photos/sample1.jpg /sdcard/Pictures/
adb push ~/Downloads/test_photos/sample2.jpg /sdcard/Pictures/
adb push ~/Downloads/test_photos/sample3.jpg /sdcard/Pictures/
adb push ~/Downloads/test_photos/profile1.jpg /sdcard/Pictures/
adb push ~/Downloads/test_photos/profile2.jpg /sdcard/Pictures/

# Refresh media database
echo "Refreshing media database..."
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/sample1.jpg
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/sample2.jpg
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/sample3.jpg
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/profile1.jpg
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Pictures/profile2.jpg

echo "✅ Test photos added successfully!"
echo "You can now find these photos in your emulator's Gallery app:"
echo "- sample1.jpg"
echo "- sample2.jpg" 
echo "- sample3.jpg"
echo "- profile1.jpg"
echo "- profile2.jpg"
echo ""
echo "To test S3 upload:"
echo "1. Run your app"
echo "2. Go to registration screen"
echo "3. Tap 'Add Profile Photo'"
echo "4. Select one of the test photos"
echo "5. Watch the upload progress indicator"
