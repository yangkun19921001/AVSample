#Android 平台音视频进阶学习路线

**0 基础学习音视频路线**


##编译环境


- os: `云服务器 centos`

- NDK: [android-ndk-r15c-linux-x86_64.zip](https://dl.google.com/android/repository/android-ndk-r15c-linux-x86_64.zip?hl=zh_cn)

- FDK-AAC: `0.1.6`

- x264: 最新版-`commit 33f9e1474613f59392be5ab6a7e7abf60fa63622`

- FFmpeg: 3.4.6

- lame: 3.100

- rtmp: committed b0631b0

  
  
  

## 进阶路线

![](https://devyk.oss-cn-qingdao.aliyuncs.com/blog/20200702235045.jpg)

**进阶路线图可以在组件库找到对应代码**

> 由于工作时间比较忙，所以只能抽空更新。
>
> 暂时定于一周一更 ，不忙就多更！



| 组件库                                                       | 对应关系                          | 是否完成 | 完成时间 | 最后更新时间 |
| ------------------------------------------------------------ | --------------------------------- | -------- | -------- | -------- |
| [fdkaac_audio_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/fdkaac_audio_encode_decode/src/main/cpp) | Libfdk-aac 音频编解码             | ✅        | 2020-06-08 |  |
| [x264_video_encode](https://github.com/yangkun19921001/AVSample/tree/master/x264_video_encode)                                        | Libx264 视频编码                | ✅       | 2020-06-11 |  |
| [mediacodec_audio_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/mediacodec_audio_encode_decode) | Android MediaCodec AAC 硬编解码   | ✅       | 2020-06-14 |  |
| [mediacodec_video_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/mediacodec_video_encode_decode) | Android MediaCodec H264 硬编解码  | ✅       | 2020-06-16 |  |
| [ffmpeg_audio_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/ffmpeg_audio_encode_decode)                               | FFmpeg API 实现音频 AAC 软编解码  |  ✅         |   2020-06-21      |         |
| [ffmpeg_video_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/ffmpeg_video_encode_decode)                               | FFmpeg API 实现视频 H264 软编解码 |  ✅        |   2020-06-23      |         |
| [lame_ffmpeg_mp3_encode_decode](https://github.com/yangkun19921001/AVSample/tree/master/lame_ffmpeg_mp3_encode_decode)                                 | MP3 编解码                       | ✅          |    2020-06-25     |         |
| [JavaAVPlayer](https://github.com/yangkun19921001/AVSample/tree/master/javaavplayer) | Java API 实现音视频播放(mp3/mp4/pcm/yuv) |    ✅   |      2020-06-28     |           |
| [NativeAVPlayer](https://github.com/yangkun19921001/AVSample/tree/master/nativeavplayer) | Native 端实现音视频播放(PCM/YUV) | ✅  | 2020-07-02 |  |
| [ffmpeg_muxer](https://github.com/yangkun19921001/AVSample/tree/master/ffmpeg_muxer) | 基于 h264,AAC 文件打包为 MP4 | ✅ | 2020-08-25 | 2020-08-30 |
| [camera_recorder](https://github.com/yangkun19921001/AVSample/tree/master/camera_recorder) | 基础实战:OpenGL ES 实现相机预览->硬编码->实时 音视频/图片音频 FFmpeg 合成 mp4 |  ✅  | 2020-07-11 | 2020-08-30 |
| [AVRtmpPushSDK](https://github.com/yangkun19921001/AVRtmpPushSDK) | 中级实战- rtmp 推流 SDK | ✅  | 2020-07-19 |  |
| [AVEditer](https://github.com/yangkun19921001/AVEditer) | 高级实战-短视频 SDK(音视频编辑 + 直播推流 + 音视频播放)  | 进行中... | | |
| [NDK_OpenGL_ES3.0](https://github.com/yangkun19921001/NDK_OpenGL_ES3.0) | Native 端 OpenGL ES 3.0 实践 | 进行中... | | |
|                                                              |                                                         |           | | |
| ... |  |  | | |

 

## 进阶文档

### 一、音视频基础 + 实战项目

| 目录                                      | 是否完成 |
| ----------------------------------------- | -------- |
| [一、音频基础知识]()                      | ❌        |
| [二、视频基础知识]()                      | ❌        |
| [三、音频采集]()                          | ❌        |
| [四、视频采集]()                          | ❌        |
| [五、YUV 格式详解]()                      | ❌        |
| [六、音频软编解码]()                      | ❌        |
| [七、视频软编解码]()                      | ❌        |
| [八、音频硬编解码]()                      | ❌        |
| [九、视频硬编解码]()                      | ❌        |
| [十、渲染 PCM]()                          | ❌        |
| [十一、渲染 YUV]()                        | ❌        |
| [十二、MP3 编解码]()                      | ❌        |
| [十三、基础实战: Camera 通过 OpenGL ES 实现音视频录制为 MP4 格式]()                 | ❌        |
| [十四、中级实战:设计一款 Android RTMP SDK ]() | ❌        |
| [十五、进阶实战:设计一款播放器]()             | ❌        |
| [十六、高级实战:设计一款短视频 SDK]()         | ❌        |



### 二、OpenGL ES  提高

> 计划中...


### 三、WebRTC

> 计划中...


### 四、OpenCV 图像处理

> 计划中....


### 五、VOIP 项目

> 计划中...


## 参考

- [bigflake/mediacodec](https://bigflake.com/mediacodec/)

- [雷神-最简单的基于FFmpeg的移动端例子：Android 视频解码器-单个库版](https://blog.csdn.net/leixiaohua1020/article/details/47011021)

- [雷神- [总结]FFMPEG视音频编解码零基础学习方法](https://blog.csdn.net/leixiaohua1020/article/details/47011021)

- [展晓凯-音视频进阶指南](http://www.music-video.cn/)

- [**ffmpeg4android**](https://github.com/byhook/ffmpeg4android)

- [字节流动 · NDK_OpenGLES_3_0](https://github.com/githubhaohao/NDK_OpenGLES_3_0)


