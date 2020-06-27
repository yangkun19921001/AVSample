package com.devyk.javaavplayer.gles

import android.opengl.GLES20
import android.view.SurfaceHolder
import android.os.Build
import android.content.Context
import android.opengl.GLSurfaceView
import android.graphics.PixelFormat
import android.util.AttributeSet
import com.devyk.common.LogHelper
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * <pre>
 *     author  : devyk on 2020-06-27 23:26
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is VideoConsumerGLPreview
 * </pre>
 */
class VideoConsumerGLPreview : GLSurfaceView, GLSurfaceView.Renderer {

    internal var mBufferWidthY: Int = 0
    internal var mBufferHeightY: Int = 0
    internal var mBufferWidthUV: Int = 0
    internal var mBufferHeightUV: Int = 0
    internal var mBuffer: ByteBuffer? = null
    internal var mBufferPositionY: Int = 0
    internal var mBufferPositionU: Int = 0
    internal var mBufferPositionV: Int = 0
    private var IS_RequestRender = false

    private var mTriangleVertices: FloatBuffer? = null
    private var mIndices: ShortBuffer? = null

    private var mProgram: Int = 0
    private var maPositionHandle: Int = 0
    private var maTextureHandle: Int = 0
    private var muSamplerYHandle: Int = 0
    private var muSamplerUHandle: Int = 0
    private var muSamplerVHandle: Int = 0
    private val mTextureY = IntArray(1)
    private val mTextureU = IntArray(1)
    private val mTextureV = IntArray(1)

    private var mSurfaceCreated: Boolean = false
    val isDestroyed: Boolean = false
    private var mContext: Context? = null

    private var mViewWidth: Int = 0
    private var mViewHeight: Int = 0
    private var mViewX: Int = 0
    private var mViewY: Int = 0
    private var mFullScreenRequired: Boolean = false
    private val TAG = this.javaClass.simpleName


    /**
     * Y 分量
     */
    private val yByteBuffer = ByteBuffer.allocateDirect(1280 * 720)

    /**
     * uv 分量
     */
    private val uvByteBuffer = ByteBuffer.allocateDirect(1280 * 720 shr 1)

    val isReady: Boolean
        get() = mSurfaceCreated && !isDestroyed

    /**
     * @param context
     * @param fullScreenRequired 是否全屏
     * @param buffer             传入的 buffer
     * @param bufferWidth        宽
     * @param bufferHeight       高
     */


    constructor(
        context: Context,
        fullScreenRequired: Boolean,
        buffer: ByteBuffer?,
        bufferWidth: Int,
        bufferHeight: Int
    ) : super(context) {
        init(context, fullScreenRequired, buffer, bufferWidth, bufferHeight)
    }


    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    private fun init(
        context: Context,
        fullScreenRequired: Boolean,
        buffer: ByteBuffer?,
        bufferWidth: Int,
        bufferHeight: Int
    ) {
        setEGLContextClientVersion(2)
        //设置默认颜色
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(this)
        holder.setFormat(PixelFormat.TRANSLUCENT)
        holder.setType(SurfaceHolder.SURFACE_TYPE_GPU)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        setBuffer(buffer, bufferWidth, bufferHeight)

        mContext = context

        mTriangleVertices =
            ByteBuffer.allocateDirect(TRIANFLE_VERTICES_DATA.size * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        mTriangleVertices!!.put(TRIANFLE_VERTICES_DATA).position(0)

        mIndices = ByteBuffer.allocateDirect(INDICES_DATA.size * SHORT_SIZE_BYTES).order(ByteOrder.nativeOrder())
            .asShortBuffer()
        mIndices!!.put(INDICES_DATA).position(0)

        mFullScreenRequired = fullScreenRequired
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            preserveEGLContextOnPause = true//如果没有这一句，那onPause之后再onResume屏幕将会是黑屏滴
        }
    }


    fun setBuffer(buffer: ByteBuffer?, bufferWidth: Int, bufferHeight: Int) {

        mBuffer = buffer
        mBufferWidthY = bufferWidth
        mBufferHeightY = bufferHeight

        mBufferWidthUV = mBufferWidthY shr 1
        mBufferHeightUV = mBufferHeightY shr 1

        mBufferPositionY = 0
        mBufferPositionU = mBufferWidthY * mBufferHeightY
        mBufferPositionV = mBufferPositionU + mBufferWidthUV * mBufferHeightUV


    }



    override fun surfaceDestroyed(holder: SurfaceHolder) {
        //            mSurfaceCreated = false;
        //            mSurfaceDestroyed = true;
        super.surfaceDestroyed(holder)
    }

    override fun onDrawFrame(glUnused: GL10) {
        GLES20.glViewport(mViewX, mViewY, mViewWidth, mViewHeight)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        checkGlError("glUseProgram")

        if (mBuffer != null) {
            synchronized(this) {
                //y
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0])
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_LUMINANCE,
                    mBufferWidthY,
                    mBufferHeightY,
                    0,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    mBuffer!!.position(mBufferPositionY)
                )
                GLES20.glUniform1i(muSamplerYHandle, 0)

