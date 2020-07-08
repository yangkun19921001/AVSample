precision mediump float;
varying vec2 ft_Position;
uniform sampler2D sTexture;
void main() {
    lowp vec4 textureColor = texture2D(sTexture, ft_Position);
     float gray = textureColor.r * 0.2125 + textureColor.g * 0.7154 + textureColor.b * 0.0721;
     gl_FragColor = vec4(gray, gray, gray, textureColor.w);
}
