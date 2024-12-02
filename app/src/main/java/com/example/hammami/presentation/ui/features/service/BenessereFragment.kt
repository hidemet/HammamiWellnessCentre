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
import com.example.hammami.databinding.FragmentBenessereBinding
import com.example.hammami.presentation.ui.adapters.BenessereAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.service.BenessereViewModel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BenessereFragment: BaseFragment() {
    //private lateinit var binding: FragmentBenessereBinding
    //private lateinit var bindingViewPager: FragmentServiziBenessereBinding //non ne sono sicuro, è una prova
    private lateinit var benessereAdapter: BenessereAdapter
    //private val viewModel by viewModels<BenessereViewModel>()

    private var _binding: com.example.hammami.databinding.FragmentBenessereBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BenessereViewModel by viewModels()
    //private val benessereAdapter by lazy { createActiveCouponsAdapter() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBenessereBinding.inflate(inflater, container, false)
        //bindingViewPager = FragmentServiziBenessereBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoriesFragments = arrayListOf<Fragment>(
            BenessereFragment()
        )

        /*
        val viewPager2Adapter =
            BenessereViewpagerAdapter(categoriesFragments, childFragmentManager, lifecycle)
        binding.viewpagerBenessere.orientation = ViewPager2.ORIENTATION_VERTICAL
        binding.viewpagerBenessere.adapter = viewPager2Adapter
         */

        //setupRecyclerView()
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
        viewModel.allBenessere.collectLatest { state ->
            updateUI(state)
        }
    }

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            handleEvent(event)
        }
    }

    private fun updateUI(state: BenessereServices) = with(binding) {
        //progressIndicator.isVisible = state.isLoading
        updateBenessereList(state)
    }

    private fun updateBenessereList(state: BenessereServices) = with(binding) {
        benessereAdapter.submitList(state.servicesBenessere)
    }


    private fun setupInitialState() = with(binding) {
        binding.mainProgressBar.visibility = View.GONE
    }


    private fun setupAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun handleEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ShowMessage -> showSnackbar(event.message)
            is UiEvent.ShowError -> showSnackbar(event.message)
            else -> {}
        }
    }

    private fun hideLoading() {
        binding.mainProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainProgressBar.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        benessereAdapter = BenessereAdapter()
        binding.rvBenessere.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = benessereAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("BenessereFragment", "onResume")
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("BenessereFragment", "onPause")
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