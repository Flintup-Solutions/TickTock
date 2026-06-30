package com.ticktock.tts

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object TimeAnnouncer {
    private val isSpeaking = AtomicBoolean(false)

    fun announceCurrentTime(context: Context) {
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

            speakBlocking(appContext, text)
        } finally {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, previousVolume, 0)
            isSpeaking.set(false)
        }
    }

    private fun speakBlocking(context: Context, text: String) {
        val latch = CountDownLatch(1)
        val handler = Handler(Looper.getMainLooper())
        var ttsRef: TextToSpeech? = null

        handler.post {
            ttsRef = TextToSpeech(context) { status ->
                val tts = ttsRef ?: run {
                    latch.countDown()
                    return@TextToSpeech
                }

                if (status != TextToSpeech.SUCCESS) {
                    tts.shutdown()
                    latch.countDown()
                    return@TextToSpeech
                }

                tts.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                tts.setSpeechRate(0.9f)

                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit

                    override fun onDone(utteranceId: String?) {
                        tts.shutdown()
                        latch.countDown()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        tts.shutdown()
                        latch.countDown()
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        tts.shutdown()
                        latch.countDown()
                    }
                })

                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
            }
        }

        latch.await(15, TimeUnit.SECONDS)
    }

    private const val UTTERANCE_ID = "ticktock_time_announcement"
}
