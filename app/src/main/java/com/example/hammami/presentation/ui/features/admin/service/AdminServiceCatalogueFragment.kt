package com.example.hammami.presentation.ui.features.admin.service

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.hammami.databinding.FragmentAdminServiceCatalogueBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminServiceCatalogueFragment : Fragment() {
    private var _binding: FragmentAdminServiceCatalogueBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminServiceCatalogueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Setup UI
    }
}