//
// Created by 阳坤 on 2020-08-23.
//

#include "FFmpegMuxer.h"
#include "logger.h"

#define IO_BUFFER_SIZE 32768  //缓存32k

static FILE *g_fileH264 = NULL;
FILE *AUDIO_FILE = 0;
FILE *VIDEO_FILE = 0;

template<typename T>
T RandT(T _min, T _max) {
    T temp;
    if (_min > _max) {
        temp = _max;
        _max = _min;
        _min = temp;
    }
    return rand() / (double) RAND_MAX * (_max - _min) + _min;
}

int FillAudioIoBuffer(void *opaque, unsigned char *o_pbBuf, int i_iMaxSize) {
    int iRet = -1;
    if (!feof(g_fileH264)) {
        iRet = fread(o_pbBuf, 1, RandT(i_iMaxSize / 2, i_iMaxSize), g_fileH264);
    } else {
    }
    return iRet;
}


int FillVideoIoBuffer(void *opaque, unsigned char *o_pbBuf, int i_iMaxSize) {
    int iRet = -1;
    if (!feof(g_fileH264)) {
        iRet = fread(o_pbBuf, 1, RandT(i_iMaxSize / 2, i_iMaxSize), g_fileH264);
    } else {
    }
    return iRet;
}

void FFmpegMuxer::WritePTS(AVPacket *avPacket, AVStream *inputStream) {
    if (avPacket->pts == AV_NOPTS_VALUE) {
        //Write PTS
        AVRational time_base = inputStream->time_base;
        //计算两帧的时间
        int64_t calc_duration =
                (double) AV_TIME_BASE / av_q2d(inputStream->r_frame_rate);
        //Parameters
        avPacket->pts = (double) (frameIndex * calc_duration) /
                        (double) (av_q2d(time_base) * AV_TIME_BASE);
        avPacket->dts = avPacket->pts;
        avPacket->duration = (double) calc_duration /
                             (double) (av_q2d(time_base) * AV_TIME_BASE);
        frameIndex++;
    }
}

int
FFmpegMuxer::NewStream(AVFormatContext *avFormatContext, int &inputIndex, AVMediaType mediaType) {
    int outputStreamIndex = -1;
    for (int index = 0; index < avFormatContext->nb_streams; index++) {
        //根据输入流创建一个输出流
        if (avFormatContext->streams[index]->codecpar->codec_type == mediaType) {
            AVStream *in_stream = avFormatContext->streams[index];
            AVCodecContext *codec_ctx = avcodec_alloc_context3(NULL);
            avcodec_parameters_to_context(codec_ctx, in_stream->codecpar);
            AVStream *out_stream = avformat_new_stream(outFormatCtx, codec_ctx->codec);
            inputIndex = index;
            if (NULL == out_stream) {
                LOGE("Could't allocating output stream");
                return outputStreamIndex;
            }
            outputStreamIndex = out_stream->index;
            if (avcodec_parameters_from_context(out_stream->codecpar, codec_ctx) < 0) {
                LOGE("Could't to copy context from input to output stream codec context");
                return outputStreamIndex;
            }
            codec_ctx->codec_tag = 0;
            if (outFormatCtx->oformat->flags & AVFMT_GLOBALHEADER) {
                codec_ctx->flags |= CODEC_FLAG_GLOBAL_HEADER;
            }
            break;
        }
    }
    return outputStreamIndex;
}

static inline long getCurrentTime() {
    struct timeval tv;
    gettimeofday(&tv, nullptr);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}


/**
 * H264 和 AAC 文件封住 Mp4
 * @param videoPath
 * @param audioPath
 * @param outputPath
 * @return
 */
