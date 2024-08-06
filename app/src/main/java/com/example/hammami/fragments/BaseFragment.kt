package com.example.hammami.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.hammami.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController


abstract class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeFlows()

    }


    abstract fun setupUI()

    abstract fun observeFlows()

    protected fun <T> Flow<Resource<T>>.collectResource(
        onSuccess: (T) -> Unit,
        onError: (String?) -> Unit,
        onLoading: () -> Unit = { showLoading(true) }
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            collect { state ->
                when (state) {
                    is Resource.Loading -> onLoading()
                    is Resource.Success -> {
                        showLoading(false)
                        state.data?.let { onSuccess(it) }
                    }

                    is Resource.Error -> {
                        showLoading(false)
                        onError(state.message)
                    }

                    is Resource.Unspecified -> Unit
                }
            }
        }
    }

    protected open fun showLoading(isLoading: Boolean) {
        // Implementazione di default vuota, da sovrascrivere nei fragment figli se necessario
    }

    protected fun onBackClick() = findNavController().popBackStack()


    protected fun showSnackbar(
        message: String,
        actionText: String? = null,
        action: (() -> Unit)? = null
    ) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG).apply {
                if (actionText != null && action != null) {
                    setAction(actionText) { action() }
                }
                show()
            }
        }
    }
}