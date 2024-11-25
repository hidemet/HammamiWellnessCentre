package com.example.hammami.presentation.ui.features.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.domain.model.Service
import com.example.hammami.databinding.FragmentMainCategoryBinding
import com.example.hammami.domain.error.DataError
import com.example.hammami.presentation.ui.adapters.BestDealsAdapter
import com.example.hammami.presentation.ui.adapters.NewServicesAdapter
import com.example.hammami.presentation.ui.adapters.RecommendedAdapter
import com.example.hammami.core.result.Result
import com.example.hammami.presentation.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val TAG = "MainCategoryFragment"

@AndroidEntryPoint
class MainCategoryFragment : Fragment(R.layout.fragment_main_category) {
    private var _binding: FragmentMainCategoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var newServicesAdapter: NewServicesAdapter
    private lateinit var recommendedAdapter: RecommendedAdapter
    private lateinit var bestDealsAdapter: BestDealsAdapter
    private val viewModel by viewModels<MainCategoryViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        observeViewModelStates()
    }

    private fun setupRecyclerViews() {
        setupNewServicesRv()
        setupBestDealsRv()
        setupRecommendedRv()
    }

    private fun observeViewModelStates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeNewServices() }
                launch { observeBestDeals() }
                launch { observeRecommended() }
            }
        }
    }

    private suspend fun observeNewServices() {
        viewModel.newServices.collect { result ->
            when (result) {
                is Result.Success<List<Service>, DataError> -> {
                    hideLoading()
                    Log.d(TAG, "Received new services: ${result.data}")
                    newServicesAdapter.differ.submitList(result.data)
                }
                is Result.Error<List<Service>, DataError> -> {
                    hideLoading()
                    handleError(result.error)
                }
            }
        }
    }


    private suspend fun observeBestDeals() {
        viewModel.bestDeals.collect { result ->
            when (result) {
                is Result.Success<List<Service>, DataError> -> {
                    hideLoading()
                    bestDealsAdapter.differ.submitList(result.data)
                }
                is Result.Error<List<Service>, DataError> -> {
                    hideLoading()
                    handleError(result.error)
                }
            }
        }
    }


    private suspend fun observeRecommended() {
        viewModel.recommended.collect { result ->
            when (result) {
                is Result.Success<List<Service>, DataError> -> {
                    hideLoading()
                    recommendedAdapter.differ.submitList(result.data)
                }
                is Result.Error<List<Service>, DataError> -> {
                    hideLoading()
                    handleError(result.error)
                }
            }
        }
    }

    private fun handleError(error: DataError) {
        Log.e(TAG, error.toString())
        Toast.makeText(requireContext(), error.toString(), Toast.LENGTH_SHORT).show()
    }


    private fun hideLoading() {
        binding.mainProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainProgressBar.visibility = View.VISIBLE
    }

    private fun setupNewServicesRv() {
        newServicesAdapter = NewServicesAdapter()
        binding.rvNewServices.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = newServicesAdapter
        }
    }

    private fun setupRecommendedRv() {
        recommendedAdapter = RecommendedAdapter()
        binding.rvRecommended.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedAdapter
        }
    }

    private fun setupBestDealsRv() {
        bestDealsAdapter = BestDealsAdapter()
        binding.rvBestDeals.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = bestDealsAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}