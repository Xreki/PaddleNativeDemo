# Copyright (c) 2016 PaddlePaddle Authors. All Rights Reserve.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License

# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_CURRENT_SOURCE_DIR}/")

# Prebuilt third-party library: paddle
set(PADDLE_ROOT ${CMAKE_SOURCE_DIR}/../paddle)
find_package(Paddle)

set(SRC_FILES src/main/cpp/optical_character_recognizer_jni.cpp
              src/main/cpp/paddle_inference.cpp
              ${CMAKE_SOURCE_DIR}/../utils/cpp/android_log_wrapper.cpp)

add_library(optical_character_recognizer SHARED ${SRC_FILES})

target_include_directories(optical_character_recognizer PRIVATE
                           ${PADDLE_ROOT}/include
                           ${CMAKE_SOURCE_DIR}/../utils/cpp)

target_link_libraries(optical_character_recognizer
                      ${PADDLE_LIBRARIES}
                      ${PADDLE_THIRD_PARTY_LIBRARIES}
                      android
                      log)
