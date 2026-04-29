package com.application.habittracker.ui.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

@Composable
actual fun rememberSoundPlayer(): () -> Unit = remember { ::playCompletionBeep }

private fun playCompletionBeep() {
    Thread {
        try {
            val sampleRate = 44100

            // Tone 1: A4 (440 Hz) — 120 ms
            val dur1 = sampleRate * 120 / 1000
            // Silent gap — 30 ms
            val gap  = sampleRate * 30  / 1000
            // Tone 2: C#5 (554 Hz) — 200 ms
            val dur2 = sampleRate * 200 / 1000

            val totalSamples = dur1 + gap + dur2

            val samples = ShortArray(totalSamples) { i ->
                when {
                    i < dur1 -> {
                        val t   = i.toDouble() / sampleRate
                        val env = min(i / 441.0, 1.0) * min((dur1 - i) / 1323.0, 1.0)
                        (Short.MAX_VALUE * sin(2.0 * PI * 440.0 * t) * env * 0.70).toInt().toShort()
                    }
                    i < dur1 + gap -> 0
                    else -> {
                        val j   = i - dur1 - gap
                        val t   = j.toDouble() / sampleRate
                        val env = min(j / 441.0, 1.0) * min((dur2 - j) / 3528.0, 1.0)
                        (Short.MAX_VALUE * sin(2.0 * PI * 554.37 * t) * env * 0.75).toInt().toShort()
                    }
                }
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBuf, totalSamples * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(samples, 0, totalSamples)
            track.play()
            Thread.sleep(totalSamples * 1000L / sampleRate + 80)
            track.stop()
            track.release()
        } catch (_: Exception) { }
    }.start()
}
