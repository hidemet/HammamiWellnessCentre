package com.example.hammami.fragments.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.hammami.R
import com.example.hammami.databinding.FragmentEditUserProfileBinding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.User
import com.example.hammami.viewmodel.EditUserProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditUserProfileFragment : BaseFragment() {

    private var _binding: FragmentEditUserProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditUserProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackClick()
            }

            editPersonalInfoButton.setOnClickListener {
                Log.d("EditUserProfileFragment", "Edit Personal Info button clicked")
                openEditPersonalInfoDialog()
            }

            editContactsButton.setOnClickListener {
                openEditContactsDialog()
            }

            changePasswordButton.setOnClickListener {
                openChangePasswordDialog()
            }
        }
    }

    override fun observeFlows() {
        viewModel.userState.collectResource(
            onSuccess = { user ->
                updateUIWithUserData(user)
            },
            onError = { errorMessage ->
                showSnackbar(errorMessage ?: "An error occurred")
            },
            onLoading = {
                showLoading(true)
            },
            onComplete = {
                showLoading(false)
            }
        )
    }

    override fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentScrollView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun updateUIWithUserData(user: User) {
        binding.apply {
            firstName.text = user.firstName
            lastName.text = user.lastName
            birthDate.text = user.birthDate
            allergies.text = user.allergies.ifEmpty { "-" }
            disabilities.text = user.disabilities.ifEmpty { "-" }
            phoneNumberValue.text = user.phoneNumber
            emailAddressValue.text = user.email


            // Gestione dell'immagine del profilo con Glide

            if (user.profileImage.isNotEmpty()) {
                Glide.with(this@EditUserProfileFragment)
                    .load(user.profileImage)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(profileImageView) // Assicurati di avere un ImageView con questo id nel tuo layout
            } else {
                // Imposta un'immagine predefinita
                profileImageView.setImageResource(R.drawable.default_profile_image)
            }
        }


    }

    private fun openEditPersonalInfoDialog() {
        try {
            EditPersonalInfoDialogFragment().show(parentFragmentManager, "EditPersonalInfoDialog")
            Log.d("EditUserProfileFragment", "Dialog show() called")
        } catch (e: Exception) {
            Log.e("ProfileFragment", "Errore nel mostrare EditProfileFragment", e)
            showSnackbar("Errore nel mostrare la schermata di modifica delle informazioni personali")
        }
    }

    private fun openEditContactsDialog() {
        //EditContactsDialogFragment().show(parentFragmentManager, "EditContactsDialog")
    }

    private fun openChangePasswordDialog() {
        //  ChangePasswordDialogFragment().show(parentFragmentManager, "ChangePasswordDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}