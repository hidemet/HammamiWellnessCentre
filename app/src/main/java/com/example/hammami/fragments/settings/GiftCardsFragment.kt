package com.example.hammami.fragments.settings
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.adapters.GiftCardAdapter
import com.example.hammami.databinding.FragmentGiftCardsBinding
import com.example.hammami.viewmodel.GiftCardsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GiftCardsFragment : Fragment() {

    private var _binding: FragmentGiftCardsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GiftCardsViewModel by viewModels()

    private lateinit var giftCardAdapter: GiftCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGiftCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUI()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setUpUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { onBackButtonClick() }
        }
    }

    private fun setupRecyclerView() {
        giftCardAdapter = GiftCardAdapter(emptyList()) { value ->
            viewModel.onGiftCardSelected(value)
        }
        binding.giftCardRecyclerView.apply {
            adapter = giftCardAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.giftCardValues.collect { values ->
                    giftCardAdapter.updateGiftCards(values)
                }

            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { value ->
                    navigateToPayment(value)
                }
            }
        }
    }

    private fun onBackButtonClick() {
        findNavController().popBackStack()
    }
    private fun navigateToPayment(value: Int) {
        // Usa Navigation Component o il tuo metodo di navigazione preferito
        // Per esempio:
        // findNavController().navigate(GiftCardFragmentDirections.actionGiftCardFragmentToPaymentFragment(value))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}