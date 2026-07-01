package com.ticktock.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

object TimeAnnouncer {
    private val isSpeaking = AtomicBoolean(false)

    suspend fun announceCurrentTime(context: Context) {
        if (!isSpeaking.compareAndSet(false, true)) return

        val appContext = context.applicationContext
        val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

        try {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

            val now = ZonedDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            val text = "The time is ${now.format(formatter)}"

            speak(appContext, text)
        } finally {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, 0)
            isSpeaking.set(false)
        }
    }

    private suspend fun speak(context: Context, text: String) {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                var tts: TextToSpeech? = null
                tts = TextToSpeech(context) { status ->
                    val engine = tts
                    if (engine == null) {
                        if (continuation.isActive) continuation.resume(Unit)
                        return@TextToSpeech
                    }

                    if (status != TextToSpeech.SUCCESS) {
                        engine.shutdown()
                        if (continuation.isActive) continuation.resume(Unit)
                        return@TextToSpeech
                    }

                    engine.setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build(),
                    )
                    engine.setSpeechRate(0.9f)

                    engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) = Unit

                        override fun onDone(utteranceId: String?) {
                            engine.shutdown()
                            if (continuation.isActive) continuation.resume(Unit)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            engine.shutdown()
                            if (continuation.isActive) continuation.resume(Unit)
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            engine.shutdown()
                            if (continuation.isActive) continuation.resume(Unit)
                        }
                    })

                    engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
                }

                continuation.invokeOnCancellation {
                    tts?.stop()
                    tts?.shutdown()
                }
            }
        }
    }

    private const val UTTERANCE_ID = "ticktock_time_announcement"
}
