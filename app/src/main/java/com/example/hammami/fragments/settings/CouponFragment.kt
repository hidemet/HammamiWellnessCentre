package com.example.hammami.fragments.settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.R
import com.example.hammami.adapters.ActiveCouponAdapter
import com.example.hammami.adapters.AvailableCouponAdapter
import com.example.hammami.databinding.FragmentCouponBinding
import com.example.hammami.fragments.BaseFragment
import com.example.hammami.models.User
import com.example.hammami.util.Resource
import com.example.hammami.viewmodel.CouponViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class CouponFragment : BaseFragment() {

    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CouponViewModel by viewModels()

    private lateinit var availableCouponsAdapter: AvailableCouponAdapter
    private lateinit var activeCouponsAdapter: ActiveCouponAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun setupUI() {
        binding.topAppBar.setNavigationOnClickListener { onBackClick() }
        setupRecyclerViews()
        viewModel.loadCoupons()
        observeFlows()
    }

    private fun setupRecyclerViews() {
        availableCouponsAdapter = AvailableCouponAdapter(emptyList()) { value ->
            showConfirmationDialog(value)
        }
        activeCouponsAdapter = ActiveCouponAdapter { coupon ->
            viewModel.copyCouponToClipboard(coupon.code)
            showSnackbar("Codice coupon copiato negli appunti")
        }

        binding.rvAvailableCoupons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = availableCouponsAdapter
        }

        binding.rvActiveCoupons.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = activeCouponsAdapter
        }
    }

    private fun showConfirmationDialog(value: Int) {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Conferma")
            .setMessage("Vuoi riscattare un buono da $value € con i tuoi ${value * 5} punti?")
            .setPositiveButton("Conferma") { _, _ ->
                viewModel.onCouponSelected(value)
            }.setNegativeButton("Annulla", null).show()
    }

    private fun showGeneratedCouponDialog(couponCode: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_generated_coupon, null)
        dialogView.findViewById<TextInputEditText>(R.id.textFieldCouponCode).apply {
            setText(couponCode)
            setOnClickListener {
                viewModel.copyCouponToClipboard(couponCode)
                showSnackbar("Codice coupon copiato negli appunti")
            }
        }

        MaterialAlertDialogBuilder(requireContext()).setView(dialogView)
            .setPositiveButton("Chiudi", null).create().show()
    }


    override fun observeFlows() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { resource ->
                    when (resource) {
                        is Resource.Success -> updateUI(resource.data!!)
                        is Resource.Error -> showSnackbar(resource.message ?: "Errore sconosciuto")
                        else -> Unit
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.availableCouponValues.collect { values ->
                    availableCouponsAdapter.updateCoupons(values)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeCoupons.collect { couponsResource ->
                    when (couponsResource) {
                        is Resource.Success -> {
                            activeCouponsAdapter.submitList(couponsResource.data)
                            binding.tvNoActiveCoupons.visibility =
                                if (couponsResource.data!!.isEmpty()) View.VISIBLE else View.GONE
                            showLoading(false)
                        }

                        is Resource.Error -> {
                            showSnackbar(couponsResource.message ?: "Errore sconosciuto")
                            binding.tvNoActiveCoupons.visibility = View.GONE
                            showLoading(false)
                        }

                        is Resource.Loading -> {
                            showLoading(true)
                            binding.tvNoActiveCoupons.visibility = View.GONE
                        }

                        is Resource.Unspecified -> {
                            binding.tvNoActiveCoupons.visibility = View.GONE
                            showLoading(false)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.couponGenerationResult.collect { resource ->
                    when (resource) {
                        is Resource.Success -> showGeneratedCouponDialog(resource.data!!.code)
                        is Resource.Error -> showSnackbar(resource.message ?: "Errore sconosciuto")
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun updateUI(user: User) {
        binding.tvPoints.text = getString(R.string.points_text, user.points)
    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}