package com.example.hammami.di

import com.example.hammami.util.PreferencesManager
import android.content.Context
import com.example.hammami.database.FirebaseDb
import com.example.hammami.models.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
// Questo modulo fornisce le dipendenze che saranno disponibili per tutta la durata dell'applicazione.
// Le dipendenze fornite qui saranno disponibili in tutte le attività e i servizi dell'applicazione.
@InstallIn(SingletonComponent::class)
object AppModule {

    // Questo metodo fornisce un'istanza di FirebaseAuth. Questa istanza sarà un singleton,
    // cioè ne esisterà una sola per tutta l'applicazione. Questo metodo sarà chiamato da Dagger
    // ogni volta che un'istanza di FirebaseAuth sarà richiesta come dipendenza.
    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    // In questo modo, quando Dagger vede che abbiamo bisogno di un'istanza di FirebaseAuth in un ViewModel,
    // troverà questo metodo nel modulo e lo chiamerà per ottenere l'istanza di FirebaseAuth.
    // Questo è il concetto di iniezione delle dipendenze.

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDb(auth: FirebaseAuth, firestore: FirebaseFirestore): FirebaseDb {
        return FirebaseDb(auth, firestore)
    }


    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }


    @Provides
    @Singleton
    fun provideUserRepository(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        preferencesManager: PreferencesManager
    ): UserRepository = UserRepository(firebaseAuth,firestore, preferencesManager)

    @Provides
    @Singleton
    fun provideStorageReference(): StorageReference = FirebaseStorage.getInstance().reference

}