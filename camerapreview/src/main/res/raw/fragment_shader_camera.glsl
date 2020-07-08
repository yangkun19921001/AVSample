#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 ft_Position;
uniform samplerExternalOES sTexture;
void main() {
    gl_FragColor=texture2D(sTexture, ft_Position);
}
