package com.devyk.av.camera_recorder.callback

/**
 * <pre>
 *     author  : devyk on 2020-07-06 11:05
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is IRenderer
 *
 *
 *     OpenGL ES 坐标系：
 *     @see ![](https://devyk.oss-cn-qingdao.aliyuncs.com/blog/20200706174743.png)
 * </pre>
 */
public interface IRenderer {
    /**
     * 当 Surface 创建的时候
     */
    public fun onSurfaceCreate(width: Int, height: Int);

    /**
     * 当 surface 窗口改变的时候
     */
    public fun onSurfaceChange(width: Int, height: Int);

    /**
     * 绘制的时候
     */
    public fun onDraw();
}