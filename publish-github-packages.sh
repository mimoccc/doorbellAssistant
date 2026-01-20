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

# Publish Phone Module to GitHub Packages
# Usage: ./publish-github-packages.sh
# Requires: JDK 17+
echo "Publishing Phone module to GitHub Packages..."
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
# Check for GitHub token
if [ -z "$GITHUB_TOKEN" ]; then
    echo "Error: GITHUB_TOKEN environment variable not set"
    echo "Please set it with: export GITHUB_TOKEN=your_token_here"
    exit 1
fi
# Build and publish the phone module
./gradlew :phone:publish
if [ $? -eq 0 ]; then
    echo "✅ Phone module published successfully to GitHub Packages!"
    echo "📦 You can now use it in other projects with:"
    echo ""
    echo "repositories {"
    echo "    maven {"
    echo "        url = uri('https://maven.pkg.github.com/mimoccc/doorbellAssistant')"
    echo "        credentials {"
    echo "            username = 'YOUR_GITHUB_USERNAME'"
    echo "            password = 'YOUR_GITHUB_TOKEN'"
    echo "        }"
    echo "    }"
    echo "}"
    echo ""
    echo "dependencies {"
    echo "    implementation 'org.mjdev:phone:1.0.0'"
    echo "}"
else
    echo "❌ Failed to publish Phone module"
    exit 1
fi
