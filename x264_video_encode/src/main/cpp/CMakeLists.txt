cmake_minimum_required(VERSION 3.4.1)


#三方开源项目 路径
set(THIRD_PARTY_ROOT ${CMAKE_SOURCE_DIR}/../../../../thirdparty)
#设置 x264 path
set(X264_ROOT_PATH ${THIRD_PARTY_ROOT}/x264)
set(X264_LIB_PATH ${X264_ROOT_PATH}/libs/${CMAKE_ANDROID_ARCH_ABI}/libx264.a)

#native 路径
set(ENCODE_ROOT ${CMAKE_SOURCE_DIR}/encode)

#------------------------------------native--------------------------------
#native
file(GLOB ENCODE_ALL_CPP ${ENCODE_ROOT}/*.cpp)

file(GLOB NATIVE_ALL_CPP ${CMAKE_SOURCE_DIR}/*.cpp)

file(GLOB ENCODE_ALL_H ${ENCODE_ROOT}/*.h)


file(GLOB COMMON_ALL_H ${CMAKE_SOURCE_DIR}/*.h)

#native 头文件
include_directories(${ENCODE_ALL_H})
include_directories(${COMMON_ALL_H})

#------------------------------------x264--------------------------------
include_directories(${X264_ROOT_PATH}/include/)
add_library(x264 STATIC IMPORTED)
set_target_properties(x264 PROPERTIES IMPORTED_LOCATION ${X264_LIB_PATH})



#-------------------------------------------------------------------------

add_library(
        native-x264
        SHARED
        ${ENCODE_ALL_CPP}
        ${NATIVE_ALL_CPP}
)


find_library(
        log-lib
        log)



target_link_libraries(
        native-x264
        x264
        android
        ${log-lib}


)