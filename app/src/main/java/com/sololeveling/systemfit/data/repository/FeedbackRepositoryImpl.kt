package com.sololeveling.systemfit.data.repository

import com.sololeveling.systemfit.data.remote.model.FeedbackDto
import com.sololeveling.systemfit.data.remote.model.FeedbackWithUserDto
import com.sololeveling.systemfit.domain.repository.FeedbackRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient
) : FeedbackRepository {

    override suspend fun submitFeedback(userId: String, category: String, content: String, deviceInfo: String) {
        val dto = FeedbackDto(
            userId = userId,
            category = category,
            content = content,
            deviceInfo = deviceInfo
        )
        // Omit generated fields like id and created_at by using a DTO that excludes them or by Supabase defaulting.
        // The DTO has default empty strings, but Supabase ignores UUID parsing if we don't send it, but wait: 
        // if we send id="", Supabase will try to parse "" as UUID and fail.
        // We should build a JsonObject or a specific Insert DTO.
        val insertMap = mapOf(
            "user_id" to userId,
            "category" to category,
            "content" to content,
            "device_info" to deviceInfo
        )
        
        supabase.postgrest["feedbacks"].insert(insertMap)
    }

    override suspend fun updateFeedback(id: String, content: String) {
        supabase.postgrest["feedbacks"].update(
            {
                set("content", content)
            }
        ) {
            filter {
                eq("id", id)
            }
        }
    }

    override suspend fun deleteFeedback(id: String) {
        supabase.postgrest["feedbacks"].delete {
            filter {
                eq("id", id)
            }
        }
    }

    override suspend fun getAllFeedback(): List<FeedbackWithUserDto> {
        return supabase.postgrest["feedbacks"]
            .select(columns = Columns.raw("*, users(name)"))
            .decodeList<FeedbackWithUserDto>()
    }
}