int FFmpegMuxer::AAC_H264_FILE_To_MP4(const char *videoPath, const char *audioPath, const char *outputPath) {
    //1.注册所有组件
    av_register_all();
    //2.打开输出上下文
    avformat_alloc_output_context2(&outFormatCtx, NULL, NULL, outputPath);
    if (NULL == outFormatCtx) {
        LOGE("Could't create output context");
        return 0;
    }

    //3.打开视频输入文件上下文
    if (avformat_open_input(&videoFormatCtx, videoPath, 0, 0) < 0) {
        LOGE("Could't open input file");
        return 0;
    }
    if (avformat_find_stream_info(videoFormatCtx, 0) < 0) {
        LOGE("Could't find input stream information");
        return 0;
    }
    //4.打开音频输入文件上下文
    if (avformat_open_input(&audioFormatCtx, audioPath, 0, 0) < 0) {
        LOGE("Could't open input file");
        return 0;
    }
    if (avformat_find_stream_info(audioFormatCtx, 0) < 0) {
        LOGE("Could't find input stream information");
        return 0;

    }

    //5.新建视频和音频输出流
    videoStreamIndex = NewStream(videoFormatCtx, videoIndex, AVMEDIA_TYPE_VIDEO);
    audioStreamIndex = NewStream(audioFormatCtx, audioIndex, AVMEDIA_TYPE_AUDIO);

    //6.打开输出文件
    if (!(outFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        if (avio_open(&outFormatCtx->pb, outputPath, AVIO_FLAG_WRITE) < 0) {
            LOGE("Could't open output %s", outputPath);
            return 0;
        }
    }
    //7.写文件头
    int ret = avformat_write_header(outFormatCtx, NULL);
    if (ret < 0) {
        LOGE("Could't write header %s", av_err2str(ret));
        return 0;
    }

    int audioExit = 0;
    int videoExit = 0;
    while (true) {
        int streamIndex = 0;
        AVStream *inStream, *outStream;


        if (av_compare_ts(cur_pts_v, videoFormatCtx->streams[videoIndex]->time_base, cur_pts_a,
                          audioFormatCtx->streams[audioIndex]->time_base) <= 0) {
            streamIndex = videoStreamIndex;
            while (!videoExit) {
                int ret = av_read_frame(videoFormatCtx, &avPacket);
                if (ret < 0) {
                    videoExit = true;
                    break;
                }
                inStream = videoFormatCtx->streams[avPacket.stream_index];
                outStream = outFormatCtx->streams[streamIndex];
                if (avPacket.stream_index == videoIndex) {
                    //没有PTS的需要写入 Raw H.264
                    WritePTS(&avPacket, inStream);
                    cur_pts_v = avPacket.pts;
                    break;
                }
            }

        } else {
            streamIndex = audioStreamIndex;
            while (!audioExit) {

                int ret = av_read_frame(audioFormatCtx, &avPacket);
                if (ret < 0) {
                    audioExit = true;
                    break;
                }
                inStream = audioFormatCtx->streams[avPacket.stream_index];
                outStream = outFormatCtx->streams[streamIndex];
                if (avPacket.stream_index == audioIndex) {
                    WritePTS(&avPacket, inStream);
                    cur_pts_a = avPacket.pts;
                    break;
                }
            }
        }
        if (audioExit) {
            LOGE("准备退出！");
            break;
        }

        //8.转换PTS/DTS
        avPacket.pts = av_rescale_q_rnd(avPacket.pts, inStream->time_base, outStream->time_base,
                                        (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        avPacket.dts = av_rescale_q_rnd(avPacket.dts, inStream->time_base, outStream->time_base,
                                        (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        avPacket.duration = av_rescale_q(avPacket.duration, inStream->time_base,
                                         outStream->time_base);
        avPacket.pos = -1;
        avPacket.stream_index = streamIndex;

        //写数据
        if (av_interleaved_write_frame(outFormatCtx, &avPacket) < 0) {
            LOGE("Could't write frame");
            break;
        }
        av_packet_unref(&avPacket);
    }
    //9.写文件尾
    av_write_trailer(outFormatCtx);

    avformat_close_input(&videoFormatCtx);
    avformat_close_input(&audioFormatCtx);
    //关闭操作
    if (outFormatCtx && !(outFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        avio_close(outFormatCtx->pb);
    }
    avformat_free_context(outFormatCtx);
    LOGE("merge mp4 success! outPath:%s", outputPath);
    return 0;
}



int
FFmpegMuxer::Transform(const char *videoPath, const char *audioPath, const char *outputPath) {
    //1.注册所有组件
    av_register_all();
    //2.打开输出上下文
    avformat_alloc_output_context2(&outFormatCtx, NULL, NULL, outputPath);
    if (NULL == outFormatCtx) {
        LOGE("Could't create output context");
        return 0;
    }

    //3.打开视频输入文件上下文
    if (avformat_open_input(&videoFormatCtx, videoPath, 0, 0) < 0) {
        LOGE("Could't open input file");
        return 0;
    }
    if (avformat_find_stream_info(videoFormatCtx, 0) < 0) {
        LOGE("Could't find input stream information");
        return 0;
    }
    //4.打开音频输入文件上下文
    if (avformat_open_input(&audioFormatCtx, audioPath, 0, 0) < 0) {
        LOGE("Could't open input file");
        return 0;
    }
    if (avformat_find_stream_info(audioFormatCtx, 0) < 0) {
        LOGE("Could't find input stream information");
        return 0;

    }

    //5.新建视频和音频输出流
    videoStreamIndex = NewStream(videoFormatCtx, videoIndex, AVMEDIA_TYPE_VIDEO);
    audioStreamIndex = NewStream(audioFormatCtx, audioIndex, AVMEDIA_TYPE_AUDIO);

    //6.打开输出文件
    if (!(outFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        if (avio_open(&outFormatCtx->pb, outputPath, AVIO_FLAG_WRITE) < 0) {
            LOGE("Could't open output %s", outputPath);
            return 0;
        }
    }
    //7.写文件头
    int ret = avformat_write_header(outFormatCtx, NULL);
    if (ret < 0) {
        LOGE("Could't write header %s", av_err2str(ret));
        return 0;
    }

    int audioExit = 0;
    int videoExit = 0;
    while (true) {
        int streamIndex = 0;
        AVStream *inStream, *outStream;


        if (av_compare_ts(cur_pts_v, videoFormatCtx->streams[videoIndex]->time_base, cur_pts_a,
                          audioFormatCtx->streams[audioIndex]->time_base) <= 0) {
            streamIndex = videoStreamIndex;
            while (!videoExit) {
                int ret = av_read_frame(videoFormatCtx, &avPacket);
                if (ret < 0) {
                    videoExit = true;
                    break;
                }
                inStream = videoFormatCtx->streams[avPacket.stream_index];
                outStream = outFormatCtx->streams[streamIndex];
                if (avPacket.stream_index == videoIndex) {
                    //没有PTS的需要写入 Raw H.264
                    WritePTS(&avPacket, inStream);
                    cur_pts_v = avPacket.pts;
                    break;
                }
            }

        } else {
            streamIndex = audioStreamIndex;
            while (!audioExit) {

                int ret = av_read_frame(audioFormatCtx, &avPacket);
                if (ret < 0) {
                    audioExit = true;
                    break;
                }
                inStream = audioFormatCtx->streams[avPacket.stream_index];
                outStream = outFormatCtx->streams[streamIndex];
                if (avPacket.stream_index == audioIndex) {
                    WritePTS(&avPacket, inStream);
                    cur_pts_a = avPacket.pts;
                    break;
                }
            }
        }
        if (audioExit) {
            LOGE("准备退出！");
            break;
        }

        //8.转换PTS/DTS
        avPacket.pts = av_rescale_q_rnd(avPacket.pts, inStream->time_base, outStream->time_base,
                                        (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        avPacket.dts = av_rescale_q_rnd(avPacket.dts, inStream->time_base, outStream->time_base,
                                        (AVRounding) (AV_ROUND_NEAR_INF | AV_ROUND_PASS_MINMAX));
        avPacket.duration = av_rescale_q(avPacket.duration, inStream->time_base,
                                         outStream->time_base);
        avPacket.pos = -1;
        avPacket.stream_index = streamIndex;

        //写数据
        if (av_interleaved_write_frame(outFormatCtx, &avPacket) < 0) {
            LOGE("Could't write frame");
            break;
        }
        av_packet_unref(&avPacket);
    }
    //9.写文件尾
    av_write_trailer(outFormatCtx);

    avformat_close_input(&videoFormatCtx);
    avformat_close_input(&audioFormatCtx);
    //关闭操作
    if (outFormatCtx && !(outFormatCtx->oformat->flags & AVFMT_NOFILE)) {
        avio_close(outFormatCtx->pb);
    }
    avformat_free_context(outFormatCtx);
    LOGE("merge mp4 success! outPath:%s", outputPath);
    return 0;
}


/**
 * 视频封住为 MP4
 * @param videoPath
 * @param audioPath
 * @param outputPath
 * @return
 */
int FFmpegMuxer::memoryTransform(const char *videoPath, const char *audioPath, const char *outputPath) {
    AVInputFormat *ptInputFormat = NULL;//The output container format.Muxing only, must be set by the caller before avformat_write_header().
    AVOutputFormat *ptOutputFormat = NULL;//The output container format.Muxing only, must be set by the caller before avformat_write_header().
    AVFormatContext *ptInFormatContext = NULL;//输入文件的封装格式上下文，内部包含所有的视频信息
    AVFormatContext *ptOutFormatContext = NULL;//输出文件的封装格式上下文，内部包含所有的视频信息
    AVPacket tOutPacket = {0};//存储一帧压缩编码数据给输出文件
    const char *strInVideoFileName = NULL, *strOutFileName = NULL;//输入文件名和输出文件名
    int iRet, i;
    int iVideoStreamIndex = -1;//视频流应该处在的位置
    int iFrameIndex = 0;
    long long llCurrentPts = 0;
    int iOutVideoStreamIndex = -1; //输出流中的视频流所在的位置
    AVStream *ptInStream = NULL, *ptOutStream = NULL;//输入音视频流和输出音视频流
    unsigned char *pbIoBuf = NULL;//io数据缓冲区
    AVIOContext *ptAVIO = NULL;//AVIOContext管理输入输出数据的结构体


    strInVideoFileName = videoPath;//Input file URL
    strOutFileName = outputPath;//Output file URL

    av_register_all();//注册FFmpeg所有组件

    /*------------Input:填充ptInFormatContext------------*/
    g_fileH264 = fopen(strInVideoFileName, "rb+");
    ptInFormatContext = avformat_alloc_context();
    pbIoBuf = (unsigned char *) av_malloc(IO_BUFFER_SIZE);
    //FillIoBuffer则是将数据读取至pbIoBuf的回调函数。FillIoBuffer()形式（参数，返回值）是固定的，是一个回调函数,
    ptAVIO = avio_alloc_context(pbIoBuf, IO_BUFFER_SIZE, 0, NULL, FillVideoIoBuffer, NULL,
                                NULL);  //当系统需要数据的时候，会自动调用该回调函数以获取数据
    ptInFormatContext->pb = ptAVIO; //当自行指定了AVIOContext之后，avformat_open_input()里面的URL参数就不起作用了

    ptInputFormat = av_find_input_format("h264");//得到ptInputFormat以便后面打开使用
    //ps:函数调用成功之后处理过的AVFormatContext结构体;file:打开的视音频流的文件路径或者流媒体URL;fmt:强制指定AVFormatContext中AVInputFormat的,为NULL,FFmpeg通过文件路径或者流媒体URL自动检测;dictionay:附加的一些选项,一般情况下可以设置为NULL
    //内部主要调用两个函数：init_input()：绝大部分初始化工作都是在这里做的。s->iformat->read_header()：读取多媒体数据文件头，根据视音频流创建相应的AVStream
    if ((iRet = avformat_open_input(&ptInFormatContext, "", ptInputFormat, NULL)) <
        0) //其中的init_input()如果指定了fmt(第三个参数,比如当前就有指定)就直接返回，如果没有指定就调用av_probe_input_buffer2()推测AVInputFormat
    {//打开输入视频源//自定义了回调函数FillIoBuffer()。在使用avformat_open_input()打开媒体数据的时候，就可以不指定文件的URL了，即其第2个参数为NULL（因为数据不是靠文件读取，而是由FillIoBuffer()提供）
        printf("Could not open input file\r\n");
    } else {
        if ((iRet = avformat_find_stream_info(ptInFormatContext, 0)) < 0) {//获取视频文件信息
            printf("Failed to find input stream information\r\n");
        } else {
            av_dump_format(ptInFormatContext, 0, strInVideoFileName, 0);//手工调试的函数,内部是log，输出相关的格式信息到log里面

            /*------------Output------------*/

            /*初始化一个用于输出的AVFormatContext结构体
             *ctx：函数调用成功之后创建的AVFormatContext结构体。
             *oformat：指定AVFormatContext中的AVOutputFormat，用于确定输出格式。如果指定为NULL，
              可以设定后两个参数（format_name或者filename）由FFmpeg猜测输出格式。
              PS：使用该参数需要自己手动获取AVOutputFormat，相对于使用后两个参数来说要麻烦一些。
             *format_name：指定输出格式的名称。根据格式名称，FFmpeg会推测输出格式。输出格式可以是“flv”，“mkv”等等。
             *filename：指定输出文件的名称。根据文件名称，FFmpeg会推测输出格式。文件名称可以是“xx.flv”，“yy.mkv”等等。
             函数执行成功的话，其返回值大于等于0
             */
            avformat_alloc_output_context2(&ptOutFormatContext, NULL, NULL, strOutFileName);
            if (!ptOutFormatContext) {
                printf("Could not create output context\r\n");
                iRet = AVERROR_UNKNOWN;
            } else {
                ptOutputFormat = ptOutFormatContext->oformat;
                //for (i = 0; i < ptInFormatContext->nb_streams; i++)
                {
                    //Create output AVStream according to input AVStream
                    ptInStream = ptInFormatContext->streams[0];//0 video
                    ptOutStream = avformat_new_stream(ptOutFormatContext,
                                                      ptInStream->codec->codec);//给ptOutFormatContext中的流数组streams中的
                    if (!ptOutStream) //一条流(数组中的元素)分配空间，也正是由于这里分配了空间,后续操作直接拷贝编码数据(pkt)就可以了。
                    {
                        printf("Failed allocating output stream\r\\n");
                        iRet = AVERROR_UNKNOWN;
                        //break;
                    } else {
                        iVideoStreamIndex = 0;
                        iOutVideoStreamIndex = ptOutStream->index; //保存视频流所在数组的位置
                        if (avcodec_copy_context(ptOutStream->codec, ptInStream->codec) <
                            0) //Copy the settings of AVCodecContext
                        {//avcodec_copy_context()函数可以将输入视频/音频的参数拷贝至输出视频/音频的AVCodecContext结构体
                            printf("Failed to copy context from input to output stream codec context\r\n");
                            iRet = AVERROR_UNKNOWN;
                            //break;
                        } else {
                            ptOutStream->codec->codec_tag = 0;
                            if (ptOutFormatContext->oformat->flags & AVFMT_GLOBALHEADER)
                                ptOutStream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;

                        }
                    }
                }
                if (AVERROR_UNKNOWN == iRet) {
                } else {
                    av_dump_format(ptOutFormatContext, 0, strOutFileName, 1);//Output information------------------
                    //Open output file
                    if (!(ptOutputFormat->flags & AVFMT_NOFILE)) {   /*打开FFmpeg的输入输出文件,使后续读写操作可以执行
                         *s：函数调用成功之后创建的AVIOContext结构体。
                         *url：输入输出协议的地址（文件也是一种“广义”的协议，对于文件来说就是文件的路径）。
                         *flags：打开地址的方式。可以选择只读，只写，或者读写。取值如下。
                                 AVIO_FLAG_READ：只读。AVIO_FLAG_WRITE：只写。AVIO_FLAG_READ_WRITE：读写。*/
                        iRet = avio_open(&ptOutFormatContext->pb, strOutFileName, AVIO_FLAG_WRITE);
                        if (iRet < 0) {
                            printf("Could not open output file %s\r\n", strOutFileName);
                        } else {
                            //Write file header
                            if (avformat_write_header(ptOutFormatContext, NULL) <
                                0) //avformat_write_header()中最关键的地方就是调用了AVOutputFormat的write_header()
                            {//不同的AVOutputFormat有不同的write_header()的实现方法
                                printf("Error occurred when opening output file\r\n");
                            } else {
                                while (1) {
                                    int iStreamIndex = -1;//用于标识当前是哪个流
                                    iStreamIndex = iOutVideoStreamIndex;
                                    //Get an AVPacket//从视频输入流中取出视频的AVPacket
                                    iRet = av_read_frame(ptInFormatContext, &tOutPacket);//从输入文件读取一帧压缩数据
                                    if (iRet < 0)
                                        break;
                                    else {
                                        do {
                                            ptInStream = ptInFormatContext->streams[tOutPacket.stream_index];
                                            ptOutStream = ptOutFormatContext->streams[iStreamIndex];
                                            if (tOutPacket.stream_index ==
                                                iVideoStreamIndex) { //H.264裸流没有PTS，因此必须手动写入PTS,应该放在av_read_frame()之后
                                                //FIX：No PTS (Example: Raw H.264)
                                                //Simple Write PTS
                                                if (tOutPacket.pts == AV_NOPTS_VALUE) {
                                                    //Write PTS
                                                    AVRational time_base1 = ptInStream->time_base;
                                                    //Duration between 2 frames (μs)     。假设25帧，两帧间隔40ms //AV_TIME_BASE表示1s，所以用它的单位为us，也就是ffmpeg中都是us
                                                    //int64_t calc_duration = AV_TIME_BASE*1/25;//或40*1000;//(double)AV_TIME_BASE / av_q2d(ptInStream->r_frame_rate);//ptInStream->r_frame_rate.den等于0所以注释掉
                                                    //帧率也可以从h264的流中获取,前面dump就有输出,但是不知道为何同样的变量前面r_frame_rate打印正常,这里使用的时候却不正常了，所以这个间隔时间只能使用avg_frame_rate或者根据假设帧率来写
                                                    int64_t calc_duration =
                                                            (double) AV_TIME_BASE / av_q2d(ptInStream->avg_frame_rate);
                                                    //Parameters    pts(显示时间戳)*pts单位(时间基*时间基单位)=真实显示的时间(所谓帧的显示时间都是相对第一帧来的)
                                                    tOutPacket.pts = (double) (iFrameIndex * calc_duration) /
                                                                     (double) (av_q2d(time_base1) *
                                                                               AV_TIME_BASE);//AV_TIME_BASE为1s，所以其单位为us
                                                    tOutPacket.dts = tOutPacket.pts;
                                                    tOutPacket.duration = (double) calc_duration /
                                                                          (double) (av_q2d(time_base1) * AV_TIME_BASE);
                                                    iFrameIndex++;
                                                    printf("Write iFrameIndex:%d,stream_index:%d,num:%d,den:%d\r\n",
                                                           iFrameIndex, tOutPacket.stream_index,
                                                           ptInStream->avg_frame_rate.num,
                                                           ptInStream->avg_frame_rate.den);
                                                }
                                                llCurrentPts = tOutPacket.pts;
                                                break;
                                            }
                                        } while (av_read_frame(ptInFormatContext, &tOutPacket) >= 0);
                                    }

                                    //Convert PTS/DTS
                                    tOutPacket.pts = av_rescale_q_rnd(tOutPacket.pts, ptInStream->time_base,
                                                                      ptOutStream->time_base,
                                                                      (AVRounding) (AV_ROUND_NEAR_INF |
                                                                                    AV_ROUND_PASS_MINMAX));
                                    tOutPacket.dts = av_rescale_q_rnd(tOutPacket.dts, ptInStream->time_base,
                                                                      ptOutStream->time_base,
                                                                      (AVRounding) (AV_ROUND_NEAR_INF |
                                                                                    AV_ROUND_PASS_MINMAX));
                                    tOutPacket.duration = av_rescale_q(tOutPacket.duration, ptInStream->time_base,
                                                                       ptOutStream->time_base);
                                    tOutPacket.pos = -1;
                                    tOutPacket.stream_index = iStreamIndex;
                                    //printf("Write 1 Packet. size:%5d\tpts:%lld\n", tOutPacket.size, tOutPacket.pts);
                                    //Write
                                    /*av_interleaved_write_frame包括interleave_packet()以及write_packet()，将还未输出的AVPacket输出出来
                                     *write_packet()函数最关键的地方就是调用了AVOutputFormat中写入数据的方法。write_packet()实际上是一个函数指针，
                                     指向特定的AVOutputFormat中的实现函数*/
                                    if (av_interleaved_write_frame(ptOutFormatContext, &tOutPacket) < 0) {
                                        printf("Error muxing packet\r\n");
                                        break;
                                    }
                                    av_free_packet(&tOutPacket);//释放空间
                                }
                                //Write file trailer//av_write_trailer()中最关键的地方就是调用了AVOutputFormat的write_trailer()
                                av_write_trailer(ptOutFormatContext);//不同的AVOutputFormat有不同的write_trailer()的实现方法
                            }
                            if (ptOutFormatContext && !(ptOutputFormat->flags & AVFMT_NOFILE))
                                avio_close(
                                        ptOutFormatContext->pb);//该函数用于关闭一个AVFormatContext->pb，一般情况下是和avio_open()成对使用的。
                        }
                    }
                }
                avformat_free_context(ptOutFormatContext);//释放空间
            }
        }
        avformat_close_input(&ptInFormatContext);//该函数用于关闭一个AVFormatContext，一般情况下是和avformat_open_input()成对使用的。
    }
    if (NULL != g_fileH264)
        fclose(g_fileH264);
    LOGE("退出成功");
    return 0;
}


