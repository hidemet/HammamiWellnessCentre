package com.example.hammami.presentation.ui.features.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.databinding.FragmentMassaggiBinding
import com.example.hammami.presentation.ui.adapters.MassaggiAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MassaggiFragment: BaseFragment() {

    private lateinit var massaggiAdapter: MassaggiAdapter
    private var _binding: com.example.hammami.databinding.FragmentMassaggiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MassaggiViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMassaggiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            MassaggiFragment()
        )

    }

    override fun setupUI() {
        setupAppBar()
        setupRecyclerView()
        setupInitialState()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeState() {
        viewModel.allMassaggi.collectLatest { state ->
            updateUI(state)
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            handleEvent(event)
        }
    }

    private fun updateUI(state: MassaggiViewModel.MassaggiServices) = with(binding) {
        //progressIndicator.isVisible = state.isLoading
        updateMassaggiList(state)
    }

    private fun updateMassaggiList(state: MassaggiViewModel.MassaggiServices) = with(binding) {
        massaggiAdapter.submitList(state.servicesMassaggi)
    }


    private fun setupInitialState() = with(binding) {
        binding.mainProgressBar.visibility = View.GONE
    }


    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun handleEvent(event: MassaggiViewModel.UiEvent) {
        when (event) {
            is MassaggiViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
            is MassaggiViewModel.UiEvent.ShowError -> showSnackbar(event.message)
        }
    }

    private fun hideLoading() {
        binding.mainProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainProgressBar.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        massaggiAdapter = MassaggiAdapter()
        binding.rvMassaggi.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = massaggiAdapter
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("MassaggiFragment", "onResume")
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MassaggiFragment", "onPause")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*
override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View {
    binding = FragmentBenessereBinding.inflate(inflater)
    bindingViewPager = FragmentServiziBenessereBinding.inflate(inflater)
    return binding.root
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val categoriesFragments = arrayListOf<Fragment>(
        BenessereFragment()
    )

    val viewPager2Adapter =
        BenessereViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
    bindingViewPager.viewpagerBenessere.orientation = ViewPager2.ORIENTATION_VERTICAL
    bindingViewPager.viewpagerBenessere.adapter = viewPager2Adapter

    setupBenessereRv()
    lifecycleScope.launchWhenStarted {
        viewModel.allBenessere.collectLatest { result ->
            when (result) {
                /*
                is Result.Loading -> {
                    showLoading()
                 */

                is Result.Error -> {
                    //hideLoading()
                    Log.e(TAG, result.error.toString())
                }

                is Result.Success -> {
                    Log.d(TAG, "Received new services: ${result.data}")
                    benessereAdapter.differ.submitList(result.data)
                    //hideLoading()
                }
            }
        }
    }
}

private fun hideLoading() {
    binding.mainProgressBar.visibility = View.GONE
}

private fun showLoading() {
    binding.mainProgressBar.visibility = View.VISIBLE
}

private fun setupBenessereRv() {
    benessereAdapter = BenessereAdapter()
    binding.rvBenessere.apply{
        layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = benessereAdapter
    }
}

companion object {
    private const val TAG = "BenessereFragment"
}

 */


    /*
    private lateinit var binding: FragmentMassaggiBinding
    private lateinit var massaggiAdapter: MassaggiAdapter
    private val viewModel by viewModels<MassaggiViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMassaggiBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBenessereRv()
        lifecycleScope.launchWhenStarted{
            viewModel.allMassaggi.collectLatest {
                when (it){
                    is Resource.Loading -> {
                        showLoading()
                    }

                    is Resource.Error -> {
                        hideLoading()
                        Log.e(TAG, it.message.toString())
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    is Resource.Success -> {
                        Log.d(TAG, "Received new services: ${it.data}")
                        massaggiAdapter.differ.submitList(it.data)
                        hideLoading()
                    }
                    is Resource.Unspecified -> Unit
                }
            }
        }
    }

    private fun hideLoading() {
        binding.mainProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainProgressBar.visibility = View.VISIBLE
    }

    private fun setupBenessereRv() {
        massaggiAdapter = MassaggiAdapter()
        binding.rvMassaggi.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = massaggiAdapter
        }
    }

     */
