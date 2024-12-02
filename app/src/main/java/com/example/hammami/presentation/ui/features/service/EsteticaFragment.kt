package com.example.hammami.presentation.ui.features.service

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hammami.databinding.FragmentEsteticaBinding
import com.example.hammami.presentation.ui.adapters.EsteticaAdapter
import com.example.hammami.presentation.ui.features.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EsteticaFragment: BaseFragment() {

    private lateinit var epilazioneAdapter: EsteticaAdapter
    private lateinit var trattCorpoAdapter: EsteticaAdapter
    private lateinit var trattVisoAdapter: EsteticaAdapter

    private var _binding: FragmentEsteticaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EsteticaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEsteticaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                launch { observeEpilazioneState() }
                launch { observeTrattCorpoState() }
                launch { observeTrattVisoState() }
                launch { observeEvents() }
            }
        }
    }

    private suspend fun observeEpilazioneState(){
        viewModel.allEpilazione.collectLatest { state ->
            updateEpilazioneList(state)
        }
    }

    private suspend fun observeTrattCorpoState(){
        viewModel.allTrattCorpo.collectLatest { state ->
            updateTrattCorpoList(state)
        }
    }

    private suspend fun observeTrattVisoState(){
        viewModel.allTrattViso.collectLatest { state ->
            updateTrattVisoList(state)
        }
    }

    /*
    private suspend fun observeState() {
        viewModel.allEpilazione.collectLatest { state ->
            updateEpilazioneList(state)
        }

        viewModel.allTrattCorpo.collectLatest { state ->
            updateTrattCorpoList(state)
        }

        viewModel.allTrattViso.collectLatest { state ->
            updateTrattVisoList(state)
        }

    }
     */

    private suspend fun observeEvents() {
        viewModel.uiEvent.collect { event ->
            handleEvent(event)
        }
    }

    /*
    private fun updateUI(state: EsteticaViewModel.EsteticaServices) = with(binding) {
        //progressIndicator.isVisible = state.isLoading
        //updateEpilazioneList(state)
        updateTrattCorpoList(state)
        updateTrattVisoList(state)
    }
        */

    /*
    private fun updateUIEpilazione(state: EsteticaViewModel.EsteticaServices) = with(binding) {
        //progressIndicator.isVisible = state.isLoading
        updateEpilazioneList(state)
    }

    private fun updateUITrattCorpo(state: EsteticaViewModel.EsteticaServices) = with(binding) {
        //progressIndicator.isVisible = state.isLoading
        updateTrattCorpoList(state)
    }

    private fun updateUITrattViso(state: EsteticaViewModel.EsteticaServices) = with(binding) {
        //progressIndicator.isVisible = state.isLoading
        updateTrattVisoList(state)
    }

     */

    private fun updateEpilazioneList(state: EsteticaViewModel.EsteticaServices) = with(binding) {
        epilazioneAdapter.submitList(state.servicesEpilazione)
    }

    private fun updateTrattCorpoList(state: EsteticaViewModel.EsteticaServices) = with(binding) {
        trattCorpoAdapter.submitList(state.servicesTrattCorpo)
    }

    private fun updateTrattVisoList(state: EsteticaViewModel.EsteticaServices) = with(binding) {
        trattVisoAdapter.submitList(state.servicesTrattViso)
    }

    private fun setupInitialState() = with(binding) {
        binding.mainProgressBar.visibility = View.GONE
    }

    private fun setupAppBar() {
       binding.topAppBar.setNavigationOnClickListener { onBackClick() }
    }

    private fun handleEvent(event: EsteticaViewModel.UiEvent) {
        when (event) {
            is EsteticaViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
            is EsteticaViewModel.UiEvent.ShowError -> showSnackbar(event.message)
        }
    }

    private fun hideLoading() {
        binding.mainProgressBar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.mainProgressBar.visibility = View.VISIBLE
    }

    private fun setupRecyclerView() {
        setupEpilazioneRv()
        setupTrattCorpoRv()
        setupTrattVisoRv()
    }

    private fun setupEpilazioneRv() {
        epilazioneAdapter = EsteticaAdapter()
        binding.rvEpilazione.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = epilazioneAdapter
        }
    }

    private fun setupTrattCorpoRv() {
        trattCorpoAdapter = EsteticaAdapter()
        binding.rvTrattCorpo.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = trattCorpoAdapter
        }
    }

    private fun setupTrattVisoRv() {
        trattVisoAdapter = EsteticaAdapter()
        binding.rvTrattViso.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = trattVisoAdapter
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("EsteticaFragment", "onResume")
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        Log.d("EsteticaFragment", "onPause")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}

    /*
    private lateinit var binding: FragmentEsteticaBinding
    private lateinit var epilazioneAdapter: EsteticaAdapter
    private lateinit var trattCorpoAdapter: EsteticaAdapter
    private lateinit var trattVisoAdapter: EsteticaAdapter
    private val viewModel by viewModels<EsteticaViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEsteticaBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEpilazioneRv()
        setupTrattCorpoRv()
        setupTrattVisoRv()

        lifecycleScope.launchWhenStarted{
            viewModel.allEpilazione.collectLatest {
                when (it){
                    /*
                    is Resource.Loading -> {
                        showLoading()
                    }
                     */

                    is Result.Error -> {
                        //hideLoading()
                        Log.e(TAG, it.error.toString())
                    }

                    is Result.Success -> {
                        Log.d(TAG, "Received new services: ${it.data}")
                        epilazioneAdapter.differ.submitList(it.data)
                        //hideLoading()
                    }
                }
            }
        }

        lifecycleScope.launchWhenStarted{
            viewModel.allTrattCorpo.collectLatest {
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
                        trattCorpoAdapter.differ.submitList(it.data)
                        hideLoading()
                    }
                    is Resource.Unspecified -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted{
            viewModel.allTrattViso.collectLatest {
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
                        trattVisoAdapter.differ.submitList(it.data)
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

    private fun setupEpilazioneRv() {
        epilazioneAdapter = EsteticaAdapter()
        binding.rvEpilazione.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = epilazioneAdapter
        }
    }

    private fun setupTrattCorpoRv() {
        trattCorpoAdapter = EsteticaAdapter()
        binding.rvTrattCorpo.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = trattCorpoAdapter
        }
    }

    private fun setupTrattVisoRv() {
        trattVisoAdapter = EsteticaAdapter()
        binding.rvTrattViso.apply{
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = trattVisoAdapter
        }
    }

    companion object {
        private const val TAG = "EsteticaFragment"
    }
     */
