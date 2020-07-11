package com.devyk.av.camera_recorder.egl.renderer

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.devyk.av.camera_recorder.R
import com.devyk.av.camera_recorder.callback.IRenderer
import com.devyk.av.camera_recorder.egl.ShaderHelper
import com.devyk.common.LogHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


/**
 * <pre>
 *     author  : devyk on 2020-07-06 17:45
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BitmapRenderer
 * </pre>
 */
public class BitmapRenderer(context: Context) : IRenderer {

    private var TAG = this.javaClass.simpleName

    /**
     * 顶点坐标
     */
    private val mVertexData: FloatArray = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f
    )

    /**
     *  FBO 纹理坐标
     */
    private val mFragmentData: FloatArray = floatArrayOf(
        0f, 0f,
        1f, 0f,
        0f, 1f,
        1f, 1f

        //正常纹理坐标
//        0f, 1f,
//        1f, 1f,
//        0f, 0f,
//        1f, 0f
    )

    /**
     * 定义一个上下文变量
     */
    private lateinit var mContext: Context

    /**
     * 执行着色器代码的程序
     */
    private var program = 0

    /**
     * 顶点索引
     */
    private var vPosition = 0
    /**
     * 纹理索引
     */
    private var fPosition = 0

    /**
     * 绘制的纹理 ID
     */
    private var mTextureID = 0

    /**
     * 使用 VBO
     * 概念:
     * - 不使用VBO时，我们每次绘制（ glDrawArrays ）图形时都是从本地内存处获取顶点数据然后传输给OpenGL来绘制，这样就会频繁的操作CPU->GPU增大开销，从而降低效率。
     * - 使用VBO，我们就能把顶点数据缓存到GPU开辟的一段内存中，然后使用时不必再从本地获取，而是直接从显存中获取，这样就能提升绘制的效率。
     *
     *
     */
    private var mVboID = 0;

    /**
     *  FBO 概念：
     * 为什么要用FBO?
     * - 当我们需要对纹理进行多次渲染采样时，而这些渲染采样是不需要展示给用户看的，所以我们就可以用一个单独的缓冲对象（离屏渲染）来存储我们的这几次渲染采样的结果，等处理完后才显示到窗口上。
     *
     * 优势
     * - 提高渲染效率，避免闪屏，可以很方便的实现纹理共享等。
     */
    private var mFboID = 0

    /**
     *纹理采样器 （获取对应的纹理ID）
     */
    private var sampler = 0
    /**
     * 矩阵值:正交投影
     */
    private var umatrix = 0

    /**
     * 为顶点坐标分配 native 地址空间
     */
    private lateinit var mVertexBuffer: FloatBuffer
    /**
     * 为片元坐标分配 native 地址空间
     */
    private lateinit var mFragmentBuffer: FloatBuffer

    /**
     * fbo renderer
     */
    private lateinit var mFboRenderer: FboRenderer

    /**
     * bitmap 纹理 ID
     */
    private var mImageTextureId = 0;

    /**
     * 设置 fbo 离屏渲染的 size
     */
    private var mWidth = 1080
    private var mHeight = 1920

    /**
     * 设置 fbo 离屏渲染的 size
     */
    private var mBitmapWidth = 0f
    private var mbitmapHeight = 0f

    /**
     * 做矩阵方向变换的
     */
    private val mMatrix = FloatArray(16)

    private var mRendererListener: OnRendererListener? = null


    private var mImagePath = ""

    init {
        mContext = context
        mFboRenderer = FboRenderer(mContext)
        mVertexBuffer = ByteBuffer.allocateDirect(mVertexData.size * 4)
            .order(ByteOrder.nativeOrder()) //大内存在前面，字节对齐
            .asFloatBuffer()
            .put(mVertexData)
        //指向第一个索引，相当于 C 里面的第一个内存地址
        mVertexBuffer.position(0)

        mFragmentBuffer = ByteBuffer.allocateDirect(mFragmentData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(mFragmentData)
        mFragmentBuffer.position(0)


    }


    override fun onSurfaceCreate(width: Int, height: Int) {
        this.mHeight = height
        this.mWidth = width

        mFboRenderer.onSurfaceCreate(width, height)

        //1. 获取顶点/片元源代码资源
        var vertexSource = ShaderHelper.getRawShaderResource(mContext, R.raw.vertex_shader_matrix)
        var fragmentSource = ShaderHelper.getRawShaderResource(mContext, R.raw.fragment_shader)

        //2. 为 顶点和片元创建一个执行程序
        program = ShaderHelper.createProgram(vertexSource, fragmentSource)

        //3. 拿到顶点/片元 源代码中的索引位置
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        fPosition = GLES20.glGetAttribLocation(program, "f_Position")
        sampler = GLES20.glGetAttribLocation(program, "sTexture")
        umatrix = GLES20.glGetUniformLocation(program, "u_Matrix");

        //4. 生成一个 VBO
        var vbo = IntArray(1)
        GLES20.glGenBuffers(1, vbo, 0);
        mVboID = vbo[0]
        //4.1 绑定 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboID)
        //4.2 分配 VBO 需要的缓存大小
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            mVertexData.size * 4 + mFragmentData.size * 4,
            null,
            GLES20.GL_STATIC_DRAW
        );
        //4.3 为 VBO 设置顶点、片元数据的值
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, mVertexData.size * 4, mVertexBuffer);
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, mVertexData.size * 4, mFragmentData.size * 4, mFragmentBuffer);
        //4.4 解绑 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        //5. 生成一个 FBO
        var fbo = IntArray(1)
        GLES20.glGenFramebuffers(1, fbo, 0)
        mFboID = fbo[0]
        //5.1 绑定 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboID)


        //6. 生成一个纹理 ID
        var textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        mTextureID = textureIds[0]

        //6.1. 指定将要绘制的纹理对象并且传递给对应的片元着色器中
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID)
        GLES20.glUniform1i(sampler, 0)

        //6.2. 设置环绕和过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        //5.2 设置 FBO 分配的内存大小
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            mWidth,
            mHeight,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )

        //5.3 把纹理绑定到 FBO 上
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            mTextureID,
            0
        );


        //5.4 检查 FBO 是否绑定成功
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            LogHelper.e(TAG, "fbo bind error");
        } else {
            LogHelper.e(TAG, "fbo bind success");
        }

        //5.5 解绑 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //解绑纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

