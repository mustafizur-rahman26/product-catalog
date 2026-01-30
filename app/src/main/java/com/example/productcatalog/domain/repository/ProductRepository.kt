package com.example.productcatalog.domain.repository

import com.example.productcatalog.domain.model.PaginatedProducts
import com.example.productcatalog.domain.model.ProductDetail
import com.example.productcatalog.util.Async

interface ProductRepository {
    suspend fun getProducts(page: Int = 1): Async<PaginatedProducts>
    suspend fun getProductDetail(productId: Int): Async<ProductDetail>
}
