package com.sololeveling.systemfit.data.remote.DataSource

import com.google.firebase.firestore.FirebaseFirestore
import com.sololeveling.systemfit.data.remote.model.FirestoreUserDto
import com.sololeveling.systemfit.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RemoteSyncSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun syncUser(user: User) {
        try {
            val dto = FirestoreUserDto(
                id = user.id,
                level = user.level,
                currentXp = user.currentXp,
                str = user.str,
                vit = user.vit,
                agi = user.agi,
                availableStatPoints = user.availableStatPoints,
                currentStreak = user.currentStreak
            )
            firestore.collection("users").document(user.id).set(dto).await()
        } catch (e: Exception) {
            // Silently fail remote sync for offline capability
            e.printStackTrace()
        }
    }
}
