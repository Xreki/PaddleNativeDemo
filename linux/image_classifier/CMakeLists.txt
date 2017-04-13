project(image_classifier)
cmake_minimum_required(VERSION 2.8)

find_package(Protobuf 3.1)
if(PROTOBUF_FOUND)
    message(STATUS "Found Protobuf: " ${PROTOBUF_LIBRARY})  
endif()

## PaddlePaddle
if(NOT PADDLE_ROOT)
    message(FATAL_ERROR "Set PADDLE_ROOT as your root directory installed PaddlePaddle")
endif()

find_path(PADDLE_INC_DIR paddle/capi.h PATHS ${PADDLE_ROOT}/include)
find_library(PADDLE_SHARED_LIB NAMES paddle_capi_shared PATHS
    ${PADDLE_ROOT}/lib)

if(PADDLE_INC_DIR AND PADDLE_SHARED_LIB)
    include_directories(${PADDLE_INC_DIR})
    set(PADDLE_LIBRARY ${PADDLE_SHARED_LIB})
    message(STATUS "Found paddle: " ${PADDLE_LIBRARY})
else()
    message(FATAL_ERROR "Cannot find PaddlePaddle")
endif()

add_executable(image_classifier main.cpp)
target_link_libraries(image_classifier
    "-Wl,--start-group"
    ${PROTOBUF_LIBRARY}
    ${PADDLE_LIBRARY}
    "-Wl,--end-group")

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
        message(STATUS "Found OpenCV: " ${OPENCV_CORE_LIB}
                ${OPENCV_HIGHGUI_LIB} ${OPENCV_IMGPROC_LIB})
      
        add_executable(image_classifier_opencv main_opencv.cpp)
        target_link_libraries(image_classifier_opencv
                "-Wl,--start-group"
                ${OPENCV_CORE_LIB}
                ${OPENCV_HIGHGUI_LIB}
                ${OPENCV_IMGPROC_LIB}
                ${PROTOBUF_LIBRARY}
                ${PADDLE_LIBRARY}
                "-Wl,--end-group")
    endif()
endif()
