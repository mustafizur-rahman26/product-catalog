package com.example.productcatalog.domain.model

data class PaginatedProducts(
    val products: List<Product>,
    val currentPage: Int,
    val totalPages: Int,
) {
    val hasMorePages: Boolean
        get() = currentPage < totalPages
}
