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
import com.example.hammami.adapters.BenessereAdapter
import com.example.hammami.adapters.MassaggiAdapter
import com.example.hammami.databinding.FragmentBenessereBinding
import com.example.hammami.databinding.FragmentMassaggiBinding
import com.example.hammami.util.Resource
import com.example.hammami.viewmodel.BenessereViewModel
import com.example.hammami.viewmodel.MassaggiViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "MassaggiViewModel"

@AndroidEntryPoint
class MassaggiFragment: Fragment(R.layout.fragment_massaggi) {
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
}