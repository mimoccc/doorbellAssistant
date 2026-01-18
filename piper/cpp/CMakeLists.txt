cmake_minimum_required(VERSION 3.22.1)
project("piper")
find_library(
        log-lib
        log
)
include_directories(include)
add_library(
        piper
        SHARED
        espeak_wrapper.c
)
target_link_libraries(
        piper
        ${log-lib}
)
add_library(libespeak-ng SHARED IMPORTED)
set_target_properties(libespeak-ng PROPERTIES IMPORTED_LOCATION
    ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libttsespeak.so)
target_link_libraries(
        piper
        libespeak-ng
        ${log-lib}
)
