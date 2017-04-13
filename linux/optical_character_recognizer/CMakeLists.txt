project(optical_character_recognizer)
cmake_minimum_required(VERSION 2.8)

if(NOT PADDLE_ROOT)
    message(FATAL_ERROR "Set PADDLE_ROOT as your root directory installed PaddlePaddle")
endif()

find_path(PADDLE_INC_DIR paddle/capi.h PATHS ${PADDLE_ROOT}/include)
#find_library(PADDLE_STATIC_LIB NAMES paddle_capi_whole PATHS
#    ${PADDLE_ROOT}/lib)
find_library(PADDLE_SHARED_LIB NAMES paddle_capi_shared PATHS
    ${PADDLE_ROOT}/lib)

if(PADDLE_INC_DIR AND PADDLE_STATIC_LIB)
    set(PADDLE_LIBRARY ${PADDLE_STATIC_LIB})
elseif(PADDLE_INC_DIR AND PADDLE_SHARED_LIB)
    set(PADDLE_LIBRARY ${PADDLE_SHARED_LIB})
else()
    message(FATAL_ERROR "Cannot find PaddlePaddle")
endif()

message(STATUS "Found paddle: " ${PADDLE_LIBRARY})

aux_source_directory(. SRC_LIST)
add_executable(${PROJECT_NAME} ${SRC_LIST})
set_property(TARGET ${PROJECT_NAME} PROPERTY C_STANDARD 99)

include_directories(${PADDLE_INC_DIR})
target_link_libraries(${PROJECT_NAME}
    "-Wl,--start-group"
    ${PADDLE_LIBRARY}
    "-Wl,--end-group")
