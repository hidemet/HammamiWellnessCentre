package com.example.hammami.presentation.ui.features.appointments

//import com.example.hammami.presentation.ui.adapters.AppointmentAdapter

//@AndroidEntryPoint
//class NewAppointmentsFragment : BaseFragment(){
//
//    private lateinit var appointmentAdapter: AppointmentAdapter
//
//    private var _binding: FragmentNewAppointmentsBinding? = null
//    private val binding get() = _binding!!
//
//    private val viewModel: AppointmentsViewModel by viewModels()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentNewAppointmentsBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        viewModel.loadNewAppointmentsData(viewModel.userEmail!!)
//        Log.e("NewAppointmentsFragment", "email: ${viewModel.userEmail}")
//        //Log.e("NewAppointmentsFragment", "emailUiState: ${viewModel.uiState.value.user?.email}")
//        setupUI()
//        observeFlows()
//    }
//
//    override fun setupUI() {
//        //setupAppBar()
//        setupRecyclerView()
//    }
//
//    override fun observeFlows() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch { observeAppointments() }
//                //launch { observeEvents() }
//            }
//        }
//    }
//
//    private suspend fun observeAppointments() {
//        viewModel.newAppointments.collectLatest { state ->
//            //updateUI(state)
//            appointmentAdapter.submitList(state)
//        }
//    }
//
//
//    private fun handleEvent(event: AppointmentsViewModel.UiEvent) {
//        when (event) {
//            is AppointmentsViewModel.UiEvent.ShowMessage -> showSnackbar(event.message)
//            is AppointmentsViewModel.UiEvent.ShowError -> showSnackbar(event.message)
//            else -> {}
//        }
//    }
//
//    private fun setupRecyclerView() {
//        appointmentAdapter = AppointmentAdapter()
//        binding.rvNewAppointments.apply{
//            layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//            adapter = appointmentAdapter
//        }
//    }

    /*
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

     */

//}



    /*
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_new_appointments, container, false)
    }


     */