package com.example.hammami.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.hammami.R
import com.example.hammami.adapters.ProfileOptionAdapter
import com.example.hammami.databinding.FragmentProfileBinding
import com.example.hammami.models.ItemProfileOption
import com.example.hammami.models.User
import com.example.hammami.util.Resource
import com.example.hammami.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModelStates()
    }

    private fun setupUI() {
        setupOptionsList()
        with(binding) {
            topAppBar.setNavigationOnClickListener {
                findNavController().popBackStack()
            }
            editProfileButton.setOnClickListener {
                //EditProfileFragment().show(parentFragmentManager, "EditProfileFragment")
            }
        }
    }

    private fun setupOptionsList() {
        val options = listOf(
            ItemProfileOption(
                "Gift Card",
                R.drawable.ic_gift_card,
                navigationDestination = R.id.giftCardsFragment
            )
        )

        binding.optionsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ProfileOptionAdapter(options)
        }
    }

    private fun observeViewModelStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collectLatest { userResource ->
                when (userResource) {
                    is Resource.Loading -> showLoading(true)
                    is Resource.Success -> {
                        showLoading(false)
                        userResource.data?.let { user ->
                            updateUIWithUserData(user)
                        }
                    }

                    is Resource.Error -> {
                        showLoading(false)
                        showErrorWithRetry(
                            userResource.message ?: getString(R.string.unknown_error)
                        )
                    }

                    is Resource.Unspecified -> Unit

                }
            }
        }
    }

    private fun updateUIWithUserData(user: User) {
        binding.apply {
            userName.text = "${user.firstName} ${user.lastName}"
            userPoints.text = "${user.points} punti"

            // Carica l'immagine dell'utente usando Glide
            Glide.with(this@ProfileFragment)
                .load(user.profileImage) // URL o percorso dell'immagine
                .error(R.drawable.ic_profile) // Immagine di fallback in caso di errore
                .into(profileImage) // ImageView dove verrà caricata l'immagine
        }
    }


    private fun showLoading(isLoading: Boolean) {
        binding.linearProgressIndicator.isVisible = isLoading
        binding.contentScrollView.isVisible = !isLoading
    }


    private fun showErrorWithRetry(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction(getString(R.string.retry)) {
                viewModel.refreshUser()
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}