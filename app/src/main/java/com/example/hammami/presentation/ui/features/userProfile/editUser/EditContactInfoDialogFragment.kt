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
import com.example.hammami.databinding.DialogEditContactInfoBinding
import com.example.hammami.presentation.ui.activities.UserProfileViewModel
import com.example.hammami.presentation.ui.activities.UserProfileViewModel.*
import com.example.hammami.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.UserInfo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class EditContactInfoDialogFragment : DialogFragment() {
    private var _binding: DialogEditContactInfoBinding? = null
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
        _binding = DialogEditContactInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { dismiss() }
            saveButton.setOnClickListener { onSaveButtonClick() }

            viewModel.uiState.value.user?.let { user ->
                phoneNumberEditText.setText(user.phoneNumber)
                emailEditText.setText(user.email)
            }
            setupTextChangeListeners()
        }
    }

    private fun setupTextChangeListeners() {
        listOf(binding.phoneNumberEditText, binding.emailEditText).forEach {
            it.addTextChangedListener { binding.saveButton.isVisible = true }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { updateUI(it) } }
                launch { viewModel.uiEvents.collect { handleEvent(it) } }
            }
        }
    }

    private fun updateUI(state: UiState) {
        showLoading(state.isLoading)
        state.userValidationError?.let { validation ->
            with(binding) {
                phoneNumberInputLayout.error =
                    validation.phoneNumberError?.asString(requireContext())
                emailInputLayout.error = validation.emailError?.asString(requireContext())
            }
        }
    }

    private fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.UserMessage -> {
                showSnackbar(event.message.asString(requireContext()))
                dismiss()
            }
            else -> Unit
        }
    }


    private fun onSaveButtonClick() {
        hideKeyboard()
        val userInfo = UserData.ContactInfoData(
            phoneNumber = binding.phoneNumberEditText.text.toString(),
            email = binding.emailEditText.text.toString()
        )
        viewModel.updateUserData(userInfo)
    }


    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            linearProgressIndicator.isVisible = isLoading
            contentScrollView.isVisible = !isLoading
            saveButton.isEnabled = !isLoading
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = EditContactInfoDialogFragment()
    }
}