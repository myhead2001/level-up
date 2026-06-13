package com.sololeveling.systemfit.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EmergencyHaltUseCaseTest {

    private val useCase = EmergencyHaltUseCase()

    @Test
    fun `invokeHalt returns active cooldown for 180 seconds`() {
        val result = useCase.invokeHalt()
        assertThat(result.isCooldownActive).isTrue()
        assertThat(result.remainingCooldownSeconds).isEqualTo(180)
    }
}
