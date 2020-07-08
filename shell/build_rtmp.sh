#!/bin/bash



ARCH=$1

source config.sh $ARCH
LIBS_DIR=$(cd `dirname $0`; pwd)/libs/librtmp
echo "LIBS_DIR="$LIBS_DIR


# https://github.com/yixia/librtmp.git

cd /root/android/library/librtmp/librtmp

TOOLCHAIN=$ANDROID_NDK_ROOT/toolchains/$TOOLCHAIN_BASE-$AOSP_TOOLCHAIN_SUFFIX/prebuilt/linux-x86_64
CROSS_COMPILE=$TOOLCHAIN/bin/$TOOLNAME_BASE-
SYSROOT=$ANDROID_NDK_ROOT/platforms/$AOSP_API/$AOSP_ARCH

PREFIX=$LIBS_DIR/$AOSP_ABI

#配置NDK 环境变量
NDK_ROOT=$ANDROID_NDK_ROOT


#指定 Android API
ANDROID_API=21


export XCFLAGS="-isysroot $NDK_ROOT/sysroot -isystem $NDK_ROOT/sysroot/usr/include/$TOOLNAME_BASE -D__ANDROID_API__=$ANDROID_API"
export XLDFLAGS="--sysroot=$SYSROOT "
export CROSS_COMPILE=$CROSS_COMPILE

make clean
make install SYS=android prefix=$PREFIX CRYPTO= SHARED=  XDEF=-DNO_SSL
