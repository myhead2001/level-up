package com.sololeveling.systemfit.presentation.utils

import android.content.Context
import android.media.MediaPlayer
import com.sololeveling.systemfit.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

object SoundManager {
    private var appContext: Context? = null
    private val activePlayers = mutableListOf<MediaPlayer>()
    private var penaltyPlayer: MediaPlayer? = null
    private var levelUpPlayer: MediaPlayer? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun getAudioPrefs(): Pair<Boolean, Float> {
        val ctx = appContext ?: return Pair(false, 0f)
        val sharedPrefs = ctx.getSharedPreferences("system_fit_audio", Context.MODE_PRIVATE)
        val enabled = sharedPrefs.getBoolean("audio_enabled", true)
        val volume = sharedPrefs.getFloat("audio_volume", 0.5f)
        return Pair(enabled, volume)
    }

    /**
     * Fade out a MediaPlayer over [durationMs] then stop and release it.
     */
    private suspend fun fadeOutAndRelease(player: MediaPlayer, durationMs: Long = 1500L) {
        try {
            val (_, maxVolume) = getAudioPrefs()
            val steps = 20
            val stepDelay = durationMs / steps
            for (i in 1..steps) {
                if (!player.isPlaying) break
                val vol = maxVolume * (1.0f - (i.toFloat() / steps.toFloat()))
                player.setVolume(vol.coerceAtLeast(0f), vol.coerceAtLeast(0f))
                delay(stepDelay)
            }
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        } catch (e: Exception) {
            try { player.release() } catch (_: Exception) {}
        }
    }

    private fun playSound(resId: Int, isStartup: Boolean = false) {
        val ctx = appContext ?: return
        val (isSoundEnabled, volume) = getAudioPrefs()

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

    /**
     * Fade out and stop the startup sound instead of abruptly cutting it.
     */
    fun stopStartup() {
        val playersToFade: List<MediaPlayer>
        synchronized(activePlayers) {
            playersToFade = activePlayers.toList()
            activePlayers.clear()
        }
        if (playersToFade.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            for (player in playersToFade) {
                fadeOutAndRelease(player, durationMs = 1200L)
            }
        }
    }

    /**
     * Play level-up music in a loop. Call [stopLevelUp] to fade it out.
     */
    fun playLevelUp() {
        val ctx = appContext ?: return
        val (isSoundEnabled, volume) = getAudioPrefs()
        if (!isSoundEnabled) return

        stopLevelUp() // Stop any existing level-up music first

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaPlayer = MediaPlayer.create(ctx, R.raw.level_up) ?: return@launch
                mediaPlayer.setVolume(volume, volume)
                mediaPlayer.isLooping = true
                synchronized(this@SoundManager) {
                    levelUpPlayer = mediaPlayer
                }
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Fade out and stop the looping level-up music.
     */
    fun stopLevelUp() {
        val player = synchronized(this) {
            val p = levelUpPlayer
            levelUpPlayer = null
            p
        } ?: return

        CoroutineScope(Dispatchers.IO).launch {
            fadeOutAndRelease(player, durationMs = 1500L)
        }
    }

    fun playClaimRewards() {
        playSound(R.raw.claim_rewards)
    }

    /**
     * Play quest completion sound. Reuses the claim_rewards sound
     * since no dedicated quest_complete resource exists yet.
     */
    fun playQuestComplete() {
        playSound(R.raw.claim_rewards)
    }

    fun playPenalty() {
        val ctx = appContext ?: return
        val (isSoundEnabled, volume) = getAudioPrefs()
        if (!isSoundEnabled) return

        stopPenalty()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mediaPlayer = MediaPlayer.create(ctx, R.raw.penalty) ?: return@launch
                mediaPlayer.setVolume(volume, volume)
                mediaPlayer.isLooping = true
                synchronized(this@SoundManager) {
                    penaltyPlayer = mediaPlayer
                }
                mediaPlayer.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopPenalty() {
        val player = synchronized(this) {
            val p = penaltyPlayer
            penaltyPlayer = null
            p
        } ?: return

        CoroutineScope(Dispatchers.IO).launch {
            fadeOutAndRelease(player, durationMs = 1500L)
        }
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

