package com.example.pointcloudpoc.common.rendering

import android.opengl.GLES30
import java.io.Closeable

class Texture(val target: Int, wrapMode: Int) : Closeable {
    private val idArray = intArrayOf(0)
    val id get() = idArray[0]

    init {
        // Create texture
        GLES30.glGenTextures(1, idArray, 0);

        // Set new texture as current
        GLES30.glBindTexture(target, id);

        // Set wrap mode
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, wrapMode);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, wrapMode);

        // Set sampling
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
    }

    override fun close() {
        if (id == 0) {
            return
        }

        GLES30.glDeleteTextures(1, idArray, 0)
        idArray[0]
    }
}