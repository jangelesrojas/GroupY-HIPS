# Sets the minimum CMake version required for this native build.
cmake_minimum_required(VERSION 3.22.1)

# Names this native project.
project("hips")

# ExternalProject lets this CMake file build libjpeg-turbo as a dependency.
include(ExternalProject)

# Location of the libjpeg-turbo source code inside the Android project.
set(LIBJPEG_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/third_party/libjpeg-turbo)

# Location where the compiled libjpeg-turbo files will be installed during build.
set(LIBJPEG_INSTALL_DIR ${CMAKE_BINARY_DIR}/libjpeg-turbo-install)

# Stops the build early if the libjpeg-turbo source folder is missing.
# This gives a clearer error instead of failing later with confusing linker errors.
if(NOT EXISTS "${LIBJPEG_SOURCE_DIR}/CMakeLists.txt")
    message(FATAL_ERROR "libjpeg-turbo source not found at: ${LIBJPEG_SOURCE_DIR}")
endif()

# Builds libjpeg-turbo as an external project.
# This gives native-lib.cpp access to libjpeg so it can read and write JPEG DCT coefficients.
ExternalProject_Add(
    libjpeg_turbo_ext

    # Tells CMake where the libjpeg-turbo source code is.
    SOURCE_DIR ${LIBJPEG_SOURCE_DIR}

    # Passes Android build settings into libjpeg-turbo's own CMake build.
    CMAKE_ARGS
        -G${CMAKE_GENERATOR}
        -DCMAKE_MAKE_PROGRAM=${CMAKE_MAKE_PROGRAM}
        -DCMAKE_TOOLCHAIN_FILE=${CMAKE_TOOLCHAIN_FILE}
        -DANDROID_ABI=${ANDROID_ABI}
        -DANDROID_PLATFORM=${ANDROID_PLATFORM}
        -DCMAKE_BUILD_TYPE=${CMAKE_BUILD_TYPE}
        -DCMAKE_C_COMPILER=${CMAKE_C_COMPILER}
        -DCMAKE_CXX_COMPILER=${CMAKE_CXX_COMPILER}

        # Build a static libjpeg library so it can be linked into the HIPS native library.
        -DENABLE_SHARED=FALSE
        -DENABLE_STATIC=TRUE

        # Install location for headers and the compiled libjpeg.a file.
        -DCMAKE_INSTALL_PREFIX=${LIBJPEG_INSTALL_DIR}

    # Tells Gradle/CMake what file should exist after libjpeg-turbo finishes building.
    BUILD_BYPRODUCTS
        ${LIBJPEG_INSTALL_DIR}/lib/libjpeg.a
)

# Builds the main native HIPS library from native-lib.cpp.
# Kotlin loads this through System.loadLibrary("hips").
add_library(hips SHARED native-lib.cpp)

# Makes sure libjpeg-turbo builds before the HIPS native library.
add_dependencies(hips libjpeg_turbo_ext)

# Adds the libjpeg-turbo header files so native-lib.cpp can include jpeglib.h.
target_include_directories(hips PRIVATE
    ${LIBJPEG_INSTALL_DIR}/include
)

# Links the HIPS native library with Android system libraries and libjpeg.
target_link_libraries(hips
    # Android native library support.
    android

    # Android logging support, useful if native logging is added later.
    log

    # Static libjpeg library built from libjpeg-turbo.
    ${LIBJPEG_INSTALL_DIR}/lib/libjpeg.a
)
