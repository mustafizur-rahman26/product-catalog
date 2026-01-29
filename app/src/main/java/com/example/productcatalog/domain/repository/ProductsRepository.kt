package com.example.productcatalog.domain.repository

import com.example.productcatalog.domain.model.PaginatedProducts
import com.example.productcatalog.util.Async

interface ProductsRepository {
    suspend fun getProducts(
        page: Int = 1
    ): Async<PaginatedProducts>
}