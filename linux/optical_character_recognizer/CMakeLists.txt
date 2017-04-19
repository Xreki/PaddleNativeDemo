project(optical_character_recognizer)
cmake_minimum_required(VERSION 2.8)

find_package(Protobuf 3.1)
if(PROTOBUF_FOUND)
    message(STATUS "Found Protobuf: " ${PROTOBUF_LIBRARY})  
endif()

if(NOT PADDLE_ROOT)
    message(FATAL_ERROR "Set PADDLE_ROOT as your root directory installed PaddlePaddle")
endif()

find_path(PADDLE_INC_DIR paddle/capi.h PATHS ${PADDLE_ROOT}/include)
find_library(PADDLE_SHARED_LIB NAMES paddle_capi_shared PATHS
    ${PADDLE_ROOT}/lib)

if(PADDLE_INC_DIR AND PADDLE_STATIC_LIB)
    set(PADDLE_LIBRARY ${PADDLE_STATIC_LIB})
elseif(PADDLE_INC_DIR AND PADDLE_SHARED_LIB)
    include_directories(${PADDLE_INC_DIR})
    set(PADDLE_LIBRARY ${PADDLE_SHARED_LIB})
    message(STATUS "Found paddle: " ${PADDLE_LIBRARY})
else()
    message(FATAL_ERROR "Cannot find PaddlePaddle")
endif()

add_executable(${PROJECT_NAME} main.cpp)

target_link_libraries(${PROJECT_NAME}
    "-Wl,--start-group"
    ${PROTOBUF_LIBRARY}
    ${PADDLE_LIBRARY}
    "-Wl,--end-group")
