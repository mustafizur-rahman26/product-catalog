package com.example.productcatalog.domain.model

data class ProductDetail(
    val id: Int,
    val name: String,
    val brand: String,
    val price: String?,
    val thumbnail: String,
    val description: String?,
    val category: String?,
    val images: List<String>
)
