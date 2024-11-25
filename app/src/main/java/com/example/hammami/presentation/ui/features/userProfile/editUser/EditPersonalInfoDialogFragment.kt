package com.example.hammami.presentation.ui.features.userProfile.editUser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.R
import com.example.hammami.databinding.DialogEditPersonalInfoBinding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.presentation.ui.activities.UserProfileViewModel.*
import com.example.hammami.util.hideKeyboard
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditPersonalInfoDialogFragment : DialogFragment() {
    private var _binding: DialogEditPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

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
        binding.apply {
            topAppBar.setNavigationOnClickListener { dismiss() }
            saveButton.setOnClickListener { onSaveButtonClick() }
            birthDateEditText.setOnClickListener { showDatePicker() }

            viewModel.uiState.value.user?.let { user ->
                firstNameEditText.setText(user.firstName)
                lastNameEditText.setText(user.lastName)
                birthDateEditText.setText(user.birthDate)
                genderAutoCompleteTextView.setText(user.gender, false)
                allergiesEditText.setText(user.allergies)
                disabilitiesEditText.setText(user.disabilities)
            }
        }

        setupTextChangeListeners()
    }


    private fun setupTextChangeListeners() {
        listOf(
            binding.firstNameEditText,
            binding.lastNameEditText,
            binding.birthDateEditText,
            binding.genderAutoCompleteTextView,
            binding.allergiesEditText,
            binding.disabilitiesEditText
        ).forEach {
            it.addTextChangedListener { binding.saveButton.isVisible = true }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { updateUI(it) } }
                launch { viewModel.events.collect { handleEvent(it) } }
            }
        }
    }

    private fun updateUI(state: UiState) {
        updateUIWithErrors(state.validationState)
        showLoading(state.isLoading)
    }


    private fun updateUIWithErrors(validationState: ValidationState) {
        binding.apply {
            firstNameInputLayout.error = validationState.firstNameError?.asString(requireContext())
            lastNameInputLayout.error = validationState.lastNameError?.asString(requireContext())
            birthDateInputLayout.error = validationState.birthDateError?.asString(requireContext())
            genderTextInputLayout.error = validationState.genderError?.asString(requireContext())
        }
    }


    private fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowSnackbar -> {
                showSnackbar(event.message.asString(requireContext()))
                dismiss()
            }
            else -> {}
        }
    }

    private fun onSaveButtonClick() {
        hideKeyboard()
        val userInfo = UserInfo(
            firstName = binding.firstNameEditText.text.toString(),
            lastName = binding.lastNameEditText.text.toString(),
            birthDate = binding.birthDateEditText.text.toString(),
            gender = binding.genderAutoCompleteTextView.text.toString(),
            allergies = binding.allergiesEditText.text.toString(),
            disabilities = binding.disabilitiesEditText.text.toString(),
            phoneNumber = viewModel.uiState.value.user?.phoneNumber ?: "",
            email = viewModel.uiState.value.user?.email ?: ""
        )
        viewModel.onEvent(UserProfileEvent.UpdateUserInfo(userInfo))
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.select_date))
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = Date(selection)
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.birthDateEditText.setText(format.format(date))
        }

        datePicker.show(parentFragmentManager, "DATE_PICKER")
    }

    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            linearProgressIndicator.isVisible = isLoading
            contentScrollView.isEnabled = !isLoading
            saveButton.isEnabled = !isLoading
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EditPersonalInfoDialogFragment()
    }
}