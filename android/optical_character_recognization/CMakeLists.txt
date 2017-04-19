# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# Prebuilt third-party library: paddle
set(PADDLE_ROOT ${CMAKE_SOURCE_DIR}/../paddle)
add_library(paddle_inference SHARED IMPORTED)
set_target_properties(paddle_inference PROPERTIES IMPORTED_LOCATION
                      ${PADDLE_ROOT}/lib/${ANDROID_ABI}/libpaddle_capi_shared.so)

add_library(optical_character_recognizer SHARED
            src/main/cpp/optical_character_recognizer_jni.cpp
            ${CMAKE_SOURCE_DIR}/../utils/cpp/android_log_wrapper.cpp)

target_include_directories(optical_character_recognizer PRIVATE
                           ${PADDLE_ROOT}/include
                           ${CMAKE_SOURCE_DIR}/../utils/cpp)

target_link_libraries(optical_character_recognizer
                      paddle_inference
                      android
                      log)
