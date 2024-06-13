package com.example.hammami.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module

// con in Singleton il componente rimane vivo per tutta la durata dell'applicazione
@InstallIn(
    SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    // in questo modo iniettiamo la dipendenza nel costruttore di una variabile di tipo FirebaseAuth
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()
}
