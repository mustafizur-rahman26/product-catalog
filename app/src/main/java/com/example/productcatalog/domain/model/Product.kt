package com.example.productcatalog.domain.model

data class Product(
    val id: Int,
    val name: String,
    val brand: String,
    val price: String?,
    val thumbnail: String,
)
