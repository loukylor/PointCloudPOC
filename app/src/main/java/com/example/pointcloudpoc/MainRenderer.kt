package com.example.pointcloudpoc

import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.pointcloudpoc.common.rendering.Mesh
import com.example.pointcloudpoc.common.rendering.Shader
import com.example.pointcloudpoc.common.rendering.Texture
import com.example.pointcloudpoc.common.rendering.VertexBuffer
import com.google.ar.core.Coordinates2d
import com.google.ar.core.TrackingState
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

private val corners = FloatBuffer.wrap(floatArrayOf(-1f, -1f, +1f, -1f, -1f, +1f, +1f, +1f))

class MainRenderer(private val activity: MainActivity) : GLSurfaceView.Renderer {
    private lateinit var pointCloudShader: Shader
    private lateinit var pointCloudBuffer: VertexBuffer
    private lateinit var pointCloudMesh: Mesh

    private lateinit var cameraShader: Shader
    private val cameraTexCoordsFloatBuffer = FloatBuffer.allocate(2 * 4)
    private lateinit var cameraTexCoordsBuffer: VertexBuffer
    private lateinit var cameraColorTexture: Texture
    private lateinit var cameraMesh: Mesh

    private val session get() = activity.sessionHelper.session

    private var hasInitCameraTextures = false
    private var hasViewportChanged = true
    private var viewportWidth = 0
    private var viewportHeight = 0

    // Temporary matrix allocated here to reduce number of allocations for each frame.
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)

    private val vPMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        pointCloudShader = Shader(
            "shaders/point_cloud.vert", "shaders/point_cloud.frag", activity.assets)
        pointCloudBuffer = VertexBuffer(null, 4)
        pointCloudMesh = Mesh(arrayOf(pointCloudBuffer), GLES30.GL_POINTS)

        cameraShader = Shader(
            "shaders/camera.vert", "shaders/camera.frag", activity.assets)
        cameraShader.onDraw()

        val cameraCornersBuffer = VertexBuffer(corners, 2)
        cameraTexCoordsBuffer = VertexBuffer(null, 2)
        cameraColorTexture = Texture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_CLAMP_TO_EDGE)
        cameraMesh = Mesh(
            arrayOf(cameraCornersBuffer, cameraTexCoordsBuffer), GLES30.GL_TRIANGLE_STRIP)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        hasViewportChanged = true
        viewportWidth = width
        viewportHeight = height
    }

    override fun onDrawFrame(gl: GL10?) {
        val session = session ?: return
        gl ?: return

        // Give a texture for the camera to output to
        if (!hasInitCameraTextures) {
            session.setCameraTextureNames(intArrayOf(cameraColorTexture.id))
            hasInitCameraTextures = true
        }

        // Update display geometry if viewport has changed
        if (hasViewportChanged) {
            val rotation = activity.windowManager.defaultDisplay.rotation
            session.setDisplayGeometry(rotation, viewportWidth, viewportHeight)
            hasViewportChanged = false
        }

        val frame = session.update()

        // Update camera vertices
        if (frame.hasDisplayGeometryChanged()) {
            frame.transformCoordinates2d(
                Coordinates2d.OPENGL_NORMALIZED_DEVICE_COORDINATES,
                corners,
                Coordinates2d.TEXTURE_NORMALIZED,
                cameraTexCoordsFloatBuffer)
            cameraTexCoordsBuffer.fill(cameraTexCoordsFloatBuffer)
        }

        val camera = frame.camera

        // Clear old stuff from buffers
        GLES30.glDepthMask(true)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

        // Set camera texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(cameraColorTexture.target, cameraColorTexture.id)
        var location = cameraShader.getUniformLocation("v_CameraTexCoord")
        GLES30.glUniform1i(location, GLES30.GL_TEXTURE0)

        // Draw background
        cameraShader.onDraw()
        GLES30.glDepthMask(false)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ZERO, GLES30.GL_ONE, GLES30.GL_ZERO)
        cameraMesh.onDraw()

        if (camera.trackingState == TrackingState.PAUSED) {
            return
        }

        // Get point cloud points
        val pointCloud = frame.acquirePointCloud()
        pointCloudBuffer.fill(pointCloud.points)

        // Get projection matrix (maps 3d points to 2d points on the screen)
        camera.getProjectionMatrix(projectionMatrix, 0, 0.1f, 100.0f)

        // Get view matrix (translates points based off camera position and rotation)
        camera.getViewMatrix(viewMatrix, 0)

        // Multiply the matrices together
        Matrix.multiplyMM(vPMatrix, 0, projectionMatrix, 0, viewMatrix, 0)

        // Draw the point cloud
        pointCloudShader.onDraw()
        GLES30.glDepthMask(true)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ZERO, GLES30.GL_ONE, GLES30.GL_ZERO)
        pointCloudMesh.onDraw()

        //Set the matrix value in the shader code
        location = pointCloudShader.getUniformLocation("u_MVProjection")
        GLES30.glUniformMatrix4fv(location, 1, false, vPMatrix, 0)

        var error = GLES30.glGetError()
        while (error != GLES30.GL_NO_ERROR) {
            Log.i("err", error.toString())
            error = GLES30.glGetError()
        }
    }
}