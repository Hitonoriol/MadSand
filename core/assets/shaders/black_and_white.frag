varying vec2 v_texCoords;
uniform sampler2D u_texture0;

void main()
{
	gl_FragColor = texture2D(u_texture0, v_texCoords);
	gl_FragColor = vec4(sqrt(dot(gl_FragColor * gl_FragColor, vec4(.299,  .587, .114, 0))));
}