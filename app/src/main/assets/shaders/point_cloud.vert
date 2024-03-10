#version 300 es

uniform mat4 u_MVProjection;

layout (location = 0) in vec4 a_Pos;

void main()
{
    gl_Position = u_MVProjection * vec4(a_Pos.xyz, 1.0);
    gl_PointSize = 50.0f;
}