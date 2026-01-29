cmake_minimum_required(VERSION 3.4.1)

# Create the JNI library
add_library(whisper_jni SHARED
    jni.c
)

# Find and link required libraries
find_library(log-lib log)
find_library(android-lib android)

# Link libraries
target_link_libraries(whisper_jni
    whisper
    ${log-lib}
    ${android-lib}
)

# Include directories
target_include_directories(whisper_jni PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/../include
    ${CMAKE_CURRENT_SOURCE_DIR}/../ggml/include
)

# Compiler options
target_compile_options(whisper_jni PRIVATE
    -O3
    -std=c11
)

# Add ARM optimizations
if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    target_compile_options(whisper_jni PRIVATE
        -mfpu=neon
        -mfloat-abi=softfp
        -DNDEBUG
    )
endif()

if (${ANDROID_ABI} STREQUAL "arm64-v8a")
    target_compile_options(whisper_jni PRIVATE
        -march=armv8-a+fp16
        -DNDEBUG
    )
endif()