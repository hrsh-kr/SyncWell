package com.example.syncwell.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()

        // 1) Build a PersistentCacheSettings (implements LocalCacheSettings)
        val localCacheSettings = PersistentCacheSettings
            .newBuilder()
            .setSizeBytes(100L * 1024 * 1024) // 100 MB
            .build()

        // 2) Plug it into the FirestoreSettings.Builder
        val settings = FirebaseFirestoreSettings
            .Builder()
            .setLocalCacheSettings(localCacheSettings)
            .build()

        firestore.firestoreSettings = settings
        return firestore
    }
}
