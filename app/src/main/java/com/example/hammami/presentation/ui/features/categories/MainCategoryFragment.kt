package com.example.hammami.presentation.ui.features.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.presentation.ui.adapters.NewServicesAdapter
import com.example.hammami.domain.model.Service
import com.example.hammami.databinding.FragmentMainCategoryBinding
import com.example.hammami.domain.error.DataError
import com.example.hammami.presentation.ui.adapters.BestDealsAdapter
import com.example.hammami.presentation.ui.adapters.RecommendedAdapter
import com.example.hammami.core.result.Result
import com.example.hammami.presentation.ui.adapters.MainCategoryAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import com.example.hammami.presentation.ui.features.service.BenessereFragment
import com.example.hammami.presentation.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "MainCategoryFragment"


@AndroidEntryPoint
class MainCategoryFragment: BaseFragment() {

    private lateinit var bestDealsAdapter: MainCategoryAdapter
    private lateinit var recommendedAdapter: MainCategoryAdapter
    private lateinit var newServicesAdapter: MainCategoryAdapter

    private var _binding: FragmentMainCategoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainCategoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainCategoryBinding.inflate(inflater, container, false)
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
        //setupAppBar()
        setupRecyclerView()
        setupInitialState()
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeBestDealsState() }
                launch { observeNewServicesState() }
                launch { observeRecommendedState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeBestDealsState() {
        viewModel.allBestDeals.collectLatest { state ->
            updateBestDealsList(state)
        }
    }

    private suspend fun observeNewServicesState() {
        viewModel.allNewServices.collectLatest { state ->
            updateNewServicesList(state)
        }
    }

    private suspend fun observeRecommendedState() {
        viewModel.allRecommended.collectLatest { state ->
            updateRecommendedList(state)
        }
    }

    /*
    private suspend fun observeState() {
        viewModel.allBestDeals.collectLatest { state ->
            updateUI(state)
        }

        viewModel.allNewServices.collectLatest { state ->
            updateUI(state)
        }

        viewModel.allRecommended.collectLatest { state ->
            updateUI(state)
        }
    }
     */

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            handleEvent(event)
        }
    }

    private fun updateUI(state: MainCategoryViewModel.MainCategoryServices) = with(binding) {
        //progressIndicator.isVisible = state.isLoading
        updateBestDealsList(state)
        updateNewServicesList(state)
        updateRecommendedList(state)
    }

    private fun updateBestDealsList(state: MainCategoryViewModel.MainCategoryServices) = with(binding) {
        bestDealsAdapter.submitList(state.servicesBestDeals)
    }

    private fun updateNewServicesList(state: MainCategoryViewModel.MainCategoryServices) = with(binding) {
        newServicesAdapter.submitList(state.servicesNewServices)
    }

    private fun updateRecommendedList(state: MainCategoryViewModel.MainCategoryServices) = with(binding) {
        recommendedAdapter.submitList(state.servicesRecommended)
    }

    private fun setupInitialState() = with(binding) {
        binding.mainProgressBar.visibility = View.GONE
    }

    private fun handleEvent(event: MainCategoryViewModel.UiEvent) {
        when (event) {
            is MainCategoryViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
            is MainCategoryViewModel.UiEvent.ShowError -> showSnackbar(event.message)
            else -> {}
        }
    }

    private fun hideLoading() {
        binding.mainProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainProgressBar.visibility = View.VISIBLE
    }

    private fun setupBestDealsRecyclerView() {
        bestDealsAdapter = MainCategoryAdapter()
        binding.rvBestDeals.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bestDealsAdapter
        }
    }

    private fun setupNewServicesRecyclerView() {
        newServicesAdapter = MainCategoryAdapter()
        binding.rvNewServices.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newServicesAdapter
        }
    }

    private fun setupRecommendedRecyclerView() {
        recommendedAdapter = MainCategoryAdapter()
        binding.rvRecommended.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedAdapter
        }
    }

    private fun setupRecyclerView() {
        setupBestDealsRecyclerView()
        setupNewServicesRecyclerView()
        setupRecommendedRecyclerView()
    }


    override fun onResume() {
        super.onResume()
        Log.d("MainCategoryFragment", "onResume")
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainCategoryFragment", "onPause")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
