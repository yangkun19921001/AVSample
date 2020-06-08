cmake_minimum_required(VERSION 3.4.1)



#三方开源项目 路径
set(THIRD_PARTY_ROOT ${CMAKE_SOURCE_DIR}/../../../../thirdparty)



#fdkaac
set(FDKAAC_PATH_ROOT ${THIRD_PARTY_ROOT}/fdk_aac)
set(FDKAAC_STATIC_LIB ${FDKAAC_PATH_ROOT}/libs/${CMAKE_ANDROID_ARCH_ABI}/libfdk-aac.a)
set(FDKAAC_ALL_H ${FDKAAC_PATH_ROOT}/include/)


FILE(GLOB AAC_ENCODE_CPP ${CMAKE_SOURCE_DIR}/encode/*.cpp)
FILE(GLOB AAC_DECODE_CPP ${CMAKE_SOURCE_DIR}/decode/*.cpp)
FILE(GLOB NATIVE_ALL_CPP ${CMAKE_SOURCE_DIR}/*.cpp)
FILE(GLOB ENCODE_H_PATH ${CMAKE_SOURCE_DIR}/encode/*.h)
FILE(GLOB COMMON_H_PATH ${CMAKE_SOURCE_DIR}/*.h)

include_directories(${FDKAAC_ALL_H})
include_directories(${COMMON_H_PATH})
include_directories(${ENCODE_H_PATH})
include_directories(${CMAKE_SOURCE_DIR}/decode)

add_library(fdk-aac STATIC IMPORTED)
set_target_properties(fdk-aac PROPERTIES IMPORTED_LOCATION ${FDKAAC_STATIC_LIB})

add_library(
        native-fdkaac SHARED
        ${NATIVE_ALL_CPP}
        ${AAC_ENCODE_CPP}
        ${AAC_DECODE_CPP}
)

find_library(
        log-lib
        log)


target_link_libraries(
        native-fdkaac
        fdk-aac
        android
        ${log-lib})