//
// Created by 阳坤 on 2020-05-23.
//

#include "AVShader.h"


//顶点着色器glsl
/**
 * 是针对每个顶点执行一次，用于确定顶点的位置
 * 创建 2 个三角形，形成一个矩形。
 */
#define GET_STR(x) #x /* #x 直接把传入的 X 转换为字符串 直接参考 ijkplayer*/
static const char *vertexShader = GET_STR(
        attribute
        vec4 aPosition; //顶点坐标
        attribute
        vec2 aTexCoord; //材质顶点坐标
        varying
        vec2 vTexCoord;   //输出的材质坐标
        void main() {
            vTexCoord = vec2(aTexCoord.x, 1.0 - aTexCoord.y);//0,0
            gl_Position = aPosition;
        }
);

//片元着色器,软解码和部分x86硬解码
/**
 * 是针对每个片元(每个像素点)执行一次，用于确定每个片元像素的颜色
 * 显卡中调用，性能比较高。
 */
static const char *fragYUV420P = GET_STR(
        precision
        mediump float;    //精度
        varying
        vec2 vTexCoord;     //顶点着色器传递的坐标
        uniform
        sampler2D yTexture; //输入的材质（不透明灰度，单像素）
        uniform
        sampler2D uTexture;
        uniform
        sampler2D vTexture;
        void main() {
            vec3 yuv;
            vec3 rgb;
            yuv.r = texture2D(yTexture, vTexCoord).r;
            yuv.g = texture2D(uTexture, vTexCoord).r - 0.5;
            yuv.b = texture2D(vTexture, vTexCoord).r - 0.5;
            rgb = mat3(1.0, 1.0, 1.0,
                       0.0, -0.39465, 2.03211,
                       1.13983, -0.58060, 0.0) * yuv;
            //输出像素颜色
            gl_FragColor = vec4(rgb, 1.0);
        }
);


//片元着色器,软解码和部分x86硬解码
static const char *fragNV12 = GET_STR(
        precision
        mediump float;    //精度
        varying
        vec2 vTexCoord;     //顶点着色器传递的坐标
        uniform
        sampler2D yTexture; //输入的材质（不透明灰度，单像素）
        uniform
        sampler2D uvTexture;
        void main() {
            vec3 yuv;
            vec3 rgb;
            yuv.r = texture2D(yTexture, vTexCoord).r;
            yuv.g = texture2D(uvTexture, vTexCoord).r - 0.5;
            yuv.b = texture2D(uvTexture, vTexCoord).a - 0.5;
            rgb = mat3(1.0, 1.0, 1.0,
                       0.0, -0.39465, 2.03211,
                       1.13983, -0.58060, 0.0) * yuv;
            //输出像素颜色
            gl_FragColor = vec4(rgb, 1.0);
        }
);

//片元着色器,软解码和部分x86硬解码
static const char *fragNV21 = GET_STR(
        precision
        mediump float;    //精度
        varying
        vec2 vTexCoord;     //顶点着色器传递的坐标
        uniform
        sampler2D yTexture; //输入的材质（不透明灰度，单像素）
        uniform
        sampler2D uvTexture;
        void main() {
            vec3 yuv;
            vec3 rgb;
            yuv.r = texture2D(yTexture, vTexCoord).r;
            yuv.g = texture2D(uvTexture, vTexCoord).a - 0.5;
            yuv.b = texture2D(uvTexture, vTexCoord).r - 0.5;
            rgb = mat3(1.0, 1.0, 1.0,
                       0.0, -0.39465, 2.03211,
                       1.13983, -0.58060, 0.0) * yuv;
            //输出像素颜色
            gl_FragColor = vec4(rgb, 1.0);
        }
);

