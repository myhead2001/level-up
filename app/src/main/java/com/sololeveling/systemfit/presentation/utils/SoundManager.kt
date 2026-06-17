package com.sololeveling.systemfit.presentation.utils

import android.content.Context
import android.media.MediaPlayer
import com.sololeveling.systemfit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SoundManager {
    private var appContext: Context? = null
    private val activePlayers = mutableListOf<MediaPlayer>()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun playSound(resId: Int, isStartup: Boolean = false) {
        val ctx = appContext ?: return
        val sharedPrefs = ctx.getSharedPreferences("system_fit_audio", Context.MODE_PRIVATE)
        val isSoundEnabled = sharedPrefs.getBoolean("audio_enabled", true)
        val volume = sharedPrefs.getFloat("audio_volume", 0.5f) // Default is 0.5f (medium)

        if (!isSoundEnabled) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaPlayer = MediaPlayer.create(ctx, resId) ?: return@launch
                mediaPlayer.setVolume(volume, volume)
                if (isStartup) {
                    synchronized(activePlayers) {
                        activePlayers.add(mediaPlayer)
                    }
                }
                mediaPlayer.setOnCompletionListener {
                    if (isStartup) {
                        synchronized(activePlayers) {
                            activePlayers.remove(mediaPlayer)
                        }
                    }
                    it.release()
                }
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopStartup() {
        synchronized(activePlayers) {
            val iterator = activePlayers.iterator()
            while (iterator.hasNext()) {
                val player = iterator.next()
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                iterator.remove()
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
        playSound(R.raw.startup, isStartup = true)
    }
}
