import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hammami.R
import com.example.hammami.model.User
import com.example.hammami.databinding.FragmentRegisterBinding
import com.example.hammami.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment4 : Fragment(R.layout.fragment_register4) {
    private lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonRegister.setOnClickListener {
                val user = User(
                    firstNameRegister.text.toString().trim(),
                    lastNameRegister.text.toString().trim(),
                    emailRegister.text.toString().trim()
                )

                val password = passwordRegister.text.toString()
                viewModel.createAccount(user, password)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.register.collect { event ->
                    when (event) {
                        is Resource.Loading -> {
                            binding.buttonRegister.startAnimation()
                        }

                        is Resource.Success -> {
                            Log.d("test", event.message.toString())
                            binding.buttonRegister.revertAnimation()
                        }

                        is Resource.Error -> {
                            Log.e("RegisterFragment4", event.message, toString())
                            binding.buttonRegister.revertAnimation()
                        }

                        else -> {
                            // Gestisci altri casi qui
                        }
                    }
                }
            }
        }
    }
}