package com.sololeveling.systemfit.presentation.utils

import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object SoundManager {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playTones(vararg tones: Pair<Int, Int>) {
        CoroutineScope(Dispatchers.IO).launch {
            val tg = toneGenerator ?: try {
                ToneGenerator(AudioManager.STREAM_MUSIC, 85).also { toneGenerator = it }
            } catch (e: Exception) {
                null
            } ?: return@launch

            for ((tone, duration) in tones) {
                try {
                    tg.startTone(tone, duration)
                    delay(duration + 40L) // Wait for tone to finish plus a gap
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun playLevelUp() {
        playTones(
            ToneGenerator.TONE_DTMF_1 to 100,
            ToneGenerator.TONE_DTMF_5 to 100,
            ToneGenerator.TONE_DTMF_9 to 100,
            ToneGenerator.TONE_DTMF_D to 150,
            ToneGenerator.TONE_DTMF_A to 250
        )
    }

    fun playClaimRewards() {
        playTones(
            ToneGenerator.TONE_DTMF_3 to 80,
            ToneGenerator.TONE_DTMF_6 to 80,
            ToneGenerator.TONE_DTMF_9 to 80,
            ToneGenerator.TONE_DTMF_C to 150
        )
    }

    fun playPenalty() {
        playTones(
            ToneGenerator.TONE_DTMF_D to 150,
            ToneGenerator.TONE_DTMF_9 to 150,
            ToneGenerator.TONE_DTMF_5 to 150,
            ToneGenerator.TONE_DTMF_1 to 300
        )
    }

    fun playWindowOpen() {
        playTones(
            ToneGenerator.TONE_DTMF_5 to 80,
            ToneGenerator.TONE_DTMF_9 to 120
        )
    }

    fun playWindowClose() {
        playTones(
            ToneGenerator.TONE_DTMF_9 to 80,
            ToneGenerator.TONE_DTMF_5 to 120
        )
    }

    fun playNavigation() {
        playTones(
            ToneGenerator.TONE_DTMF_5 to 60
        )
    }
}
