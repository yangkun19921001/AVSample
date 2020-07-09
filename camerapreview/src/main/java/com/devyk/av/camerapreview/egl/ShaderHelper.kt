package com.devyk.av.camerapreview.egl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.opengl.GLES20
import com.blankj.utilcode.util.StringUtils
import com.devyk.common.LogHelper
import java.io.*
import java.nio.ByteBuffer


/**
 * <pre>
 *     author  : devyk on 2020-07-06 16:48
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is ShaderHelper 对 shader 加载的一些操作帮助类
 * </pre>
 */
public object ShaderHelper {

    private var TAG = this.javaClass.simpleName


    /**
     * 从资源文件中加载着色器源代码
     */
    fun getRawShaderResource(context: Context?, id: Int): String {
        context?.let { ctx ->
            val inputStream = ctx.resources.openRawResource(id)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var sb = StringBuffer()
            var line: String? = null

            while (true) {
                try {
                    line = bufferedReader.readLine()
                    if (StringUtils.isEmpty(line)) {
                        bufferedReader.close()
                        return sb.toString()
                    }
                    sb.append(line).append("\n")
                } catch (error: IOException) {
                    LogHelper.e(TAG, error.message)
                }
            }

        }
        return ""
    }

    /**
     * 加载 着色器 代码
     */
    @Synchronized
    private fun loadShader(shaderType: Int, source: String): Int {
        //1、创建着色器
        var shader = GLES20.glCreateShader(shaderType)
        if (shader == 0) return -1
        //2、加载着色器源码
        GLES20.glShaderSource(shader, source)
        //3. 编译着色器
        GLES20.glCompileShader(shader)
        //4. 检查是否编译成功
        val compile = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compile, 0)
        if (compile[0] != GLES20.GL_TRUE) {
            LogHelper.e(TAG, "shader compile error");
            GLES20.glDeleteShader(shader);
            shader = -1;
        }
        return shader;
    }

    /**
     * 创建一个 着色器 的执行程序代码
     */
    fun createProgram(vertecSource: String, frameSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertecSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, frameSource)
        if (vertexShader == -1 || fragmentShader == -1) return -1
        //1. 创建一个渲染程序
        val program = GLES20.glCreateProgram()
        //2. 将着色器程序添加到渲染程序中
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        //3. 链接程序
        GLES20.glLinkProgram(program)
        return program
    }


    fun createTextImage(text: String, textSize: Int, textColor: String, bgColor: String?, padding: Int): Bitmap {
        val paint = Paint()
        paint.color = Color.parseColor(textColor)
        paint.textSize = textSize.toFloat()
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true

        val width = paint.measureText(text, 0, text.length)

        val top = paint.fontMetrics.top
        val bottom = paint.fontMetrics.bottom

        val bm = Bitmap.createBitmap(
            (width + padding * 2).toInt(),
            (bottom - top + padding * 2).toInt(),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bm)

        canvas.drawColor(Color.parseColor(bgColor))


        canvas.drawText(text, padding.toFloat(), -top + padding, paint)
        return bm
    }

    fun loadBitmapTexture(bitmap: Bitmap): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        val bitmapBuffer = ByteBuffer.allocate(bitmap.height * bitmap.width * 4)
        bitmap.copyPixelsToBuffer(bitmapBuffer)
        bitmapBuffer.flip()

        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap.width,
            bitmap.height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bitmapBuffer
        )
        return textureIds[0]
    }
}