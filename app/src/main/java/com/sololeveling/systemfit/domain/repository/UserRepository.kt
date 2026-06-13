package com.sololeveling.systemfit.domain.repository

import com.sololeveling.systemfit.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserStream(userId: String): Flow<User?>
    suspend fun getUser(userId: String): User?
    suspend fun saveUser(user: User)
}
