cmake_minimum_required(VERSION 3.4.1)

set(SLES_PATH  ${CMAKE_SOURCE_DIR}/sles)
set(GLES_PATH  ${CMAKE_SOURCE_DIR}/gles)
set(JNI_PATH   ${CMAKE_SOURCE_DIR}/jni)


FILE(GLOB SLES_ALL_CPP ${SLES_PATH}/*.cpp)
FILE(GLOB GLES_ALL_CPP ${GLES_PATH}/*.cpp)
FILE(GLOB JNI_ALL_CPP  ${JNI_PATH}/*.cpp)
FILE(GLOB ROOT  ${CMAKE_SOURCE_DIR}/*.cpp)

include_directories(${SLES_PATH}/)
include_directories(${GLES_PATH}/)
include_directories(${CMAKE_SOURCE_DIR}/)

add_library(
        native-player
        SHARED
        ${SLES_ALL_CPP}
        ${GLES_ALL_CPP}
        ${JNI_ALL_CPP}
        ${ROOT}
)

find_library(
        log-lib
        log
)



target_link_libraries(
        native-player
        OpenSLES
        GLESv2 EGL #[[EGL 是 nativeWindow 和 openGL ES 中间的桥梁]]
        android
        ${log-lib}
)