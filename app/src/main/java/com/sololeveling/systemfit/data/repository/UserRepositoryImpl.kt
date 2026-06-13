package com.sololeveling.systemfit.data.repository

import com.sololeveling.systemfit.data.local.dao.UserDao
import com.sololeveling.systemfit.data.local.entity.UserEntity
import com.sololeveling.systemfit.data.remote.DataSource.RemoteSyncSource
import com.sololeveling.systemfit.domain.model.User
import com.sololeveling.systemfit.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val remoteSyncSource: RemoteSyncSource
) : UserRepository {

    override fun getUserStream(userId: String): Flow<User?> {
        return userDao.getUserStream(userId).map { entity ->
            entity?.toDomainModel()
        }
    }

    override suspend fun getUser(userId: String): User? {
        return userDao.getUser(userId)?.toDomainModel()
    }

    override suspend fun saveUser(user: User) {
        val entity = user.toEntity()
        userDao.insertUser(entity)
        // Sync to remote in background
        remoteSyncSource.syncUser(user)
    }

    private fun UserEntity.toDomainModel() = User(
        id = id,
        level = level,
        currentXp = currentXp,
        str = str,
        vit = vit,
        agi = agi,
        availableStatPoints = availableStatPoints,
        currentStreak = currentStreak
    )

    private fun User.toEntity() = UserEntity(
        id = id,
        level = level,
        currentXp = currentXp,
        str = str,
        vit = vit,
        agi = agi,
        availableStatPoints = availableStatPoints,
        currentStreak = currentStreak
    )
}
