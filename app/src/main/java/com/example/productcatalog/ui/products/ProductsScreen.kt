package com.example.productcatalog.ui.products

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.productcatalog.domain.model.Product
import com.example.productcatalog.ui.theme.Dimensions

@Composable
fun ProductsScreen(
    productsViewModel: ProductsViewModel = hiltViewModel(),
    onProductClick: (Int) -> Unit
) {
    val uiState by productsViewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    ProductsContent(
        uiState = uiState,
        gridState = gridState,
        onLoadMore = { productsViewModel.loadMoreProducts() },
        onRetryLoadMore = { productsViewModel.retryLoadProducts() },
        onProductClick = onProductClick
    )
}

@Composable
fun ProductsContent(
    uiState: UiState,
    gridState: LazyGridState = rememberLazyGridState(),
    onLoadMore: () -> Unit,
    onRetryLoadMore: () -> Unit,
    onProductClick: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = Dimensions.controlDoubleSpace)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            uiState.errorMessage != null && uiState.products.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.controlDoubleSpace),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.padding(bottom = Dimensions.controlDoubleSpace),
                        text = "Error: ${uiState.errorMessage}",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Button(
                        modifier = Modifier.heightIn(min = 48.dp),
                        onClick = onRetryLoadMore,
                    ) {
                        Text("Retry")
                    }
                }
            }

            uiState.products.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimensions.controlDoubleSpace),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No products available",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.controlSpace),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.controlDoubleSpace)
                ) {
                    items(items = uiState.products, key = { it.id }) { product ->
                        ProductGridItem(
                            product = product,
                            onClick = { onProductClick(product.id) }
                        )
                    }

                    // Loading indicator at bottom when loading more
                    if (uiState.isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimensions.controlDoubleSpace),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                    
                    // Error at bottom with retry button
                    if (uiState.errorMessage != null && uiState.products.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimensions.controlDoubleSpace),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    modifier = Modifier.padding(bottom = Dimensions.controlSpace),
                                    text = uiState.errorMessage,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Button(
                                    modifier = Modifier.heightIn(min = 48.dp),
                                    onClick = onRetryLoadMore,
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    
                    if (uiState.shouldLoadMore) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LaunchedEffect(Unit) {
                                onLoadMore()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductGridItem(
    product: Product,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.Start
    ) {
        // Product Image
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(25.dp)
                )
                .clip(RoundedCornerShape(8.dp)),
            model = ImageRequest.Builder(LocalContext.current)
                .data(product.thumbnail)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .networkCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            placeholder = ColorPainter(Color(0xFFF5F5F5)),
            error = ColorPainter(Color(0xFFEEEEEE))
        )

        Spacer(modifier = Modifier.height(Dimensions.controlSpace))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.controlDoubleSpace)
                .padding(top = Dimensions.controlDoubleSpace, bottom = Dimensions.controlTripleSpace)
        ) {
            // Brand / Collection
            Text(
                text = product.brand.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(Dimensions.controlHalfSpace))

            // Product Name
            Text(
                text = product.name.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(Dimensions.controlHalfSpace))

            // Price with currency
            Text(
                text = product.price ?: "N/A",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

// Preview Functions
@Preview(showBackground = true, name = "Products Grid - Light")
@Composable
private fun ProductsContentPreview() {
    MaterialTheme {
        ProductsContent(
            uiState = UiState(
                products = getSampleProducts(),
                isLoading = false,
                errorMessage = null
            ),
            onLoadMore = {},
            onRetryLoadMore = {},
            onProductClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Loading State")
@Composable
private fun ProductsContentLoadingPreview() {
    MaterialTheme {
        ProductsContent(
            uiState = UiState(
                products = emptyList(),
                isLoading = true,
                errorMessage = null
            ),
            onLoadMore = {},
            onRetryLoadMore = {},
            onProductClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
private fun ProductsContentErrorPreview() {
    MaterialTheme {
        ProductsContent(
            uiState = UiState(
                products = emptyList(),
                isLoading = false,
                errorMessage = "Failed to load products"
            ),
            onLoadMore = {},
            onRetryLoadMore = {},
            onProductClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State")
@Composable
private fun ProductsContentEmptyPreview() {
    MaterialTheme {
        ProductsContent(
            uiState = UiState(
                products = emptyList(),
                isLoading = false,
                errorMessage = null
            ),
            onLoadMore = {},
            onRetryLoadMore = {},
            onProductClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Product Grid Item")
@Composable
private fun ProductGridItemPreview() {
    MaterialTheme {
        ProductGridItem(
            product = getSampleProducts().first(),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Product Grid Item - Large Font", fontScale = 1.5f)
@Composable
private fun ProductGridItemLargeFontPreview() {
    MaterialTheme {
        ProductGridItem(
            product = getSampleProducts().first(),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Products Grid - Large Font", fontScale = 1.5f)
@Composable
private fun ProductsContentLargeFontPreview() {
    MaterialTheme {
        ProductsContent(
            uiState = UiState(
                products = getSampleProducts(),
                isLoading = false,
                errorMessage = null
            ),
            onLoadMore = {},
            onRetryLoadMore = {},
            onProductClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Error State - Large Font", fontScale = 1.5f)
@Composable
private fun ProductsContentErrorLargeFontPreview() {
    MaterialTheme {
        ProductsContent(
            uiState = UiState(
                products = emptyList(),
                isLoading = false,
                errorMessage = "Failed to load products"
            ),
            onLoadMore = {},
            onRetryLoadMore = {},
            onProductClick = {}
        )
    }
}

// Sample data for previews using current domain model
private fun getSampleProducts() = listOf(
    Product(
        id = 685816001,
        name = "Slim Fit Jeans",
        brand = "Some",
        price = "$24.99",
        thumbnail = "https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/1.webp",
    ),
    Product(
        id = 714026050,
        name = "Cotton T-shirt",
        brand = "ABC",
        price = "$10.39",
        thumbnail = "https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/1.webp",
    ),
    Product(
        id = 970819001,
        name = "Hooded Sweatshirt",
        brand = "ABC",
        price = "$29.99",
        thumbnail = "https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/1.webp",
    ),
    Product(
        id = 608945015,
        name = "Relaxed Fit Hoodie",
        brand = "ABC",
        price = "$24.49",
        thumbnail = "https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/1.webp∫",
    )
)