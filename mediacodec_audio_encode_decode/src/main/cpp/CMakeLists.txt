cmake_minimum_required(VERSION 3.4.1)



#三方开源项目 路径

set(JNI_PATH ${CMAKE_SOURCE_DIR})

FILE(GLOB JNI_ALL_H ${JNI_PATH}/*.h)
FILE(GLOB JNI_ALL_CPP ${JNI_PATH}/*.cpp)


include_directories(${JNI_ALL_H})


add_library(
        native-jni
        SHARED
        ${JNI_ALL_CPP}


)
target_link_libraries(
        native-jni
)