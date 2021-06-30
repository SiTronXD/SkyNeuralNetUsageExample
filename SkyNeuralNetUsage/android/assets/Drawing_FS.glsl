precision highp float;

varying vec2 uv;

uniform sampler2D mainTexture;

void main()
{
	vec4 texCol = texture(mainTexture, uv);

	texCol.r += 0.3;

	gl_FragColor = texCol;
}