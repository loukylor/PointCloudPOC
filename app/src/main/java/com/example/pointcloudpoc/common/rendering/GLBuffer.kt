package com.example.pointcloudpoc.common.rendering

import android.opengl.GLES30
import java.io.Closeable
import java.nio.Buffer

open class GLBuffer(data: Buffer?, private val target: Int, val bytesPerEntry: Int) : Closeable {
    private val idArray = intArrayOf(0)
    val id get() = idArray[0]
    var capacity = 0
        private set
    var size = 0
        private set

    init {
        // Unbind any VAOs to prevent weirdness
        GLES30.glBindVertexArray(0)

        // Create 1 buffer
        GLES30.glGenBuffers(1, idArray, 0)

        // Set current buffer as the working one
        GLES30.glBindBuffer(target, id)

        // Fill buffer with given data
        if (data != null) {
            GLES30.glBufferData(
                target, data.limit() * bytesPerEntry, data, GLES30.GL_DYNAMIC_DRAW)
            capacity = data.limit()
            size = capacity
        }
    }

    fun fill(data: Buffer) {
        // Set current buffer as the working one
        GLES30.glBindBuffer(target, id)

        // Expand or fill current buffer
        if (data.limit() > capacity) {
            GLES30.glBufferData(
                target, data.limit() * bytesPerEntry, data, GLES30.GL_DYNAMIC_DRAW)
            capacity = data.limit()
            size = capacity
        } else {
            GLES30.glBufferSubData(target, 0, data.limit() * bytesPerEntry, data)
            size = data.limit()
        }
    }

    override fun close() {
        if (id == 0) {
            return
        }

        GLES30.glDeleteBuffers(1, idArray, 0)
        idArray[0] = 0
    }
}