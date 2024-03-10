package com.example.pointcloudpoc

import android.opengl.GLSurfaceView

class MainView(private val activity: MainActivity) : GLSurfaceView(activity) {
    private var renderer: Renderer

    init {
        setEGLContextClientVersion(2)

        renderer = MainRenderer(activity)
        setRenderer(renderer)
    }
}