package com.example.pointcloudpoc.common.rendering

import android.opengl.GLES30
import android.util.Log
import java.io.Closeable

class Mesh(private val buffers: Array<VertexBuffer>, val drawMode: Int) : Closeable {
    private val vertexArrayIdArray = intArrayOf(0)
    private val vertexArrayId get() = vertexArrayIdArray[0]

    init {
        // Create vertex array object (VAO)
        GLES30.glGenVertexArrays(1, vertexArrayIdArray, 0)

        // Set current VAO as the working one
        GLES30.glBindVertexArray(vertexArrayId)

        for (i in buffers.indices) {
            // Set current buffer as the working one
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[i].id)

            // Tell OpenGL how to treat the float data as points
            GLES30.glVertexAttribPointer(
                i, buffers[i].entriesPerVertex, GLES30.GL_FLOAT, false,
                buffers[i].entriesPerVertex * 4, 0)

            // Enable vertex attributes array stuff on the vertex buffer
            GLES30.glEnableVertexAttribArray(i)
        }
    }

    fun onDraw() {
        if (vertexArrayId == 0) {
            // TODO: throw an error or something
            Log.i("fuck you", "fuck you")
            return
        }

        GLES30.glBindVertexArray(vertexArrayId)
        for (buffer in buffers) {
            GLES30.glDrawArrays(drawMode, 0, buffer.vertexCount)
        }
    }

    override fun close() {
        if (vertexArrayId == 0) {
            return
        }

        GLES30.glDeleteVertexArrays(1, vertexArrayIdArray, 0)
        vertexArrayIdArray[0] = 0

        for (buffer in buffers) {
            buffer.close()
        }
    }
}