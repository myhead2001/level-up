package com.sololeveling.systemfit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://htlxgfzyfldtzwgzxbto.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh0bHhnZnp5ZmxkdHp3Z3p4YnRvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODI5Njg3OTYsImV4cCI6MjA5ODU0NDc5Nn0.Vi6hkuf9puVhqWRui3j2xhMiEFxcXDpI7Q01IwdH0ZY"
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}
