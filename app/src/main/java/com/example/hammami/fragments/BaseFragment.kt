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
        onLoading: () -> Unit = { showLoading(true) },
        onComplete: () -> Unit = { showLoading(false) }
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            collect { state ->
                when (state) {
                    is Resource.Loading -> onLoading()
                    is Resource.Success -> {
                        onComplete()
                        state.data?.let { onSuccess(it) }
                    }
                    is Resource.Error -> {
                        onComplete()
                        onError(state.message)
                    }
                    is Resource.Unspecified -> Unit
                }
            }
        }
    }

    protected open fun showLoading(isLoading: Boolean) {
        // Override this in child fragments to implement specific loading behavior
    }

    protected fun onBackClick() {
        findNavController().popBackStack()
    }

    protected fun showSnackbar(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setAction(getString(android.R.string.ok)) { }
                .show()
        }
    }
}