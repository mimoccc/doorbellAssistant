#!/bin/bash
# todo : from file
export ANDROID_NDK_HOME="/home/mimo/Android/Sdk/ndk"
export ANDROID_SDK_HOME="/home/mimo/Android/Sdk"
git clone https://github.com/alphacep/vosk-api.git
cd vosk-api/android/lib || exit
./build-vosk.sh
./gradlew build