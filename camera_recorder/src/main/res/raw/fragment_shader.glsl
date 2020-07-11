precision mediump float;
varying vec2 ft_Position;
uniform sampler2D sTexture;
void main() {
    gl_FragColor=texture2D(sTexture, ft_Position);
}
