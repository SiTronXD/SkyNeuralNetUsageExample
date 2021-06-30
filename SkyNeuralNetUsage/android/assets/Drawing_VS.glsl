attribute vec4 a_position;
attribute vec2 a_texCoord0;

varying vec2 uv;

uniform mat4 projectionMatrix;

void main()
{
	uv = a_texCoord0;

	gl_Position = projectionMatrix * a_position;
}