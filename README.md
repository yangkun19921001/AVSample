#Android 平台音视频进阶学习路线

**0 基础学习音视频路线**


##编译环境


- os: `云服务器 centos`

- NDK: [android-ndk-r15c-linux-x86_64.zip](https://dl.google.com/android/repository/android-ndk-r15c-linux-x86_64.zip?hl=zh_cn)

- FDK-AAC: `0.1.6`

- x264: 最新版-`commit 33f9e1474613f59392be5ab6a7e7abf60fa63622`

- FFmpeg: 3.4.6

- lame:

  

## 进阶路线

![](https://devyk.oss-cn-qingdao.aliyuncs.com/blog/20200611233219.jpg)

**进阶路线图可以在组件库找到对应代码**

> 由于工作时间比较忙，所以只能抽空更新。
>
> 暂时定于一周一更 ，不忙就多更！

| 组件库                                                       | 对应关系                          | 是否完成 | 完成时间 |
| ------------------------------------------------------------ | --------------------------------- | -------- | -------- |
| [fdkaac_audio_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/fdkaac_audio_encode_decode/src/main/cpp) | Libfdk-aac 音频编解码             | ✅        | 2020-06-08 |
| [x264_video_encode](https://github.com/yangkun19921001/AVSample/tree/master/x264_video_encode)                                        | Libx264 视频编码                | ✅       | 2020-06-11 |
| [mediacodec_audio_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/mediacodec_audio_encode_decode) | Android MediaCodec AAC 硬编解码   | ✅       | 2020-06-14 |
| [mediacodec_video_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/mediacodec_video_encode_decode) | Android MediaCodec H264 硬编解码  | ✅       | 2020-06-16 |
| [ffmpeg_audio_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/ffmpeg_audio_encode_decode)                               | FFmpeg API 实现音频 AAC 软编解码  |  ✅         |   2020-06-21      |
| [ffmpeg_video_encode_decode]()                               | FFmpeg API 实现视频 H264 软编解码 | ❌        |         |
| [lame_audio_encode_decode]()                                 | MP3 编解码                       | ❌        |         |
|                                                              |                                 |          |          |

 

## 进阶文档

### 一、音视频基础 + 实战短视频 SDK

| 目录                              | 是否完成 |
| --------------------------------- | -------- |
| [一、音频基础知识]()              | ❌        |
| [二、视频基础知识]()              | ❌        |
| [三、音频采集]()                  | ❌        |
| [四、视频采集]()                  | ❌        |
| [五、音频软编解码]()              | ❌        |
| [六、视频软编解码]()              | ❌        |
| [七、音频硬编解码]()              | ❌        |
| [八、视频硬编解码]()              | ❌        |
| [九、渲染 PCM]()                  | ❌        |
| [十、渲染 YUV]()                  | ❌        |
| [十一、MP3 编解码]()              | ❌        |
| [十二、实战:设计一款播放器]()     | ❌        |
| [十三、实战:设计一款短视频 SDK]() | ❌        |

### 二、OpenGL ES 提高

> 计划中...


### 三、WebRTC

> 计划中...


### 四、OpenCV 图像处理

> 计划中....


### 五、VOIP Doubango 源码分析

> 计划中...


## 参考


- [雷神-最简单的基于FFmpeg的移动端例子：Android 视频解码器-单个库版](https://blog.csdn.net/leixiaohua1020/article/details/47011021)

- [雷神- [总结]FFMPEG视音频编解码零基础学习方法](https://blog.csdn.net/leixiaohua1020/article/details/47011021)

- [展晓凯-音视频进阶指南](http://www.music-video.cn/)

- [**ffmpeg4android**](https://github.com/byhook/ffmpeg4android)

- [字节流动 · NDK_OpenGLES_3_0](https://github.com/githubhaohao/NDK_OpenGLES_3_0)


