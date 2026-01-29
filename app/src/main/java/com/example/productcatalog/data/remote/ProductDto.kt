package com.example.productcatalog.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductsResponseDto(
    @SerialName("products")
    val products: List<ProductDto> = emptyList(),

    @SerialName("total")
    val total: Int,

    @SerialName("skip")
    val skip: Int,

    @SerialName("limit")
    val limit: Int
)

@Serializable
data class ProductDto(
    @SerialName("id")
    val id: Int,

    @SerialName("title")
    val title: String,

    @SerialName("description")
    val description: String? = null,

    @SerialName("category")
    val category: String? = null,

    @SerialName("price")
    val price: Double,

    @SerialName("brand")
    val brand: String? = null,

    @SerialName("thumbnail")
    val thumbnail: String? = null,
    
    @SerialName("images")
    val images: List<String> = emptyList()
)
