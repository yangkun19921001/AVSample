precision mediump float;
varying vec2 ft_Position;
uniform sampler2D sTexture;
void main() {
    lowp vec4 textureColor = texture2D(sTexture, ft_Position);
    gl_FragColor = vec4((textureColor.rgb + vec3(-0.5f)), textureColor.w);
}
