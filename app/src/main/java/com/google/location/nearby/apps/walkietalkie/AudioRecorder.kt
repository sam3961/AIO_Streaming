package com.google.location.nearby.apps.walkietalkie

import android.annotation.SuppressLint
import android.media.*
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.Process.THREAD_PRIORITY_AUDIO
import android.os.Process.setThreadPriority
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.location.nearby.apps.walkietalkie.Constants.TAG
import java.io.IOException
import java.io.OutputStream

/**
 * When created, you must pass a [ParcelFileDescriptor]. Once [.start] is called, the
 * file descriptor will be written to until [.stop] is called.
 */
class AudioRecorder //Sender Class Sending audio from this class
/**
 * A simple audio recorder.
 *
 * @param file The output stream of the recording.
 */
(file: ParcelFileDescriptor) {
    /** The stream to write to.  */
    private val mOutputStream: OutputStream

    /**
     * If true, the background thread will continue to loop and record audio. Once false, the thread
     * will shut down.
     */
    /** @return True if actively recording. False otherwise.
     */
    @Volatile
    var isRecording: Boolean = false
        private set

    /** The background thread recording audio for us.  */
    private var mThread: Thread? = null

    init {
        mOutputStream = ParcelFileDescriptor.AutoCloseOutputStream(file)
    }

    /** Starts recording audio.  */
    fun start() {
        if (isRecording) {
            Log.w(TAG, "Already running")
            return
        }

        isRecording = true
        mThread = object : Thread() {
            @SuppressLint("NewApi")
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun run() {
                setThreadPriority(THREAD_PRIORITY_AUDIO)

                val config = AudioPlaybackCaptureConfiguration.Builder(AudioCaptureService.mediaProjection!!)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                        .addMatchingUsage(AudioAttributes.USAGE_GAME)
                        .build()

                val buffer = Buffer()

                val audioFormat = AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(buffer.sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .build()

/*
                val record = AudioRecord.Builder()
                        .setAudioFormat(audioFormat)
                        .setAudioPlaybackCaptureConfig(config)
                        .setBufferSizeInBytes(buffer.size)
                        .build()
*/

                val record = AudioRecord(
                        MediaRecorder.AudioSource.CAMCORDER,
                        buffer.sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        buffer.size)




                if (record.state != AudioRecord.STATE_INITIALIZED) {
                    Log.w(TAG, "Failed to start recording")
                    isRecording = false
                    return
                }

                record.startRecording()
                Log.d("RecordingStarted", buffer.sampleRate.toString() + "  " + buffer.size)

                // While we're running, we'll read the bytes from the AudioRecord and write them
                // to our output stream.
                try {
                    while (isRecording) {
                        val len = record.read(buffer.data, 0, buffer.size)
                        if (len >= 0 && len <= buffer.size) {
                            Log.d("bufferDataSend", buffer.data[0].toString() + "  Size  "+buffer.size+"  sample  "+buffer.sampleRate.toString())
                            mOutputStream.write(buffer.data, 0, len)
                            mOutputStream.flush()
                        } else {
                            Log.w(TAG, "Unexpected length returned: $len")
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Exception with recording stream", e)
                } finally {
                    stopInternal()
                    try {
                        record.stop()
                    } catch (e: IllegalStateException) {
                        Log.e(TAG, "Failed to stop AudioRecord", e)
                    }

                    record.release()
                }
            }
        }
        mThread!!.start()
    }

    private fun stopInternal() {
        isRecording = false
        try {
            mOutputStream.close()
        } catch (e: IOException) {
            Log.e(TAG, "Failed to close output stream", e)
        }

    }

    /** Stops recording audio.  */
    fun stop() {
        stopInternal()
        try {
            mThread!!.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, "Interrupted while joining AudioRecorder thread", e)
            Thread.currentThread().interrupt()
        }

    }

    private class Buffer : AudioBuffer() {
        override fun validSize(size: Int): Boolean {
            return size != AudioRecord.ERROR && size != AudioRecord.ERROR_BAD_VALUE
        }

        override fun getMinBufferSize(sampleRate: Int): Int {
            return AudioRecord.getMinBufferSize(
                    sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        }
    }
    
    
}
