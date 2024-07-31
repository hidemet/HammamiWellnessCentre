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
import com.example.hammami.R
import com.example.hammami.adapters.BestDealsAdapter
import com.example.hammami.adapters.CentreMemberAdapter
import com.example.hammami.adapters.NewServicesAdapter
import com.example.hammami.adapters.RecommendedAdapter
import com.example.hammami.databinding.FragmentCentreMemberBinding
import com.example.hammami.databinding.FragmentMainCategoryBinding
import com.example.hammami.util.Resource
import com.example.hammami.viewmodel.MainCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "CentreMemberFragment"
@AndroidEntryPoint
class CentreMemberFragment /* : Fragment(R.layout.fragment_centre_member) */ {
/*
    private lateinit var binding: FragmentCentreMemberBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCentreMemberBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    private fun setupCentreMembersRv() {
        centreMemberAdapter = CentreMemberAdapter()
        binding.rvHammamiMembers.apply{
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
            adapter = centreMemberAdapter
        }
    }


    private fun setupRecommendedRv() {
        recommendedAdapter = RecommendedAdapter()
        binding.rvRecommended.apply{
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendedAdapter
        }
    }

    private fun setupBestDealsRv() {
        bestDealsAdapter = BestDealsAdapter()
        binding.rvBestDeals.apply{
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL, false)
            adapter = bestDealsAdapter
        }
    }

 */
}