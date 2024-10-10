package com.example.hammami.data.datasource.user

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageUserDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadProfileImage(imageUri: Uri): String {
        try {
            val filename = UUID.randomUUID().toString()
            val ref = storage.reference.child("profile_images/$filename")
            ref.putFile(imageUri).await()
            return ref.downloadUrl.await().toString()
        } catch (e: StorageException) {
            throw e
        }
    }
}