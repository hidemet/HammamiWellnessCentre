package com.example.hammami.presentation.ui.features.service
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hammami.domain.model.Service
import com.example.hammami.domain.usecase.GetReviewsUseCase

class ServiceDetailViewModelFactory(
    private val getReviewsUseCase: GetReviewsUseCase,
    private val service: Service
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceDetailViewModel::class.java)) {
            return ServiceDetailViewModel(getReviewsUseCase).apply {
                setService(service)
            } as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}