//        //7.绘制一张图片
        mImageTextureId = loadTexrute(R.drawable.huge)

        mRendererListener?.onCreate(mTextureID)

    }

    override fun onSurfaceChange(width: Int, height: Int) {
        //指定渲染框口的大小
        GLES20.glViewport(0, 0, width, height)
        mFboRenderer.onSurfaceChange(width, height)

        this.mHeight = height
        this.mWidth = width

        //正交投影
        // 说明是横屏
        if (width > height) {
            Matrix.orthoM(
                mMatrix,
                0,
                -width / ((height / mbitmapHeight) * mBitmapWidth),
                width / ((height / mbitmapHeight) * mBitmapWidth),
                -1f,
                1f,
                -1f,
                1f
            );
        } else {
            Matrix.orthoM(
                mMatrix,
                0,
                -1f,
                1f,
                -height / ((width / mBitmapWidth) * mbitmapHeight),
                height / ((width / mBitmapWidth) * mbitmapHeight),
                -1f,
                1f
            );
        }
    }

    public fun setRotateM(rotate: Float) {
        //矩阵旋转
        Matrix.rotateM(mMatrix, 0, rotate, 1f, 0f, 0f);

    }

    override fun onDraw() {

        //7.绘制一张图片
        //绑定 fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFboID);

        //相当于清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        //1. 使用顶点和片元创建出来的执行程序
        GLES20.glUseProgram(program)
        //使用矩阵
        GLES20.glUniformMatrix4fv(umatrix, 1, false, mMatrix, 0);

        //2. 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mImageTextureId)

        //3. 绑定 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboID);


        //4. 设置顶点坐标
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)

        //5. 设置纹理坐标
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, mVertexData.size * 4)

        //6. 开始绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        //7. 解绑
        // 解绑 纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        // 解绑 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        //解绑 fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //绘制纹理
        mFboRenderer.onDraw(mTextureID)
    }

    public fun loadTexrute(src: Int): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        var bitmap = BitmapFactory.decodeResource(mContext.getResources(), src)
        mBitmapWidth = bitmap.width.toFloat()
        mbitmapHeight = bitmap.height.toFloat()
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        bitmap.recycle()
        bitmap = null
        return textureIds[0]

    }





    public interface OnRendererListener {
        fun onCreate(textureId: Int)
    }

    public fun setOnRendererListener(listener: OnRendererListener) {
        this.mRendererListener = listener
    }
}