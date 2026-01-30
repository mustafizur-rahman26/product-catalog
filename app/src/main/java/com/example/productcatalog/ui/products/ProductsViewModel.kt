package com.example.productcatalog.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productcatalog.domain.repository.ProductRepository
import com.example.productcatalog.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 0
) {
    val shouldLoadMore: Boolean
        get() = hasMorePages && !isLoadingMore && errorMessage == null
}

@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()
    private val uiStateVal: UiState
        get() = _uiState.value

    private var loadingJob: Job? = null

    init {
        loadInitialProducts()
    }

    private fun loadInitialProducts() {
        if (uiStateVal.isLoading || loadingJob?.isActive == true) {
            return
        }

        loadingJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loadProducts(page = 1)
        }
    }


    fun loadMoreProducts() {
        if (uiStateVal.isLoadingMore || !uiStateVal.hasMorePages || loadingJob?.isActive == true) {
            return
        }

        loadingJob = viewModelScope.launch {
            val nextPage = uiStateVal.currentPage + 1

            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            loadProducts(page = nextPage)
        }
    }


    private suspend fun loadProducts(page: Int) {
        productRepository.getProducts(page = page)
            .onSuccess { data ->
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        products = (currentUiState.products + data.products).distinctBy { it.id },
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = null,
                        hasMorePages = data.hasMorePages,
                        currentPage = data.currentPage
                    )
                }
            }
            .onError { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        errorMessage = error
                    )
                }
            }
    }

    fun retryLoadProducts() {
        _uiState.update { it.copy(errorMessage = null) }

        takeIf { uiStateVal.products.isEmpty() }
            ?.let { loadInitialProducts() }
            ?: loadMoreProducts()
    }
}
