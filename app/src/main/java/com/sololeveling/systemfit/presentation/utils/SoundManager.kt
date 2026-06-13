package com.sololeveling.systemfit.presentation.utils

import android.content.Context
import android.media.MediaPlayer
import com.sololeveling.systemfit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SoundManager {
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun playSound(resId: Int) {
        val ctx = appContext ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaPlayer = MediaPlayer.create(ctx, resId) ?: return@launch
                mediaPlayer.setOnCompletionListener {
                    it.release()
                }
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playLevelUp() {
        playSound(R.raw.level_up)
    }

    fun playClaimRewards() {
        playSound(R.raw.claim_rewards)
    }

    fun playPenalty() {
        playSound(R.raw.penalty)
    }

    fun playWindowOpen() {
        playSound(R.raw.window_open)
    }

    fun playWindowClose() {
        playSound(R.raw.click)
    }

    fun playNavigation() {
        playSound(R.raw.click)
    }

    fun playStatBoost() {
        playSound(R.raw.stat_boost)
    }

    fun playStartup() {
        playSound(R.raw.startup)
    }
}
