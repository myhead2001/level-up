package com.sololeveling.systemfit.di

import android.content.Context
import com.sololeveling.systemfit.data.local.SystemDatabase
import com.sololeveling.systemfit.data.local.dao.ExerciseDao
import com.sololeveling.systemfit.data.local.dao.UserDao
import com.sololeveling.systemfit.data.local.dao.WorkoutLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSystemDatabase(@ApplicationContext context: Context): SystemDatabase {
        return SystemDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: SystemDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideExerciseDao(database: SystemDatabase): ExerciseDao {
        return database.exerciseDao()
    }

    @Provides
    fun provideWorkoutLogDao(database: SystemDatabase): WorkoutLogDao {
        return database.workoutLogDao()
    }
}
