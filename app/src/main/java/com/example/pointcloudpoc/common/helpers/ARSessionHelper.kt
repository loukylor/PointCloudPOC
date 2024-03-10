package com.example.pointcloudpoc.common.helpers

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.KeyCharacterMap.UnavailableException
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.core.ArCoreApk
import com.google.ar.core.ArCoreApk.Availability
import com.google.ar.core.ArCoreApk.InstallStatus
import com.google.ar.core.Config
import com.google.ar.core.Session

private const val TAG = "ARSessionHelper"

class ARSessionHelper(private val activity: ComponentActivity) : DefaultLifecycleObserver {
    var session: Session? = null
        private set

    // Create the object that will handle requesting permissions
    private val requestPermissionsLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                tryCreateSession()
            } else {
                Log.i(TAG, "User denied camera permissions")
                activity.finish()
            }
        }

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onPause(owner: LifecycleOwner) {
        session?.pause()
    }

    override fun onResume(owner: LifecycleOwner) {
        val session = this.session ?: tryCreateSession() ?: return
        session.resume()
        this.session = session
    }

    override fun onDestroy(owner: LifecycleOwner) {
        // According to the docs, the session uses a lot of memory, so let's make sure to close it
        session?.close()
    }

    private fun tryCreateSession(): Session? {
        // TODO: Figure out the best way to do error handling so that it doesn't just log it

        // Check camera permissions

        // Check permission, if no permission, check if user is asking for why the app needs
        // permission, and if false, ask for permission from the user
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            return if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity, Manifest.permission.CAMERA)) {
                // TODO: Add something here (when the user asks for why the app needs permission)
                null
            } else {
                requestPermissionsLauncher.launch(Manifest.permission.CAMERA)
                // Wait for callback
                null
            }
        }

        // Check for availability
        // Pulled from the ARCore docs:
        // https://developers.google.com/ar/develop/java/session-config#kotlin
        when (ArCoreApk.getInstance().checkAvailability(activity)) {
            Availability.SUPPORTED_INSTALLED -> { }
            Availability.SUPPORTED_APK_TOO_OLD, Availability.SUPPORTED_NOT_INSTALLED -> {
                try {
                    when (ArCoreApk.getInstance().requestInstall(activity, true)) {
                        InstallStatus.INSTALL_REQUESTED -> {
                            Log.i(TAG, "Install requested")
                            return null
                        }
                        InstallStatus.INSTALLED -> { }
                    }
                } catch (e: UnavailableException) {
                    Log.e(TAG, "ARCore not installed", e)
                    return null
                }
            }
            Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                Log.i(TAG, "Device does not support AR")
                return null
            }

            Availability.UNKNOWN_CHECKING -> {
                Log.i(TAG, "Contacting the ARCore server to check availability")
                return null
            }
            Availability.UNKNOWN_ERROR, Availability.UNKNOWN_TIMED_OUT -> {
                Log.i(TAG, "Unknown error checking for availability. Device may be offline")
                return null
            }
        }

        // Create session
        val session = Session(activity)

        // Configure
        val config = Config(session)

        config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR

        if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
            config.depthMode = Config.DepthMode.AUTOMATIC
        } else {
            Log.i(TAG, "Device does not support depth")
            session.close()
            return null
        }
        return session
    }
}