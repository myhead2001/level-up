package com.sololeveling.systemfit.di

import com.sololeveling.systemfit.data.repository.ExerciseRepositoryImpl
import com.sololeveling.systemfit.data.repository.UserRepositoryImpl
import com.sololeveling.systemfit.domain.repository.ExerciseRepository
import com.sololeveling.systemfit.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    abstract fun bindExerciseRepository(
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): ExerciseRepository
}
