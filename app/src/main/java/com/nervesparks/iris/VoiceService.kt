package com.nervesparks.iris

import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class VoiceService : Service() {

    companion object {
        const val WAKE_WORD_DETECTED_ACTION = "com.nervesparks.iris.WAKE_WORD_DETECTED"
    }

    private var porcupineManager: PorcupineManager? = null

    private val accessKey = BuildConfig.PICOVOICE_ACCESS_KEY

    private val porcupineManagerCallback = object : PorcupineManagerCallback {
        override fun invoke(keywordIndex: Int) {
            val intent = Intent(WAKE_WORD_DETECTED_ACTION)
            LocalBroadcastManager.getInstance(this@VoiceService).sendBroadcast(intent)
            VoiceServiceState.setListening(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        try {
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(accessKey)
                .setKeywords(arrayOf(Porcupine.BuiltInKeyword.PICOVOICE))
                .build(applicationContext, porcupineManagerCallback)
        } catch (e: PorcupineException) {
            Log.e("VoiceService", "Failed to initialize Porcupine", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        porcupineManager?.start()
        VoiceServiceState.setListening(true)
        return START_STICKY
    }

    override fun onDestroy() {
        porcupineManager?.stop()
        VoiceServiceState.setListening(false)
        super.onDestroy()
    }
}
