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
import com.example.hammami.util.StringValidators
import com.example.hammami.viewmodel.EditProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : DialogFragment() {

    private var _binding: DialogEditPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileViewModel by viewModels()

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var dayEditText: TextInputEditText
    private lateinit var monthEditText: TextInputEditText // Added declaration
    private lateinit var yearEditText: TextInputEditText
    private lateinit var genderEditText: TextInputEditText // Added declaration
    private lateinit var allergiesEditText: TextInputEditText
    private lateinit var disabilitiesEditText: TextInputEditText

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
        firstNameEditText = binding.firstNameInputLayout.editText as TextInputEditText
        lastNameEditText = binding.lastNameInputLayout.editText as TextInputEditText
        dayEditText = binding.dayInputLayout.editText as TextInputEditText
        monthEditText = binding.monthInputLayout.editText as TextInputEditText // Correctly initialized
        yearEditText = binding.yearInputLayout.editText as TextInputEditText
        genderEditText = binding.genderTextInputLayout.editText as TextInputEditText // Correctly initialized
        allergiesEditText = binding.allergiesTextInputLayout.editText as TextInputEditText
        disabilitiesEditText = binding.disabilitiesTextInputLayout.editText as TextInputEditText

        binding.saveButton.setOnClickListener { onSaveButtonClick() }
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.userState.collectLatest { userResource ->
                when (userResource) {
                    is Resource.Success -> {
                        showLoading(false)
                        userResource.data?.let { user ->
                            updateUIWithUserData(user)
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
    }

    private fun updateUIWithUserData(user: User) {
        binding.apply {
            firstNameEditText.setText(user.firstName)
            lastNameEditText.setText(user.lastName)
            val (day, month, year) = user.birthDate.split("/")
            dayEditText.setText(day)
            monthEditText.setText(month)
            yearEditText.setText(year)
            genderEditText.setText(user.gender)
            allergiesEditText.setText(user.allergies)
            disabilitiesEditText.setText(user.disabilities)
        }
    }

    private fun onSaveButtonClick() {
        if (validateAllFields()) {
            val updatedUser = createUpdatedUser()
            viewModel.updateUser(updatedUser)
        }
    }

    private fun onBackClick() {
        dismiss()
    }

    private fun validateAllFields(): Boolean {
        val isFirstNameValid = ValidationUtil.validateField(binding.firstNameInputLayout, StringValidators.NotBlank)
        val isLastNameValid = ValidationUtil.validateField(binding.lastNameInputLayout, StringValidators.NotBlank)
        val isBirthDateValid = ValidationUtil.validateBirthDate(
            binding.dayInputLayout,
            binding.monthInputLayout,
            binding.yearInputLayout,
            binding.dataErrorTextView
        )
        val isGenderValid = ValidationUtil.validateField(binding.genderTextInputLayout, StringValidators.NotBlank)

        return isFirstNameValid && isLastNameValid && isBirthDateValid && isGenderValid
    }

    private fun createUpdatedUser(): User {
        val birthDate = "${dayEditText.text}/${monthEditText.text}/${yearEditText.text}"
        return User(
            firstName = firstNameEditText.text.toString(),
            lastName = lastNameEditText.text.toString(),
            birthDate = birthDate,
            gender = genderEditText.text.toString(),
            allergies = allergiesEditText.text.toString(),
            disabilities = disabilitiesEditText.text.toString()
        )
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
