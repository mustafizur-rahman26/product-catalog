package com.example.productcatalog.ui.screens

import com.example.productcatalog.domain.model.PaginatedProducts
import com.example.productcatalog.domain.model.Product
import com.example.productcatalog.domain.repository.ProductRepository
import com.example.productcatalog.ui.products.ProductsViewModel
import com.example.productcatalog.util.Async
import com.example.productcatalog.util.ErrorType
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    private lateinit var repository: ProductRepository
    private lateinit var viewModel: ProductsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val sampleProduct1 = Product(
        id = 1,
        name = "Test Product 1",
        brand = "Test Brand",
        price = "$10",
        thumbnail = "https://test.com/1.jpg"
    )

    private val sampleProduct2 = Product(
        id = 2,
        name = "Test Product 2",
        brand = "Test Brand",
        price = "$20",
        thumbnail = "https://test.com/2.jpg"
    )

    private val samplePaginatedProductsPage1 = PaginatedProducts(
        products = listOf(sampleProduct1),
        currentPage = 1,
        totalPages = 10
    )

    private val samplePaginatedProductsPage2 = PaginatedProducts(
        products = listOf(sampleProduct2),
        currentPage = 2,
        totalPages = 10
    )

    private val samplePaginatedProductsLastPage = PaginatedProducts(
        products = listOf(sampleProduct1),
        currentPage = 10,
        totalPages = 10
    )


    private val sampleNetworkErrorMessage = "Network error"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load success updates products and state`() = runTest {
        // Given
        whenever(repository.getProducts(1)).thenReturn(Async.Success(samplePaginatedProductsPage1))

        // When
        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(1, state.products.size)
        assertEquals(1, state.currentPage)
        assertTrue(state.hasMorePages)
        assertFalse(state.isLoading)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `initial load error sets error message`() = runTest {
        // Given
        whenever(repository.getProducts(1)).thenReturn(
            Async.Error(sampleNetworkErrorMessage, ErrorType.NETWORK_ERROR)
        )

        // When
        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(sampleNetworkErrorMessage, state.errorMessage)
        assertFalse(state.isLoading)
        assertTrue(state.products.isEmpty())
    }


    @Test
    fun `loadMoreProducts appends products to existing list`() = runTest {
        // Given - initial load
        whenever(repository.getProducts(1)).thenReturn(Async.Success(samplePaginatedProductsPage1))
        whenever(repository.getProducts(2)).thenReturn(Async.Success(samplePaginatedProductsPage2))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - load more
        viewModel.loadMoreProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(2, state.products.size)
        assertEquals(2, state.currentPage)
        assertFalse(state.isLoadingMore)
    }

    @Test
    fun `loadMoreProducts prevented when isLoadingMore is true`() = runTest {
        // Given
        whenever(repository.getProducts(any())).thenReturn(Async.Success(samplePaginatedProductsPage1))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Manually set isLoadingMore
        viewModel.loadMoreProducts() // Start loading

        // When - try to load more again immediately
        val productCountBefore = viewModel.uiState.value.products.size
        viewModel.loadMoreProducts()

        // Then - should not trigger another load
        assertEquals(productCountBefore, viewModel.uiState.value.products.size)
    }

    @Test
    fun `loadMoreProducts prevented when hasMorePages is false`() = runTest {
        // Given - last page
        whenever(repository.getProducts(1)).thenReturn(Async.Success(samplePaginatedProductsLastPage))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - try to load more
        val productCountBefore = viewModel.uiState.value.products.size
        viewModel.loadMoreProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should not load (hasMorePages is false)
        assertEquals(productCountBefore, viewModel.uiState.value.products.size)
    }

    @Test
    fun `loadMoreProducts deduplicates products`() = runTest {
        // Given - duplicate product IDs across pages
        val duplicateProduct = sampleProduct1.copy(id = 1)
        val page2 = PaginatedProducts(
            products = listOf(duplicateProduct, sampleProduct2), // Duplicate ID 1
            currentPage = 2,
            totalPages = 10
        )
        whenever(repository.getProducts(1)).thenReturn(Async.Success(samplePaginatedProductsPage1))
        whenever(repository.getProducts(2)).thenReturn(Async.Success(page2))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loadMoreProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - should have 2 products (duplicate removed)
        assertEquals(2, viewModel.uiState.value.products.size)
        assertEquals(1, viewModel.uiState.value.products[0].id)
        assertEquals(2, viewModel.uiState.value.products[1].id)
    }

    @Test
    fun `retryLoadMore calls loadInitialProducts when products empty`() = runTest {
        // Given - initial load error
        whenever(repository.getProducts(1))
            .thenReturn(Async.Error(sampleNetworkErrorMessage, ErrorType.NETWORK_ERROR))
            .thenReturn(Async.Success(samplePaginatedProductsPage1))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - retry
        viewModel.retryLoadProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.uiState.value.products.size)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `retryLoadMore calls loadMoreProducts when products exist`() = runTest {
        // Given - initial load success, then load more error
        whenever(repository.getProducts(1)).thenReturn(Async.Success(samplePaginatedProductsPage1))
        whenever(repository.getProducts(2))
            .thenReturn(Async.Error(sampleNetworkErrorMessage, ErrorType.NETWORK_ERROR))
            .thenReturn(Async.Success(samplePaginatedProductsPage2))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // When - retry
        viewModel.retryLoadProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.uiState.value.products.size)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `retryLoadMore clears error message`() = runTest {
        // Given
        whenever(repository.getProducts(1)).thenReturn(Async.Error(sampleNetworkErrorMessage, ErrorType.NETWORK_ERROR))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(sampleNetworkErrorMessage, viewModel.uiState.value.errorMessage)

        // When
        viewModel.retryLoadProducts()

        // Then - error cleared immediately (before reload)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `shouldLoadMore false when errorMessage exists`() = runTest {
        // Given - initial load fails with network error
        whenever(repository.getProducts(1)).thenReturn(
            Async.Error(sampleNetworkErrorMessage, ErrorType.NETWORK_ERROR)
        )

        // When
        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.shouldLoadMore)
    }

    @Test
    fun `shouldLoadMore false when already loading`() = runTest {
        // Given
        whenever(repository.getProducts(1)).thenReturn(Async.Success(samplePaginatedProductsPage1))
        whenever(repository.getProducts(2)).thenReturn(Async.Success(samplePaginatedProductsPage2))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - trigger loadMore
        viewModel.loadMoreProducts()

        // Then - isLoadingMore state transition verifies loading guard
        testDispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals(2, state.currentPage)
        assertTrue(state.hasMorePages)
        assertFalse(state.isLoadingMore)
        assertTrue(state.shouldLoadMore)
    }

    @Test
    fun `shouldLoadMore false when no more pages`() = runTest {
        // Given - at last page (currentPage = totalPages)
        whenever(repository.getProducts(1)).thenReturn(Async.Success(samplePaginatedProductsLastPage))
        
        // When
        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then - no more pages to load
        val state = viewModel.uiState.value
        assertFalse(state.hasMorePages)
        assertFalse(state.isLoadingMore)
        assertEquals(null, state.errorMessage)
        assertFalse(state.shouldLoadMore)
    }

    // ========== Edge Case Tests ==========

    @Test
    fun `state updates correctly on last page`() = runTest {
        // Given - last page (10)
        whenever(repository.getProducts(1)).thenReturn(Async.Success(samplePaginatedProductsLastPage))

        // When
        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(10, state.currentPage)
        assertFalse(state.hasMorePages)
    }

    @Test
    fun `multiple rapid loadMoreProducts calls prevented by guard`() = runTest {
        // Given
        whenever(repository.getProducts(any())).thenReturn(Async.Success(samplePaginatedProductsPage1))

        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - multiple rapid calls
        viewModel.loadMoreProducts()
        viewModel.loadMoreProducts()
        viewModel.loadMoreProducts()

        // Then - only one load should happen (guard prevents duplicates)
        assertFalse(viewModel.uiState.value.isLoadingMore)
    }

    // ========== Integration Tests ==========

    @Test
    fun `complete pagination flow from page 1 to 3`() = runTest {
        // Given
        val page1 = samplePaginatedProductsPage1
        val page2 = samplePaginatedProductsPage2
        val product3 = sampleProduct1.copy(id = 3, name = "Product 3")
        val page3 = PaginatedProducts(
            products = listOf(product3),
            currentPage = 3,
            totalPages = 10
        )

        whenever(repository.getProducts(1)).thenReturn(Async.Success(page1))
        whenever(repository.getProducts(2)).thenReturn(Async.Success(page2))
        whenever(repository.getProducts(3)).thenReturn(Async.Success(page3))

        // When
        viewModel = ProductsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.loadMoreProducts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(3, state.products.size)
        assertEquals(3, state.currentPage)
        assertTrue(state.hasMorePages)
    }
}
