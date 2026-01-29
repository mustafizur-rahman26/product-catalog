package com.example.productcatalog.data.repository

import com.example.productcatalog.data.mapper.toPaginatedProducts
import com.example.productcatalog.data.remote.ApiService
import com.example.productcatalog.di.IoDispatcher
import com.example.productcatalog.domain.repository.ProductsRepository
import com.example.productcatalog.domain.model.PaginatedProducts
import com.example.productcatalog.util.Async
import com.example.productcatalog.util.toErrorMessage
import com.example.productcatalog.util.toErrorType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductsRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ProductsRepository {
    
    companion object {
        private const val PAGE_SIZE = 30
    }
    
    override suspend fun getProducts(
        page: Int
    ): Async<PaginatedProducts> = withContext(ioDispatcher) {
        runCatching {
            val skip = (page - 1) * PAGE_SIZE
            apiService.getProducts(
                limit = PAGE_SIZE,
                skip = skip
            )
        }.fold(
            onSuccess = { response ->
                Async.Success(data = response.toPaginatedProducts())
            },
            onFailure = { error ->
                Async.Error(
                    errorMessage = error.toErrorMessage(),
                    errorType = error.toErrorType()
                )
            }
        )
    }
}
