# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

# NDK pre-build library: log
find_library(log-lib log)

# Prebuilt third-party library: paddle
set(PADDLE_ROOT ${CMAKE_SOURCE_DIR}/../paddle)
add_library(paddle_inference SHARED IMPORTED)
set_target_properties(paddle_inference PROPERTIES IMPORTED_LOCATION
                      ${PADDLE_ROOT}/lib/${ANDROID_ABI}/libpaddle_capi_shared.so)

add_library(image_classifier SHARED
            src/main/cpp/ImageClassifier_jni.cpp)

target_include_directories(image_classifier PRIVATE
                           ${PADDLE_ROOT}/include)

target_link_libraries(image_classifier
                      paddle_inference
                      ${log-lib})
