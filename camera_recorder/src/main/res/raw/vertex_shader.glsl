attribute vec4 v_Position;
attribute vec2 f_Position;
varying vec2 ft_Position;
void main() {
    ft_Position = f_Position;
    gl_Position = v_Position;
}
