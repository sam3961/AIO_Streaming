package com.google.location.nearby.apps.walkietalkie

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SYNC
import android.content.IntentFilter
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import kotlin.experimental.and
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import android.widget.Toast
import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


class AudioCaptureService : Service(){

    private var stopped = false
    private var instigatedEnd = false
    internal lateinit var mReceiver: BroadcastReceiver//registers for the events we want to know about


    private val sampleRate=44100//22050
    private lateinit var mediaProjectionManager: MediaProjectionManager

    private lateinit var audioCaptureThread: Thread
    private var audioRecord: AudioRecord? = null
    internal var intent:Intent?=null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            createNotificationChannel()
        }
        startForeground(
                SERVICE_ID,
                NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).build()
        )

        // use applicationContext to avoid memory leak on Android 10.
        // see: https://partnerissuetracker.corp.google.com/issues/139732252
        mediaProjectionManager =
                applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun createNotificationChannel() {
        val serviceChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Audio Capture Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        val manager = getSystemService(NotificationManager::class.java) as NotificationManager
        manager.createNotificationChannel(serviceChannel)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (intent != null) {
            when (intent.action) {
                ACTION_START -> {
                    mediaProjection =
                            mediaProjectionManager.getMediaProjection(
                                    Activity.RESULT_OK,
                                    intent.getParcelableExtra(EXTRA_RESULT_DATA)!!
                            ) as MediaProjection
                    //startAudioCapture()
                    Service.START_STICKY
                }
                ACTION_STOP -> {
                    stopAudioCapture()
                    Service.START_NOT_STICKY
                }
                else -> throw IllegalArgumentException("Unexpected action received: ${intent.action}")
            }
        } else {
            Service.START_NOT_STICKY
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun stopAudioCapture() {
        requireNotNull(mediaProjection) { "Tried to stop audio capture, but there was no ongoing capture in place!" }

        mediaProjection!!.stop()
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? = null

    companion object {
        public const val LOG_TAG = "AudioCaptureService"
        public const val SERVICE_ID = 123
        public const val NOTIFICATION_CHANNEL_ID = "AudioCapture channel"
        public open var mediaProjection: MediaProjection? = null


        public const val NUM_SAMPLES_PER_READ = 1024
        public const val BYTES_PER_SAMPLE = 2 // 2 bytes since we hardcoded the PCM 16-bit format
        public const val BUFFER_SIZE_IN_BYTES = NUM_SAMPLES_PER_READ * BYTES_PER_SAMPLE

        const val ACTION_START = "AudioCaptureService:Start"
        const val ACTION_STOP = "AudioCaptureService:Stop"
        const val EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData"
    }



}

