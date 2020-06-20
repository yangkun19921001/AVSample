cmake_minimum_required(VERSION 3.4.1)


#三方开源项目 路径
set(THIRD_PARTY_ROOT ${CMAKE_SOURCE_DIR}/../../../../thirdparty)
#ffmpeg 路径
set(FFMPEG_PATH ${THIRD_PARTY_ROOT}/ffmpeg)
#fdk-aac
set(FDK-AAC_PATH ${THIRD_PARTY_ROOT}/fdk_aac)
#x264
set(X264_PATH ${THIRD_PARTY_ROOT}/x264)
#编码路径
set(NATIVE_AUDIO_ENCODE_PATH ${CMAKE_SOURCE_DIR}/encode)


#FFmpeg 头文件
include_directories(${FFMPEG_PATH}/include)
#编码路径
include_directories(${NATIVE_AUDIO_ENCODE_PATH})
#cpp 目录下的头文件
include_directories(${CMAKE_SOURCE_DIR})
#fdk-aac
#include_directories(${FDK-AAC_PATH}/include)
#x264
#include_directories(${X264_PATH}/include)

#编码 CPP
FILE(GLOB NATIVE_ENCODE_ALL_CPP ${NATIVE_AUDIO_ENCODE_PATH}/*.cpp)
#JNI 接口
FILE(GLOB JNI_CPP ${CMAKE_SOURCE_DIR}/*.cpp)


#编解码库
add_library(avcodec STATIC IMPORTED)
set_target_properties(avcodec PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libavcodec.a)

#文件格式和封装协议
add_library(avformat STATIC IMPORTED)
set_target_properties(avformat PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libavformat.a)

#重采样
add_library(swresample STATIC IMPORTED)
set_target_properties(swresample PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libswresample.a)

#工具包
add_library(avutil STATIC IMPORTED)
set_target_properties(avutil PROPERTIES IMPORTED_LOCATION ${FFMPEG_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libavutil.a)

#fdk-aac
add_library(fdkaac STATIC IMPORTED)
set_target_properties(fdkaac PROPERTIES IMPORTED_LOCATION ${FDK-AAC_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libfdk-aac.a)

#libx264
add_library(x264 STATIC IMPORTED)
set_target_properties(x264 PROPERTIES IMPORTED_LOCATION ${X264_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libx264.a)

add_library(native-ffmpeg-audio SHARED
        ${JNI_CPP}
        ${NATIVE_ENCODE_ALL_CPP}
        )


find_library(log-lib log)

target_link_libraries(
        native-ffmpeg-audio
        avformat
        avcodec
        swresample
        avutil
        fdkaac
        x264
        z #[[不加 z 会导致报未定义的错]]
        ${log-lib}

)


