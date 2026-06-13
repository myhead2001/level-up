package com.sololeveling.systemfit.domain.usecase

import javax.inject.Inject

class EmergencyHaltUseCase @Inject constructor() {
    
    data class CooldownState(
        val isCooldownActive: Boolean,
        val remainingCooldownSeconds: Int
    )

    // A 3-minute controlled nasal recovery step (180 seconds)
    fun invokeHalt(): CooldownState {
        return CooldownState(
            isCooldownActive = true,
            remainingCooldownSeconds = 180
        )
    }
}