bool AVShader::init(AVShaderType type) {
    close();
    //顶点和片元shader初始化
    //顶点shader初始化
    mux.lock();
    vsh = initShader(vertexShader, GL_VERTEX_SHADER);
    if (vsh == 0) {
        mux.unlock();
        LOGE("initShader GL_VERTEX_SHADER failed!");
        return false;
    }

    LOGE("initShader GL_VERTEX_SHADER success! %d", type);

    //片元yuv420 shader初始化
    switch (type) {
        case AVSHADER_YUV420P:
            fsh = initShader(fragYUV420P, GL_FRAGMENT_SHADER);
            break;
        case AVSHADER_NV12:
            fsh = initShader(fragNV12, GL_FRAGMENT_SHADER);
            break;
        case AVSHADER_NV21:
            fsh = initShader(fragNV21, GL_FRAGMENT_SHADER);
            break;
        default:
            mux.unlock();
            LOGE("AVSHADER format is error");
            return false;
    }

    if (fsh == 0) {
        mux.unlock();
        LOGE("initShader GL_FRAGMENT_SHADER failed!");
        return false;
    }
    LOGE("initShader GL_FRAGMENT_SHADER success!");


    /////////////////////////////////////////////////////////////
    //创建渲染程序
    program = glCreateProgram();
    if (program == 0) {
        mux.unlock();
        LOGE("glCreateProgram failed!");
        return false;
    }
    //渲染程序中加入着色器代码
    glAttachShader(program, vsh); //顶点
    glAttachShader(program, fsh); //片元

    //链接程序
    glLinkProgram(program);
    GLint status = 0;
    glGetProgramiv(program, GL_LINK_STATUS, &status);
    if (status != GL_TRUE) {
        mux.unlock();
        LOGE("glLinkProgram failed!");
        return false;
    }
    glUseProgram(program);
    LOGE("glLinkProgram success!");
    /////////////////////////////////////////////////////////////


    //加入三维顶点数据 两个三角形组成正方形
    static float vers[] = {
            1.0f, -1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f,
    };
    GLuint apos = (GLuint) glGetAttribLocation(program, "aPosition");//获取 shader 顶点坐标
    glEnableVertexAttribArray(apos);
    //传递顶点
    glVertexAttribPointer(apos, 3/*x,y,z*/, GL_FLOAT, GL_FALSE, 12/*4byte * 3*/, vers);

    //加入材质坐标数据
    static float txts[] = {
            1.0f, 0.0f, //右下
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0, 1.0
    };

    GLuint atex = (GLuint) glGetAttribLocation(program, "aTexCoord");
    glEnableVertexAttribArray(atex);
    glVertexAttribPointer(atex, 2, GL_FLOAT, GL_FALSE, 8, txts);

    //材质纹理初始化
    //设置纹理层
    glUniform1i(glGetUniformLocation(program, "yTexture"), 0); //对于纹理第1层

    switch (type) {
        case AVSHADER_YUV420P:
            glUniform1i(glGetUniformLocation(program, "uTexture"), 1); //对于纹理第2层
            glUniform1i(glGetUniformLocation(program, "vTexture"), 2); //对于纹理第3层
            break;
        case AVSHADER_NV21:
        case AVSHADER_NV12:
            glUniform1i(glGetUniformLocation(program, "uvTexture"), 1); //对于纹理第2层
            break;
    }

    mux.unlock();
    LOGI("初始化Shader成功！");
    return true;
}

void AVShader::close() {
    mux.lock();
    //释放shader
    if (program)
        glDeleteProgram(program);
    if (fsh)
        glDeleteShader(fsh);
    if (vsh)
        glDeleteShader(vsh);

    //释放材质
    for (int i = 0; i < sizeof(texts) / sizeof(unsigned int); i++) {
        if (texts[i]) {
            glDeleteTextures(1, &texts[i]);
        }
        texts[i] = 0;
    }
    mux.unlock();
}

void AVShader::getTexture(unsigned int index, int width, int height, unsigned char *buf, bool isa) {
    unsigned int format = GL_LUMINANCE;
    if (isa)//带透明通道
        format = GL_LUMINANCE_ALPHA;

    mux.lock();
    if (texts[index] == 0) {
        //材质初始化
        glGenTextures(1, &texts[index]);

        //设置纹理属性
        glBindTexture(GL_TEXTURE_2D, texts[index]);
        //缩小的过滤器
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        //设置纹理的格式和大小
        glTexImage2D(GL_TEXTURE_2D,
                     0,           //细节基本 0默认
                     format,//gpu内部格式 亮度，灰度图
                     width, height, //拉升到全屏
                     0,             //边框
                     format,//数据的像素格式 亮度，灰度图 要与上面一致
                     GL_UNSIGNED_BYTE, //像素的数据类型
                     NULL              //纹理的数据
        );
    }

    //激活第1层纹理,绑定到创建的opengl纹理
    glActiveTexture(GL_TEXTURE0 + index);
    glBindTexture(GL_TEXTURE_2D, texts[index]);
    //替换纹理内容
    glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, format, GL_UNSIGNED_BYTE, buf);
    mux.unlock();
}

void AVShader::draw() {
    mux.lock();
    if (!program) {
        mux.unlock();
        return;
    }
    //三维绘制
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    mux.unlock();
}

GLuint AVShader::initShader(const char *code, GLint type) {
    //1. 创建shader
    GLuint sh = glCreateShader(type);
    if (sh == 0) {
        LOGE("glCreateShader %d failed!", type);
        return 0;
    }
    //2. 加载shader
    glShaderSource(sh,
                   1,    //shader数量
                   &code, //shader代码
                   0);   //代码长度
    //3. 编译shader
    glCompileShader(sh);

    //4. 获取编译情况
    GLint status;
    glGetShaderiv(sh, GL_COMPILE_STATUS, &status);
    if (status == 0) {
        LOGE("glCompileShader failed!");
        return 0;
    }
    LOGE("glCompileShader success!");
    return sh;
}
