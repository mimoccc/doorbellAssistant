#!/bin/bash
#
# Copyright (c) Milan Jurkulák 2026.
# Contact:
# e: mimoccc@gmail.com
# e: mj@mjdev.org
# w: https://mjdev.org
# w: https://github.com/mimoccc
# w: https://www.linkedin.com/in/milan-jurkul%C3%A1k-742081284/
#

# Publish Phone Module to Maven
# Usage: ./publish-phone-module.sh
echo "Publishing Phone module to Maven..."
# Check if we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    echo "Error: Please run this script from the project root directory"
    exit 1
fi
# Check if phone module exists
if [ ! -d "phone" ]; then
    echo "Error: Phone module directory not found"
    exit 1
fi
# Build and publish the phone module
./gradlew :phone:publishToMavenLocal
if [ $? -eq 0 ]; then
    echo "✅ Phone module published successfully to local Maven repository!"
    echo "📦 You can now use it in other projects with:"
    echo "   implementation 'org.mjdev:phone:1.0.0'"
else
    echo "❌ Failed to publish Phone module"
    exit 1
fi
