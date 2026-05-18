# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("wolfram")

# This is not a library but an executable whose purpose is to receive tun fd
# from the main process over a unix socket and pass it to xray-core. It is named
# like this simply because the build system automatically packages all files
# following the lib*.so naming into resulting APK, making my life much easier
add_executable(libxray-wrapper.so xray_wrapper.c)
