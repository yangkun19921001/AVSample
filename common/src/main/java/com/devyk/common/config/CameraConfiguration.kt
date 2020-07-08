package com.devyk.common.config

/**
 * <pre>
 *     author  : devyk on 2020-05-28 23:20
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is CameraConfiguration
 * </pre>
 */
class CameraConfiguration private constructor(builder: Builder) {

    val height: Int
    val width: Int
    val fps: Int
    val rotation: Int
    val facing: Facing
    val orientation: Orientation
    val focusMode: FocusMode

    init {
        height = builder.height
        width = builder.width
        facing = builder.facing
        fps = builder.fps
        orientation = builder.orientation
        focusMode = builder.focusMode
        rotation = builder.rotation
    }

    enum class Facing {
        FRONT,
        BACK
    }

    enum class Orientation {
        LANDSCAPE,
        PORTRAIT
    }

    enum class FocusMode {
        AUTO,
        TOUCH
    }


    class Builder {
        var height = DEFAULT_HEIGHT
        var width = DEFAULT_WIDTH
        var fps = DEFAULT_FPS
        var rotation = DEFAULT_ROTATION
        var facing = DEFAULT_FACING
        var orientation = DEFAULT_ORIENTATION
        var focusMode = DEFAULT_FOCUSMODE

        fun setPreview(height: Int, width: Int): Builder {
            this.height = height
            this.width = width
            return this
        }

        fun setFacing(facing: Facing): Builder {
            this.facing = facing
            return this
        }

        fun setOrientation(orientation: Orientation): Builder {
            this.orientation = orientation
            return this
        }

        fun setFps(fps: Int): Builder {
            this.fps = fps
            return this
        }

        fun setFocusMode(focusMode: FocusMode): Builder {
            this.focusMode = focusMode
            return this
        }

        fun setRotation(rot: Int): Builder {
            this.rotation = rot
            return this
        }

        fun build(): CameraConfiguration {
            return CameraConfiguration(this)
        }
    }

    companion object {
        val DEFAULT_HEIGHT = 480
        val DEFAULT_WIDTH = 640
        val DEFAULT_FPS = 15
        val DEFAULT_ROTATION = 0
        val DEFAULT_FACING = Facing.FRONT
        val DEFAULT_ORIENTATION = Orientation.PORTRAIT
        val DEFAULT_FOCUSMODE = FocusMode.AUTO

        fun createDefault(): CameraConfiguration {
            return Builder().build()
        }
    }
}