                //u
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureU[0])
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_LUMINANCE,
                    mBufferWidthUV,
                    mBufferHeightUV,
                    0,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    mBuffer!!.position(mBufferPositionU)
                )
                GLES20.glUniform1i(muSamplerUHandle, 1)

                //v
                GLES20.glActiveTexture(GLES20.GL_TEXTURE2)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureV[0])
                GLES20.glTexImage2D(
                    GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_LUMINANCE,
                    mBufferWidthUV,
                    mBufferHeightUV,
                    0,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    mBuffer!!.position(mBufferPositionV)
                )
                GLES20.glUniform1i(muSamplerVHandle, 2)
            }
        }


        if (IS_RequestRender)
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, INDICES_DATA.size, GLES20.GL_UNSIGNED_SHORT, mIndices)

    }


    override fun onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        setViewport(width, height)
        // GLU.gluPerspective(glUnused, 45.0f, (float)width/(float)height, 0.1f, 100.0f);
    }


    override fun onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDisable(GLES20.GL_DITHER)
        GLES20.glDisable(GLES20.GL_STENCIL_TEST)
        GLES20.glDisable(GL10.GL_DITHER)

        val extensions = GLES20.glGetString(GL10.GL_EXTENSIONS)
        LogHelper.d("OPGL", "OpenGL extensions=$extensions")

        // Ignore the passed-in GL10 interface, and use the GLES20
        // class's static methods instead.
        mProgram = createProgram(VERTEX_SHADER_SOURCE, FRAGMENT_SHADER_SOURCE)
        if (mProgram == 0) {
            return
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (maPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord")
        checkGlError("glGetAttribLocation aTextureCoord")
        if (maTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }

        muSamplerYHandle = GLES20.glGetUniformLocation(mProgram, "SamplerY")
        if (muSamplerYHandle == -1) {
            throw RuntimeException("Could not get uniform location for SamplerY")
        }
        muSamplerUHandle = GLES20.glGetUniformLocation(mProgram, "SamplerU")
        if (muSamplerUHandle == -1) {
            throw RuntimeException("Could not get uniform location for SamplerU")
        }
        muSamplerVHandle = GLES20.glGetUniformLocation(mProgram, "SamplerV")
        if (muSamplerVHandle == -1) {
            throw RuntimeException("Could not get uniform location for SamplerV")
        }

        mTriangleVertices!!.position(TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            maPositionHandle,
            3,
            GLES20.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maPosition")

        mTriangleVertices!!.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glEnableVertexAttribArray(maPositionHandle)
        checkGlError("glEnableVertexAttribArray maPositionHandle")
        GLES20.glVertexAttribPointer(
            maTextureHandle,
            2,
            GLES20.GL_FLOAT,
            false,
            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
            mTriangleVertices
        )
        checkGlError("glVertexAttribPointer maTextureHandle")
        GLES20.glEnableVertexAttribArray(maTextureHandle)
        checkGlError("glEnableVertexAttribArray maTextureHandle")

        GLES20.glGenTextures(1, mTextureY, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureY[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLES20.glGenTextures(1, mTextureU, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureU[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        GLES20.glGenTextures(1, mTextureV, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureV[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        mSurfaceCreated = true

        setViewport(width, height)

    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                LogHelper.e(TAG, "Could not compile shader $shaderType:")
                LogHelper.e(TAG, GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }

        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }

        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES20.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                LogHelper.e(TAG, "Could not link program: ")
                LogHelper.e(TAG, GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    private fun setViewport(width: Int, height: Int) {
        if (mFullScreenRequired) {
            mViewWidth = width
            mViewHeight = height
            mViewY = 0
            mViewX = mViewY
        } else {
            val fRatio = mBufferWidthY.toFloat() / mBufferHeightY.toFloat()
            mViewWidth = if ((width.toFloat() / fRatio).toInt() > height) (height.toFloat() * fRatio).toInt() else width
            mViewHeight = if ((mViewWidth / fRatio).toInt() > height) height else (mViewWidth / fRatio).toInt()
            mViewX = width - mViewWidth shr 1
            mViewY = height - mViewHeight shr 1
        }
    }

    private fun checkGlError(op: String) {
        val glGetError = GLES20.glGetError()
        while (glGetError != GLES20.GL_NO_ERROR) {
            LogHelper.e(TAG, "$op: glError $glGetError")
            throw RuntimeException("$op: glError $glGetError")
        }
    }

    fun setIsRequestRender(
        isRequestRender: Boolean
    ) {
        IS_RequestRender = isRequestRender
    }

    companion object {

        private val FLOAT_SIZE_BYTES = 4
        private val SHORT_SIZE_BYTES = 2
        private val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
        private val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
        private val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3

        private val TRIANFLE_VERTICES_DATA =
            floatArrayOf(1f, -1f, 0f, 1f, 1f, 1f, 1f, 0f, 1f, 0f, -1f, 1f, 0f, 0f, 0f, -1f, -1f, 0f, 0f, 1f)
        private val INDICES_DATA = shortArrayOf(0, 1, 2, 2, 3, 0)

        private val VERTEX_SHADER_SOURCE = "attribute vec4 aPosition;\n" +
                "attribute vec2 aTextureCoord;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main() {\n" +
                "  gl_Position = aPosition;\n" +
                "  vTextureCoord = aTextureCoord;\n" +
                "}\n"

        private val FRAGMENT_SHADER_SOURCE = "precision mediump float;" +
                "varying vec2 vTextureCoord;" +
                "" +
                "uniform sampler2D SamplerY; " +
                "uniform sampler2D SamplerU;" +
                "uniform sampler2D SamplerV;" +
                "" +
                "const mat3 yuv2rgb = mat3(1, 0, 1.2802,1, -0.214821, -0.380589,1, 2.127982, 0);" +
                "" +
                "void main() {    " +
                "    vec3 yuv = vec3(1.1643 * (texture2D(SamplerY, vTextureCoord).r - 0.0625)," +
                "                    texture2D(SamplerU, vTextureCoord).r - 0.5," +
                "                    texture2D(SamplerV, vTextureCoord).r - 0.5);" +
                "    vec3 rgb = yuv * yuv2rgb;    " +
                "    gl_FragColor = vec4(rgb, 1.0);" +
                "} "
    }
}