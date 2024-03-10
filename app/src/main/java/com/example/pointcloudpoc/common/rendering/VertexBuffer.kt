package com.example.pointcloudpoc.common.rendering

import android.opengl.GLES30
import java.nio.FloatBuffer

class VertexBuffer(data: FloatBuffer?, val entriesPerVertex: Int)
        : GLBuffer(data, GLES30.GL_ARRAY_BUFFER, 4) {
    val vertexCount get() = size / entriesPerVertex
}