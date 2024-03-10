package com.example.pointcloudpoc.common.rendering

import android.content.res.AssetManager
import android.opengl.GLES30
import android.util.Log
import java.io.Closeable

// TODO: Check for errors
class Shader(vertSrc: String, fragSrc: String) : Closeable {
    private var programId: Int

    init {
        // Compile and create vertex shader (handles positions of drawn vertices)
        val vertShaderId = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER)
        GLES30.glShaderSource(vertShaderId, vertSrc)
        GLES30.glCompileShader(vertShaderId)

        // Compile and create fragment shader (handles color of drawn vertices)
        val fragShaderId = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER)
        GLES30.glShaderSource(fragShaderId, fragSrc)
        GLES30.glCompileShader(fragShaderId)

        // Create the program that openGL will actually use to draw with the shader
        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vertShaderId)
        GLES30.glAttachShader(programId, fragShaderId)
        GLES30.glLinkProgram(programId)

        // Now that the shaders are attached to a program, the objects themselves aren't needed
        GLES30.glDeleteShader(vertShaderId)
        GLES30.glDeleteShader(fragShaderId)
    }

    constructor(vertPath: String, fragPath: String, assetManager: AssetManager) : this(
        assetManager.open(vertPath).bufferedReader().readText(),
        assetManager.open(fragPath).bufferedReader().readText()) { }

    fun onDraw() {
        if (programId == 0) {
            // TODO: Throw an error or something
            return
        }

        GLES30.glUseProgram(programId)
    }

    fun getUniformLocation(uniform: String): Int {
        return GLES30.glGetUniformLocation(programId, uniform)
    }

    override fun close() {
        if (programId == 0) {
            return
        }

        GLES30.glDeleteProgram(programId)
        programId = 0
    }
}