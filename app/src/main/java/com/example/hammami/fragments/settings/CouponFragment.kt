package com.example.hammami.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.adapters.CouponAdapter
import com.example.hammami.databinding.FragmentCouponBinding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.viewmodel.UserProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CouponFragment : BaseFragment() {

    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserProfileViewModel by viewModels()

    private lateinit var couponAdapter: CouponAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        with(binding) {
            topAppBar.setNavigationOnClickListener { onBackClick() }
        }
        viewModel.loadCoupons()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        couponAdapter = CouponAdapter(emptyList()) { value ->
            showConfirmationDialog(value)
        }
        binding.rvCoupons.apply {
            adapter = couponAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        viewModel.loadCoupons()
    }

    private fun showConfirmationDialog(value: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Conferma")
            .setMessage("Vuoi riscattare un buono da $value € con i tuoi ${value * 5} pt?")
            .setPositiveButton("Conferma") { _, _ ->
                viewModel.onCouponSelected(value)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun showGeneratedCouponDialog(couponCode: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_generated_coupon, null)
        val couponTextView = dialogView.findViewById<TextInputEditText>(R.id.textFieldCouponCode)
        val proceedButton = dialogView.findViewById<Button>(R.id.buttonProceed)

        couponTextView.setText(couponCode)
    }

    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.couponValues.collect { values ->
                couponAdapter.updateCoupons(values)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collectResource(
                onSuccess = { user ->
                    binding.tvPoints.text = "${user.points} punti"
                },
                onError = { errorMessage ->
                    showSnackbar(errorMessage ?: "Errore sconosciuto")
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.generatedCoupon.collect { couponCode ->
                couponCode?.let {
                    showGeneratedCouponDialog(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}