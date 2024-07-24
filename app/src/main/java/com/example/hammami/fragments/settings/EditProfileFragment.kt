package com.example.hammami.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hammami.R
import com.example.hammami.databinding.DialogEditPersonalInfoBinding
import com.example.hammami.models.User
import com.example.hammami.util.RegisterFieldsState
import com.example.hammami.util.RegisterValidation
import com.example.hammami.util.Resource
import com.example.hammami.viewmodel.EditProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class EditProfileFragment : DialogFragment() {

    private var _binding: DialogEditPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.topAppBar.setNavigationOnClickListener {
            dismiss()
        }

        binding.saveButton.setOnClickListener {
            saveUserData()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userState.collectLatest { userResource ->
                when (userResource) {
                    is Resource.Success -> {
                        showLoading(false)
                        userResource.data?.let { user ->
                            updateUserInfo(user)
                        }
                    }

                    is Resource.Loading -> showLoading(true)
                    is Resource.Error -> {
                        showLoading(false)
                        showError(userResource.message ?: getString(R.string.unknown_error))
                    }

                    is Resource.Unspecified -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.validationState.collectLatest { state ->
                state?.let { showValidationErrors(it) }
            }
        }
    }

    private fun updateUserInfo(user: User) {
        with(user) {
            val (day, month, year) = birthDate.toFormattedDate()
            with(binding) {
                firstNameEditText.setText(firstName)
                lastNameEditText.setText(lastName)
                dayEditText.setText(day)
                monthAutoCompleteTextView.setText(month, false)
                yearEditText.setText(year)
                genderAutoCompleteTextView.setText(gender, false)
                allergiesEditText.setText(allergies)
                disabilitiesEditText.setText(disabilities)
            }
        }

    }

    private fun String.toFormattedDate(): Triple<String, String, String> {
        val parts = this.split("/")
        return Triple(parts.getOrNull(0) ?: "", parts.getOrNull(1) ?: "", parts.getOrNull(2) ?: "")
    }

    private fun saveUserData() {
        with(binding) {
            val updatedUser = User(
                firstName = firstNameEditText.text.toString(),
                lastName = lastNameEditText.text.toString(),
                birthDate = "${dayEditText.text}/${monthAutoCompleteTextView.text}/${yearEditText.text}",
                gender = genderAutoCompleteTextView.text.toString(),
                allergies = allergiesEditText.text.toString(),
                disabilities = disabilitiesEditText.text.toString(),
            )

            viewModel.validateAndUpdateUser(updatedUser)
        }
    }

    private fun showValidationErrors(state: RegisterFieldsState) {
        with(binding) {
            firstNameInputLayout.error = (state.firstName as? RegisterValidation.Failed)?.message
            lastNameInputLayout.error = (state.lastName as? RegisterValidation.Failed)?.message
            // Show other validation errors...
        }
    }

    private fun showLoading(isLoading: Boolean) {
        //   binding.progressBar.isVisible = isLoading
        // binding.contentLayout.isVisible = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}