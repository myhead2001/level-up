package com.sololeveling.systemfit.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.sololeveling.systemfit.data.local.SystemDatabase
import com.sololeveling.systemfit.data.local.entity.UserEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var db: SystemDatabase
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, SystemDatabase::class.java
        ).build()
        userDao = db.userDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndGetUser() = runTest {
        val user = UserEntity("player_1", level = 2, currentXp = 50, str = 12, vit = 10, agi = 10, availableStatPoints = 1, currentStreak = 2)
        userDao.insertUser(user)

        val loaded = userDao.getUser("player_1")
        assertThat(loaded?.level).isEqualTo(2)
        assertThat(loaded?.str).isEqualTo(12)
    }

    @Test
    fun getUserStreamEmitsUpdates() = runTest {
        val user = UserEntity("player_1", level = 2, currentXp = 50, str = 12, vit = 10, agi = 10, availableStatPoints = 1, currentStreak = 2)
        userDao.insertUser(user)

        userDao.getUserStream("player_1").test {
            val initial = awaitItem()
            assertThat(initial?.level).isEqualTo(2)

            val updatedUser = user.copy(level = 3)
            userDao.updateUser(updatedUser)

            val next = awaitItem()
            assertThat(next?.level).isEqualTo(3)
            
            cancelAndIgnoreRemainingEvents()
        }
    }
}
