package com.example.hammami.di

import android.app.Application
import android.content.Context
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hammami.R
import com.example.hammami.core.validator.AndroidEmailPatternValidator
import com.example.hammami.core.validator.EmailPatternValidator
import com.example.hammami.data.datasource.auth.FirebaseAuthDataSource
import com.example.hammami.data.repositories.AuthRepository
import com.example.hammami.data.repositories.UserRepository
import com.example.hammami.domain.usecase.auth.DeleteAccountUseCase
import com.example.hammami.domain.usecase.auth.ResetPasswordUseCase
import com.example.hammami.domain.usecase.auth.SignInUseCase
import com.example.hammami.domain.usecase.auth.SignUpUseCase
import com.example.hammami.domain.usecase.auth.CheckAuthStateUseCase
import com.example.hammami.domain.usecase.user.UploadUserImageUseCase
import com.example.hammami.domain.usecase.validation.account.ValidateConfirmedPasswordUseCase
import com.example.hammami.domain.usecase.validation.account.ValidateEmailUseCase
import com.example.hammami.domain.usecase.validation.account.ValidatePasswordUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateBirthDateUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateFirstNameUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateGenderUseCase
import com.example.hammami.domain.usecase.validation.user.ValidateLastNameUseCase
import com.example.hammami.domain.usecase.validation.user.ValidatePhoneNumberUseCase
import com.example.hammami.core.utils.ClipboardManager
import com.example.hammami.domain.usecase.user.GetCurrentUserIdUseCase
import com.example.hammami.core.utils.PreferencesManager
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
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions().placeholder(R.drawable.ic_launcher_foreground)
            .error(R.drawable.ic_launcher_foreground)
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.DATA)
    )


    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth() = FirebaseAuth.getInstance()

    @Provides
    fun provideGetCurrentUserIdUseCase(authRepository: AuthRepository): GetCurrentUserIdUseCase {
        return GetCurrentUserIdUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthDataSource(
        auth: FirebaseAuth
    ): FirebaseAuthDataSource = FirebaseAuthDataSource(auth)

    @Provides
    fun provideIntroductionSP(
        application: Application
    ) = application.getSharedPreferences("introduction", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideClipboardManager(@ApplicationContext context: Context): ClipboardManager {
        return ClipboardManager(context)
    }

    @Provides
    @Singleton
    fun provideStorageReference(): StorageReference = FirebaseStorage.getInstance().reference

    @Provides
    @Singleton
    fun provideValidateFirstNameUseCase(): ValidateFirstNameUseCase = ValidateFirstNameUseCase()

    @Provides
    @Singleton
    fun provideValidateLastNameUseCase(): ValidateLastNameUseCase = ValidateLastNameUseCase()

    @Provides
    @Singleton
    fun provideValidateBirthDateUseCase(): ValidateBirthDateUseCase = ValidateBirthDateUseCase()

    @Provides
    @Singleton
    fun provideValidateGenderUseCase(): ValidateGenderUseCase = ValidateGenderUseCase()

    @Provides
    @Singleton
    fun provideValidateEmailUseCase(validator: AndroidEmailPatternValidator): ValidateEmailUseCase =
        ValidateEmailUseCase(validator)

    @Provides
    @Singleton
    fun provideValidatePhoneNumberUseCase(): ValidatePhoneNumberUseCase =
        ValidatePhoneNumberUseCase()

    @Provides
    @Singleton
    fun provideValidatePasswordUseCase(): ValidatePasswordUseCase = ValidatePasswordUseCase()

    @Provides
    @Singleton
    fun provideValidateConfirmedPasswordUseCase(): ValidateConfirmedPasswordUseCase =
        ValidateConfirmedPasswordUseCase()



//    @Provides
//    fun provideUpdateUserProfileUseCase(userRepository: UserRepository): UpdateUserUseCase {
//        return UpdateUserUseCase(userRepository)
//    }

    @Provides
    fun provideUploadUserImageUseCase(userRepository: UserRepository): UploadUserImageUseCase {
        return UploadUserImageUseCase(userRepository)
    }

    @Provides
    fun provideDeleteUserUseCase(userRepository: UserRepository): DeleteAccountUseCase {
        return DeleteAccountUseCase(userRepository)
    }


    @Provides
    @Singleton
    fun provideCheckAuthStateUseCase(
        authRepository: AuthRepository
    ): CheckAuthStateUseCase {
        return CheckAuthStateUseCase(authRepository)
    }

    @Provides
    fun provideSignInUseCase(authRepository: AuthRepository): SignInUseCase {
        return SignInUseCase(authRepository)
    }

    @Provides
    fun provideResetPasswordUseCase(authRepository: AuthRepository): ResetPasswordUseCase {
        return ResetPasswordUseCase(authRepository)
    }


    @Provides
    fun provideSignUpUseCase(userRepository: UserRepository): SignUpUseCase {
        return SignUpUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideEmailPatternValidator(): EmailPatternValidator {
        return AndroidEmailPatternValidator()
    }
}