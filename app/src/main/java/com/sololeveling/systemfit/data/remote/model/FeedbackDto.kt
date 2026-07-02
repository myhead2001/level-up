package com.sololeveling.systemfit.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedbackDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val category: String = "",
    val content: String = "",
    @SerialName("device_info") val deviceInfo: String = "",
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class FeedbackWithUserDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val category: String = "",
    val content: String = "",
    @SerialName("device_info") val deviceInfo: String = "",
    @SerialName("created_at") val createdAt: String = "",
    val users: UserNameDto? = null
)

@Serializable
data class UserNameDto(
    val name: String = ""
)
