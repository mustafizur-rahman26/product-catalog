package com.example.productcatalog.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.productcatalog.domain.model.Product
import com.example.productcatalog.ui.products.ProductsContent
import com.example.productcatalog.ui.products.UiState
import com.example.productcatalog.ui.theme.ProductCatalogTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleProducts = listOf(
        Product(
            id = 1,
            name = "Slim Fit Jeans",
            brand = "ABC",
            price = "24.99 Kr.",
            thumbnail = "https://example.com/image1.jpg"
        ),
        Product(
            id = 2,
            name = "Cotton T-shirt",
            brand = "ABC",
            price = "10.39 Kr.",
            thumbnail = "https://example.com/image2.jpg"
        ),
        Product(
            id = 3,
            name = "Hooded Sweatshirt",
            brand = "ABC",
            price = "29.99 Kr.",
            thumbnail = "https://example.com/image3.jpg"
        )
    )

    val sampleErrorMessage = "Failed to load products"

    @Test
    fun errorState_displaysErrorMessageAndRetryButton() {
        // Given
        val uiState = UiState(
            products = emptyList(),
            isLoading = false,
            errorMessage = sampleErrorMessage
        )

        // When
        composeTestRule.setContent {
            ProductCatalogTheme {
                ProductsContent(
                    uiState = uiState,
                    onLoadMore = {},
                    onRetryLoadMore = {},
                    onProductClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Error: $sampleErrorMessage")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_displaysEmptyMessage() {
        // Given
        val uiState = UiState(
            products = emptyList(),
            isLoading = false,
            errorMessage = null
        )

        // When
        composeTestRule.setContent {
            ProductCatalogTheme {
                ProductsContent(
                    uiState = uiState,
                    onLoadMore = {},
                    onRetryLoadMore = {},
                    onProductClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("No products available")
            .assertIsDisplayed()
    }

    @Test
    fun successState_displaysProductList() {
        // Given
        val uiState = UiState(
            products = sampleProducts,
            isLoading = false,
            errorMessage = null
        )

        // When
        composeTestRule.setContent {
            ProductCatalogTheme {
                ProductsContent(
                    uiState = uiState,
                    onLoadMore = {},
                    onRetryLoadMore = {},
                    onProductClick = {}
                )
            }
        }

        // Then - Verify each product's details
        // Product 1
        composeTestRule.onAllNodesWithText("ABC")[0].assertIsDisplayed()
        composeTestRule.onNodeWithText("SLIM FIT JEANS", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("24.99 Kr.").assertIsDisplayed()

        // Product 2
        composeTestRule.onAllNodesWithText("ABC")[1].assertIsDisplayed()
        composeTestRule.onNodeWithText("COTTON T-SHIRT", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("10.39 Kr.").assertIsDisplayed()

        // Product 3
        composeTestRule.onAllNodesWithText("ABC")[2].assertIsDisplayed()
        composeTestRule.onNodeWithText("HOODED SWEATSHIRT", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("29.99 Kr.").assertIsDisplayed()
    }

    @Test
    fun productWithNoPrice_displaysNA() {
        // Given
        val productWithoutPrice = Product(
            id = 1,
            name = "Free Item",
            brand = "Brand",
            price = null,
            thumbnail = "https://example.com/image.jpg"
        )
        val uiState = UiState(
            products = listOf(productWithoutPrice),
            isLoading = false,
            errorMessage = null
        )

        // When
        composeTestRule.setContent {
            ProductCatalogTheme {
                ProductsContent(
                    uiState = uiState,
                    onLoadMore = {},
                    onRetryLoadMore = {},
                    onProductClick = {}
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("N/A")
            .assertIsDisplayed()
    }

}
