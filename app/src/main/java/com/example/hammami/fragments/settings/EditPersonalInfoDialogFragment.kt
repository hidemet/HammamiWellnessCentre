package com.example.hammami.fragments.settings

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.hammami.R
import com.example.hammami.databinding.DialogEditPersonalInfoBinding
import com.example.hammami.models.User
import com.example.hammami.util.Resource
import com.example.hammami.util.StringValidators
import com.example.hammami.viewmodel.EditUserProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditPersonalInfoDialogFragment : DialogFragment() {

    private var _binding: DialogEditPersonalInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditUserProfileViewModel by viewModels()

    private var isDataModified = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        Log.d("EditPersonalInfoDialog", "onCreate called")

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        Log.d("EditPersonalInfoDialog", "onCreateDialog called")
        return dialog
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
        Log.d("EditPersonalInfoDialog", "onViewCreated called")


        setupUI()
        observeViewModel()
        setupTextChangeListeners()
    }

    private fun setupUI() {
        binding.saveButton.isEnabled = false
        binding.saveButton.setOnClickListener { onSaveButtonClick() }
        binding.topAppBar.setNavigationOnClickListener { dismiss() }
    }

    private fun setupTextChangeListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (!isDataModified) {
                    isDataModified = true
                    binding.saveButton.visibility = View.VISIBLE
                }
                binding.saveButton.isEnabled = true

            }
        }

        binding.apply {
            firstNameEditText.addTextChangedListener(textWatcher)
            lastNameEditText.addTextChangedListener(textWatcher)
            dayEditText.addTextChangedListener(textWatcher)
            monthAutoCompleteTextView.addTextChangedListener(textWatcher)
            yearEditText.addTextChangedListener(textWatcher)
            genderAutoCompleteTextView.addTextChangedListener(textWatcher)
            allergiesEditText.addTextChangedListener(textWatcher)
            disabilitiesEditText.addTextChangedListener(textWatcher)
        }
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
                        showSuccessAndDismiss()

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

    private fun showSuccessAndDismiss() {
        Snackbar.make(
            binding.root,
            getString(R.string.profile_updated_successfully),
            Snackbar.LENGTH_SHORT
        ).show()
        dismiss()
    }

    private fun updateUIWithUserData(user: User) {
        binding.apply {
            firstNameEditText.setText(user.firstName)
            lastNameEditText.setText(user.lastName)
            val (day, month, year) = user.birthDate.split("/")
            dayEditText.setText(day)
            yearEditText.setText(year)

            // Set month
            val months = resources.getStringArray(R.array.month_array)
            val monthIndex = months.indexOf(month)
            if (monthIndex != -1) {
                monthAutoCompleteTextView.listSelection = monthIndex
                monthAutoCompleteTextView.setText(months[monthIndex], false)
            }

            // Set gender
            val genders = resources.getStringArray(R.array.gender_array)
            val genderIndex = genders.indexOf(user.gender)
            if (genderIndex != -1) {
                genderAutoCompleteTextView.listSelection = genderIndex
                genderAutoCompleteTextView.setText(genders[genderIndex], false)
            }

            allergiesEditText.setText(user.allergies)
            disabilitiesEditText.setText(user.disabilities)
        }
        isDataModified = false
        binding.saveButton.visibility = View.GONE
    }

    private fun onSaveButtonClick() {
        if (validateAllFields()) {
            val updatedUser = createUpdatedUser()
            viewModel.updateUser(updatedUser)
            binding.saveButton.visibility = View.GONE
            binding.saveButton.isEnabled = false
        }
    }

    private fun validateAllFields(): Boolean {
        val isFirstNameValid =
            ValidationUtil.validateField(binding.firstNameInputLayout, StringValidators.NotBlank)
        val isLastNameValid =
            ValidationUtil.validateField(binding.lastNameInputLayout, StringValidators.NotBlank)
        val isBirthDateValid = ValidationUtil.validateBirthDate(
            binding.dayInputLayout,
            binding.monthInputLayout,
            binding.yearInputLayout,
            binding.dataErrorTextView
        )
        val isGenderValid =
            ValidationUtil.validateField(binding.genderTextInputLayout, StringValidators.NotBlank)

        return isFirstNameValid && isLastNameValid && isBirthDateValid && isGenderValid
    }

    private fun createUpdatedUser(): User {
        val birthDate =
            "${binding.dayEditText.text}/${binding.monthAutoCompleteTextView.text}/${binding.yearEditText.text}"
        return User(
            firstName = binding.firstNameEditText.text.toString(),
            lastName = binding.lastNameEditText.text.toString(),
            birthDate = birthDate,
            gender = binding.genderAutoCompleteTextView.text.toString(),
            allergies = binding.allergiesEditText.text.toString(),
            disabilities = binding.disabilitiesEditText.text.toString()
        )
    }

    private fun showLoading(isLoading: Boolean) {
        // Implement your loading UI logic here
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}