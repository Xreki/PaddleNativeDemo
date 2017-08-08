project(image_classifier)
cmake_minimum_required(VERSION 2.8)

set(PROJ_ROOT ${CMAKE_CURRENT_SOURCE_DIR})
set(BINARY_NAME "image_classifier")

## PaddlePaddle
if(NOT PADDLE_ROOT)
    message(FATAL_ERROR "Set PADDLE_ROOT as your root directory installed PaddlePaddle")
endif()

find_path(PADDLE_INC_DIR paddle/capi.h PATHS ${PADDLE_ROOT}/include)
find_library(PADDLE_SHARED_LIB NAMES paddle_capi_shared PATHS
    ${PADDLE_ROOT}/lib
    ${PADDLE_ROOT}/lib/${CMAKE_ANDROID_ARCH_ABI})

if(PADDLE_INC_DIR AND PADDLE_SHARED_LIB)
    include_directories(${PADDLE_INC_DIR})
    set(PADDLE_LIBRARY ${PADDLE_SHARED_LIB})
    set(EXTERNAL_LIBS ${EXTERNAL_LIBS} ${PADDLE_LIBRARY})
    message(STATUS "Found paddle: " ${PADDLE_LIBRARY})
else()
    message(FATAL_ERROR "Cannot find PaddlePaddle")
endif()

## OpenCV
if(OPENCV_ROOT)
    find_path(OPENCV_INC_DIR opencv2/opencv.hpp PATHS ${OPENCV_ROOT}/include)
    find_library(OPENCV_CORE_LIB NAMES opencv_core PATHS ${OPENCV_ROOT}/lib)
    find_library(OPENCV_HIGHGUI_LIB NAMES opencv_highgui PATHS
        ${OPENCV_ROOT}/lib)
    find_library(OPENCV_IMGPROC_LIB NAMES opencv_imgproc PATHS
        ${OPENCV_ROOT}/lib)
    if(OPENCV_INC_DIR AND OPENCV_CORE_LIB AND OPENCV_HIGHGUI_LIB AND OPENCV_IMGPROC_LIB)
        include_directories(${OPENCV_INC_DIR})
        add_definitions(-DUSE_OPENCV)
        message(STATUS "Found OpenCV: " ${OPENCV_CORE_LIB}
                ${OPENCV_HIGHGUI_LIB} ${OPENCV_IMGPROC_LIB})
      
        set(BINARY_NAME image_classifier_opencv)
        set(EXTERNAL_LIBS ${EXTERNAL_LIBS} ${OPENCV_CORE_LIB} ${OPENCV_HIGHGUI_LIB} ${OPENCV_IMGPROC_LIB})
    endif()
endif()

set(CMAKE_C_FLAGS "-O3")
set(CMAKE_CXX_FLAGS "-O3 -std=c++11")

add_executable(${BINARY_NAME} main.cpp ../utils/timer.cc)
include_directories(${PROJ_ROOT}/..)
target_link_libraries(${BINARY_NAME}
    "-Wl,--start-group"
    ${EXTERNAL_LIBS}
    "-Wl,--end-group")
