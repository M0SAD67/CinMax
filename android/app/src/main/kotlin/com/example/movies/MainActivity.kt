package com.example.movies

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.util.Rational
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterFragmentActivity() {
    private val CHANNEL = "pip_channel"
    private var isPlaying = true
    private var enableAutoPip = false  // Enable auto-PiP when user leaves while playing

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_REWIND = "ACTION_REWIND"
        const val ACTION_FORWARD = "ACTION_FORWARD"
    }

    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> {
                    isPlaying = true
                    flutterEngine?.dartExecutor?.let {
                        MethodChannel(it.binaryMessenger, CHANNEL).invokeMethod("play", null)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        updatePipActions()
                    }
                }
                ACTION_PAUSE -> {
                    isPlaying = false
                    flutterEngine?.dartExecutor?.let {
                        MethodChannel(it.binaryMessenger, CHANNEL).invokeMethod("pause", null)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        updatePipActions()
                    }
                }
                ACTION_REWIND -> {
                    flutterEngine?.dartExecutor?.let {
                        MethodChannel(it.binaryMessenger, CHANNEL).invokeMethod("rewind", null)
                    }
                }
                ACTION_FORWARD -> {
                    flutterEngine?.dartExecutor?.let {
                        MethodChannel(it.binaryMessenger, CHANNEL).invokeMethod("forward", null)
                    }
                }
            }
        }
    }

    private val PREVIEW_CHANNEL = "com.gallary.mosa/video_preview"
    private val STATS_CHANNEL = "com.example.movies/stats"
    private lateinit var videoPreviewHandler: VideoPreviewHandler

    // Network Stats
    private var lastRxBytes: Long = 0
    private var lastTime: Long = 0

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Initialize handlers
        videoPreviewHandler = VideoPreviewHandler(this)

        // Setup Video Preview Channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, PREVIEW_CHANNEL).setMethodCallHandler { call, result ->
            videoPreviewHandler.handleMethodCall(call, result)
        }

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "enterPip" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        isPlaying = call.argument<Boolean>("isPlaying") ?: true
                        enterPipMode()
                        result.success(true)
                    } else {
                        result.success(false)
                    }
                }
                "isPipSupported" -> {
                    result.success(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                }
                "updatePlayState" -> {
                    isPlaying = call.argument<Boolean>("isPlaying") ?: true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isInPictureInPictureMode) {
                        updatePipActions()
                    }
                    result.success(true)
                }
                "setAutoPip" -> {
                    enableAutoPip = call.argument<Boolean>("enable") ?: false
                    result.success(true)
                }
                else -> result.notImplemented()
            }
        }

        // Stats Channel
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, STATS_CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "getNetworkSpeed" -> {
                    val currentRxBytes = android.net.TrafficStats.getTotalRxBytes()
                    val currentTime = System.currentTimeMillis()
                    
                    if (lastRxBytes == 0L || lastTime == 0L) {
                        lastRxBytes = currentRxBytes
                        lastTime = currentTime
                        result.success(0)
                        return@setMethodCallHandler
                    }

                    val timeDiff = currentTime - lastTime
                    if (timeDiff >= 500) { // Update every 500ms min
                        val bytesDiff = currentRxBytes - lastRxBytes
                        // Calculate Long bits/sec approx or just return bytes/sec
                        // bytes -> bits (*8) -> / seconds (*1000 / ms)
                        // speed = (bytesDiff * 8 * 1000) / timeDiff  (bits per second)
                        // But let's return Bytes per second (Bps) and format in Dart
                        val speedBps = (bytesDiff * 1000) / timeDiff
                        
                        lastRxBytes = currentRxBytes
                        lastTime = currentTime
                        result.success(speedBps)
                    } else {
                        // Return previous valid or 0
                        result.success(0) 
                    }
                }
                else -> result.notImplemented()
            }
        }


        // Register receiver
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_REWIND)
            addAction(ACTION_FORWARD)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pipReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(pipReceiver, filter)
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Auto-enter PiP when user presses home button or switches apps
        if (enableAutoPip && isPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPipMode()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun enterPipMode() {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(buildPipActions())
            .build()
        enterPictureInPictureMode(params)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePipActions() {
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(Rational(16, 9))
            .setActions(buildPipActions())
            .build()
        setPictureInPictureParams(params)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildPipActions(): List<RemoteAction> {
        val actions = mutableListOf<RemoteAction>()

        // Rewind action
        val rewindIntent = PendingIntent.getBroadcast(
            this,
            1,
            Intent(ACTION_REWIND),
            PendingIntent.FLAG_IMMUTABLE
        )
        actions.add(
            RemoteAction(
                Icon.createWithResource(this, R.drawable.ic_pip_rewind),
                "Rewind",
                "Rewind 10 seconds",
                rewindIntent
            )
        )

        // Play/Pause action
        if (isPlaying) {
            val pauseIntent = PendingIntent.getBroadcast(
                this,
                2,
                Intent(ACTION_PAUSE),
                PendingIntent.FLAG_IMMUTABLE
            )
            actions.add(
                RemoteAction(
                    Icon.createWithResource(this, R.drawable.ic_pip_pause),
                    "Pause",
                    "Pause video",
                    pauseIntent
                )
            )
        } else {
            val playIntent = PendingIntent.getBroadcast(
                this,
                3,
                Intent(ACTION_PLAY),
                PendingIntent.FLAG_IMMUTABLE
            )
            actions.add(
                RemoteAction(
                    Icon.createWithResource(this, R.drawable.ic_pip_play),
                    "Play",
                    "Play video",
                    playIntent
                )
            )
        }

        // Forward action
        val forwardIntent = PendingIntent.getBroadcast(
            this,
            4,
            Intent(ACTION_FORWARD),
            PendingIntent.FLAG_IMMUTABLE
        )
        actions.add(
            RemoteAction(
                Icon.createWithResource(this, R.drawable.ic_pip_forward),
                "Forward",
                "Forward 10 seconds",
                forwardIntent
            )
        )

        return actions
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(pipReceiver)
        if (::videoPreviewHandler.isInitialized) {
            videoPreviewHandler.dispose()
        }
    }
}
