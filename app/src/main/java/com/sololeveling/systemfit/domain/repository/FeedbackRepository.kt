package com.sololeveling.systemfit.domain.repository

import com.sololeveling.systemfit.data.remote.model.FeedbackDto
import com.sololeveling.systemfit.data.remote.model.FeedbackWithUserDto

interface FeedbackRepository {
    suspend fun submitFeedback(userId: String, category: String, content: String, deviceInfo: String)
    suspend fun updateFeedback(id: String, content: String)
    suspend fun deleteFeedback(id: String)
    suspend fun getAllFeedback(): List<FeedbackWithUserDto>
}
