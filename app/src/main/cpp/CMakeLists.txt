cmake_minimum_required(VERSION 3.22.1)
project("hips")

include(ExternalProject)

set(LIBJPEG_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/libjpeg-turbo)
set(LIBJPEG_INSTALL_DIR ${CMAKE_BINARY_DIR}/libjpeg-turbo-install)

if(NOT EXISTS "${LIBJPEG_SOURCE_DIR}/CMakeLists.txt")
    message(FATAL_ERROR "libjpeg-turbo source not found at: ${LIBJPEG_SOURCE_DIR}")
endif()

ExternalProject_Add(
    libjpeg_turbo_ext
    SOURCE_DIR ${LIBJPEG_SOURCE_DIR}
    CMAKE_ARGS
        -G${CMAKE_GENERATOR}
        -DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM}
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
        -DCMAKE_C_COMPILER=${CMAKE_C_COMPILER}
        -DCMAKE_CXX_COMPILER=${CMAKE_CXX_COMPILER}
        -DENABLE_SHARED=FALSE
        -DENABLE_STATIC=TRUE
        -DCMAKE_INSTALL_PREFIX=${LIBJPEG_INSTALL_DIR}
    BUILD_BYPRODUCTS
        ${LIBJPEG_INSTALL_DIR}/lib/libjpeg.a
)

add_library(hips SHARED native-lib.cpp)

add_dependencies(hips libjpeg_turbo_ext)

target_include_directories(hips PRIVATE
    ${LIBJPEG_INSTALL_DIR}/include
)

target_link_libraries(hips
    android
    log
    ${LIBJPEG_INSTALL_DIR}/lib/libjpeg.a
)