package com.example.hammami.fragments.categories

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.hammami.R
import com.example.hammami.adapters.BenessereAdapter
import com.example.hammami.adapters.BenessereViewpagerAdapter
import com.example.hammami.adapters.BestDealsAdapter
import com.example.hammami.adapters.NewServicesAdapter
import com.example.hammami.adapters.RecommendedAdapter
import com.example.hammami.databinding.FragmentBenessereBinding
import com.example.hammami.databinding.FragmentMainCategoryBinding
import com.example.hammami.databinding.FragmentServiziBenessereBinding
import com.example.hammami.util.Resource
import com.example.hammami.viewmodel.BenessereViewModel
import com.example.hammami.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "BenessereViewModel"
@AndroidEntryPoint
class BenessereFragment: Fragment(R.layout.fragment_benessere) {
    private lateinit var binding: FragmentBenessereBinding
    private lateinit var bindingViewPager: FragmentServiziBenessereBinding //non ne sono sicuro, è una prova
    private lateinit var benessereAdapter: BenessereAdapter
    private val viewModel by viewModels<BenessereViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBenessereBinding.inflate(inflater)
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
        lifecycleScope.launchWhenStarted{
            viewModel.allBenessere.collectLatest {
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
                        benessereAdapter.differ.submitList(it.data)
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
        benessereAdapter = BenessereAdapter()
        binding.rvBenessere.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = benessereAdapter
        }
    }
}