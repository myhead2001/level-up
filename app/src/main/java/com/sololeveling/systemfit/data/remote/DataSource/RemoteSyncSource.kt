package com.sololeveling.systemfit.data.remote.DataSource

import com.sololeveling.systemfit.data.remote.model.SupabaseUserDto
import com.sololeveling.systemfit.domain.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject

class RemoteSyncSource @Inject constructor(
    private val supabase: SupabaseClient
) {
    suspend fun syncUser(user: User) {
        try {
            val dto = SupabaseUserDto(
                id = user.id,
                level = user.level,
                currentXp = user.currentXp,
                str = user.str,
                vit = user.vit,
                agi = user.agi,
                availableStatPoints = user.availableStatPoints,
                currentStreak = user.currentStreak
            )
            supabase.postgrest["users"].upsert(dto)
        } catch (e: Exception) {
            // Silently fail remote sync for offline capability
            e.printStackTrace()
        }
    }
}
