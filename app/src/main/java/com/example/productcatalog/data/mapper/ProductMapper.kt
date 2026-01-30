package com.example.productcatalog.data.mapper

import android.annotation.SuppressLint
import com.example.productcatalog.data.remote.ProductDetailDto
import com.example.productcatalog.data.remote.ProductDto
import com.example.productcatalog.data.remote.ProductsResponseDto
import com.example.productcatalog.domain.model.PaginatedProducts
import com.example.productcatalog.domain.model.Product
import com.example.productcatalog.domain.model.ProductDetail
import kotlin.math.ceil

fun ProductsResponseDto.toPaginatedProducts(): PaginatedProducts {
    val currentPage = (skip / limit) + 1
    val totalPages = ceil(total.toDouble() / limit).toInt()
    
    return PaginatedProducts(
        products = this.products.map { it.toProduct() },
        currentPage = currentPage,
        totalPages = totalPages,
    )
}

@SuppressLint("DefaultLocale")
fun ProductDto.toProduct(): Product = Product(
    id = id,
    name = title,
    brand = brand ?: "Unknown",
    price = "$${String.format("%.2f", price)}",
    thumbnail = thumbnail
)

@SuppressLint("DefaultLocale")
fun ProductDetailDto.toProductDetail(): ProductDetail = ProductDetail(
    id = id,
    name = title,
    brand = brand ?: "Unknown",
    price = "$${String.format("%.2f", price)}",
    thumbnail = thumbnail,
    description = description,
    category = category,
    images = images
)
