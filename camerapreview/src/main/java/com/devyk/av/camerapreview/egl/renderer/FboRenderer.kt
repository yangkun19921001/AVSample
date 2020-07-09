package com.devyk.av.camerapreview.egl.renderer

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import com.devyk.av.camerapreview.R
import com.devyk.av.camerapreview.callback.IRenderer
import com.devyk.av.camerapreview.egl.ShaderHelper
import com.devyk.av.camerapreview.utils.BitmapUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * <pre>
 *     author  : devyk on 2020-07-07 10:55
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FboRenderer
 * </pre>
 *
 * FBO 概念：
 * 为什么要用FBO?
 * - 当我们需要对纹理进行多次渲染采样时，而这些渲染采样是不需要展示给用户看的，所以我们就可以用一个单独的缓冲对象（离屏渲染）来存储我们的这几次渲染采样的结果，等处理完后才显示到窗口上。
 *
 * 优势
 * - 提高渲染效率，避免闪屏，可以很方便的实现纹理共享等。
 *
 *
 *
 */
public class FboRenderer(context: Context) : IRenderer {

    private var TAG = this.javaClass.simpleName

    /**
     * 顶点坐标
     */
    private val mVertexData: FloatArray = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f,

        0f, 0f,
        0f, 0f,
        0f, 0f,
        0f, 0f
    )

    /**
     * 纹理坐标
     */
    private val mFragmentData: FloatArray = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
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
     *纹理采样器 （获取对应的纹理ID）
     */
    private var sampler = 0

    /**
     * 为顶点坐标分配 native 地址空间
     */
    private lateinit var mVertexBuffer: FloatBuffer
    /**
     * 为片元坐标分配 native 地址空间
     */
    private lateinit var mFragmentBuffer: FloatBuffer

    private lateinit var mBitmap: Bitmap
    private var mBitmapTextureId = 0

    init {

        mBitmap = BitmapUtils.creatBitmap("我是水印",context,20, "#ff0000", "#00000000")

        var r = 1.0f * mBitmap.getWidth() / mBitmap.getHeight();
        var w = r * 0.1f;

        mVertexData[8] = 0.8f - w;
        mVertexData[9] = -0.8f;

        mVertexData[10] = 0.8f;
        mVertexData[11] = -0.8f;

        mVertexData[12] = 0.8f - w;
        mVertexData[13] = -0.7f;

        mVertexData[14] = 0.8f;
        mVertexData[15] = -0.7f;


        mContext = context
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
        //1. 获取顶点/片元源代码资源
        var vertexSource = ShaderHelper.getRawShaderResource(mContext, R.raw.vertex_shader)
        var fragmentSource = ShaderHelper.getRawShaderResource(mContext, R.raw.fragment_shader)

        //2. 为 顶点和片元创建一个执行程序
        program = ShaderHelper.createProgram(vertexSource, fragmentSource)

        //3. 拿到顶点/片元 源代码中的索引位置
        vPosition = GLES20.glGetAttribLocation(program, "v_Position")
        fPosition = GLES20.glGetAttribLocation(program, "f_Position")
        sampler = GLES20.glGetAttribLocation(program, "sTexture")

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

        mBitmapTextureId = ShaderHelper.loadBitmapTexture(mBitmap);
    }

    override fun onSurfaceChange(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDraw() {
    }

    public fun onDraw(textureId: Int) {
        //相当于清屏
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        //1. 使用顶点和片元创建出来的执行程序
        GLES20.glUseProgram(program)

        //2. 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

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

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBitmapTextureId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8,
            32);

        GLES20.glEnableVertexAttribArray(fPosition);
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8,
            mVertexData.size * 4)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //7. 解绑
        // 解绑 纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        // 解绑 VBO
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }
}