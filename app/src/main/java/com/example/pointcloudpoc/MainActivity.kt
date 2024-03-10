package com.example.pointcloudpoc

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableInferredTarget
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.pointcloudpoc.common.helpers.ARSessionHelper
import com.example.pointcloudpoc.ui.theme.PointCloudPOCTheme

class MainActivity : ComponentActivity() {
    lateinit var sessionHelper: ARSessionHelper

    private lateinit var gLView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContent {
//            PointCloudPOCTheme {
//                // A surface container using the 'background' color from the theme
//                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
//                    Greeting("bitchass mofo")
//                }
//            }
//        }

        sessionHelper = ARSessionHelper(this)

        gLView = MainView(this)
        setContentView(gLView)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Surface(color = Color(0, 0, 0)) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PointCloudPOCTheme {
        Greeting("bitchass mofo")
    }
}