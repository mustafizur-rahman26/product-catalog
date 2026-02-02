# Product Catalog - Android Application

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.12.00-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Min API](https://img.shields.io/badge/API-24%2B-orange.svg?style=flat)](https://android-arsenal.com/api?level=24)

A modern Android application showcasing a product catalog with infinite scroll pagination, built using Kotlin and Jetpack Compose.

<div align="center">
  <img src="screenshots/product_list.png" width="300" alt="Product List Screen"/>
  <img src="screenshots/product_detail.png" width="300" alt="Product Detail Screen"/>
</div>

<p align="center">
  <i>Product catalog with 2-column grid layout | Product detail view with images and description</i>
</p>

## 📱 Features

- **Product Catalog**: Browse products in a responsive 2-column grid layout
- **Product Detail View**: Detailed product information with images, description, and pricing
- **Navigation**: Seamless navigation from product list to detailed view
- **Infinite Scroll**: Automatic pagination with load-more functionality
- **Error Handling**: Comprehensive error states with retry mechanisms

## 🏗️ Architecture

This project follows **Clean Architecture** with clear separation of concerns:

```
app/
├── data/
│   ├── mapper/          # DTO to Domain model mapping
│   ├── remote/          # API services and DTOs
│   └── repository/      # Repository implementations
├── domain/
│   ├── model/           # Business models
│   └── repository/      # Repository interfaces
├── ui/
│   ├── screens/         # Composable screens and ViewModels
│   └── theme/           # Material 3 theming
├── di/                  # Dependency injection modules
└── util/                # Utility classes and extensions
```

### Architecture Layers

1. **Presentation Layer** (`ui/`)



   ```
   ┌──────────────────────────────────────────────────────────────────┐
   │                   PRESENTATION LAYER (MVVM)                      │
   ├──────────────────────────────────────────────────────────────────┤
   │                                                                  │
   │  ┌───────────────────┐  observes   ┌────────────────────────┐    │
   │  │     Screens       │ ◄────────── │      ViewModels        │    │
   │  │   (Composables)   │             │    (State mgmt)        │    │
   │  │                   │  triggers   │                        │    │
   │  │ - ProductList     │ ──────────► │ - ProductsViewModel    │    │
   │  │   Screen          │             │ - ProductDetail        │    │
   │  │ - ProductDetail   │             │   ViewModel            │    │
   │  │   Screen          │             │                        │    │
   │  └───────────────────┘             └───────────┬────────────┘    │
   │                                                │                 │
   │                                   exposes      │ StateFlow       │
   │                                                ▼                 │
   │                                   ┌─────────────────┐            │
   │                                   │    UiState      │            │
   │                                   │                 │            │
   │                                   │  products       │            │
   │                                   │  isLoading      │            │
   │                                   │  isLoadingMore  │            │
   │                                   │  errorMessage   │            │
   │                                   │  hasMorePages   │            │
   │                                   │  currentPage    │            │
   │                                   └─────────────────┘            │
   │                                                                  │
   │  ┌─────────────────────────────────────────────────────────────┐ │
   │  │  Theme — Material 3 Colors & Typography                     │ │
   │  └─────────────────────────────────────────────────────────────┘ │
   └──────────────────────────────────────────────────────────────────┘
   ```

   ```kotlin
    data class UiState(
        val products: List<Product> = emptyList(),
        val isLoading: Boolean = false, // Fullscreen loading
        val isLoadingMore: Boolean = false, // Pagination loading at bottom
        val errorMessage: String? = null,
        val hasMorePages: Boolean = true,
        val currentPage: Int = 0
    )
    ```

2. **Domain Layer** (`domain/`)


   ```
   ┌─────────────────────────────────────────────────────────────────┐
   │                     DOMAIN LAYER                                │
   ├─────────────────────────────────────────────────────────────────┤
   │                                                                 │
   │  ┌────────────────────────────────────────────────────────┐     │
   │  │           Repository Interfaces (Contracts)            │     │
   │  │                                                        │     │
   │  │  interface ProductRepository {                         │     │
   │  │      suspend fun getProducts(page: Int): Async<...>    │     │
   │  │      suspend fun getProductDetail(productId: Int): ..  │     │
   │  │  }                                                     │     │
   │  └───────────────────────────┬────────────────────────────┘     │
   │                              │ defines                          │
   │                              │ contracts                        │
   │  ┌───────────────────────────▼────────────────────────────┐     │
   │  │              Domain Models (Business Entities)         │     │
   │  │                                                        │     │
   │  │  Product (id, name, brand, price, thumbnail)           │     │
   │  │  ProductDetail (description, category, images, ...)    │     │
   │  │  PaginatedProducts (products, currentPage, ...)        │     │
   │  └───────────────────────────┬────────────────────────────┘     │
   │                              │ wraps                            │
   │  ┌───────────────────────────▼─────────────────────────────┐    │
   │  │              Async Result Wrapper                       │    │
   │  │                                                         │    │
   │  │  sealed interface Async<out T>                          │    │
   │  │      - Success<T>(data: T)                              │    │
   │  │      - Error(errorMessage: String, errorType: ErrorType)│    │
   │  └─────────────────────────────────────────────────────────┘    │
   │                                                                 │
   └─────────────────────────────────────────────────────────────────┘
   ```

3. **Data Layer** (`data/`)


   ```
   ┌─────────────────────────────────────────────────────────────────┐
   │                      DATA LAYER                                 │
   ├─────────────────────────────────────────────────────────────────┤
   │                                                                 │
   │  ┌─────────────────────────────────────────────────────────┐    │
   │  │          Repository Implementations                     │    │
   │  │                                                         │    │
   │  │  class ProductRepositoryImpl : ProductRepository {      │    │
   │  │      - getProducts(page: Int)                           │    │
   │  │      - getProductDetail(productId: Int)                 │    │
   │  │      - Error handling with runCatching                  │    │
   │  │      - Executes on I/O dispatcher                       │    │
   │  │  }                                                      │    │
   │  └─────────────────────┬───────────────────────────────────┘    │
   │                        │ uses                                   │
   │  ┌─────────────────────▼─────────────────────────────────┐      │
   │  │              Remote Layer (Network)                   │      │
   │  │                                                       │      │
   │  │  ApiService (Retrofit)                                │      │
   │  │  - getProducts(limit, skip) → ProductsResponseDto     │      │
   │  │  - getProductById(id) → ProductDetailDto              │      │
   │  │                                                       │      │
   │  │  DTOs (Data Transfer Objects)                         │      │
   │  │  - ProductsResponseDto, ProductDto, ProductDetailDto  │      │
   │  └─────────────────────┬─────────────────────────────────┘      │
   │                        │ maps via                               │
   │  ┌─────────────────────▼─────────────────────────────────┐      │
   │  │              Mapper Layer                             │      │
   │  │                                                       │      │
   │  │  toPaginatedProducts()   DTO → Domain Model           │      │
   │  │  toProduct()              DTO → Domain Model          │      │
   │  │  toProductDetail()        DTO → Domain Model          │      │
   │  │                                                       │      │
   │  │  - Price formatting ($XX.XX)                          │      │
   │  │  - Pagination calculation (currentPage, totalPages)   │      │
   │  │  - Default value handling (brand ?: "Unknown")        │      │
   │  └─────────────────────┬─────────────────────────────────┘      │
   │                        │ returns                                │
   │  ┌─────────────────────▼─────────────────────────────────┐      │
   │  │         Async Result <Domain Model>                   │      │
   │  │                                                       │      │
   │  │  Success(data: PaginatedProducts)                     │      │
   │  │  Error(errorMessage, errorType)                       │      │
   │  └───────────────────────────────────────────────────────┘      │
   │                                                                 │
   │  ┌─────────────────────────────────────────────────────────┐    │
   │  │  Error Handling                                         │    │
   │  │  - Network exceptions → ErrorType (Network/...)         │    │
   │  │  - HTTP errors → ErrorType (ServerError/...)            │    │
   │  │  - Timeout/Unknown → ErrorType (UnknownError)           │    │
   │  │  - Maps exceptions to user-friendly messages            │    │
   │  └─────────────────────────────────────────────────────────┘    │
   └─────────────────────────────────────────────────────────────────┘
   ```

## 🛠️ Tech Stack

### Core Technologies
- **Kotlin** 2.2.21 - Modern programming language
- **Jetpack Compose** - Declarative UI framework
- **Material 3** - Latest Material Design components
- **Coroutines** 1.10.2 - Asynchronous programming
- **StateFlow** - Reactive state management

### Dependency Injection
- **Dagger Hilt** 2.57.2 - Compile-time DI framework

### Networking
- **Retrofit** 3.0.0 - Type-safe HTTP client
- **OkHttp** 5.3.2 - HTTP client with logging interceptor
- **Kotlinx Serialization** 1.9.0 - JSON serialization

### Image Loading
- **Coil** 2.7.0 - Image loading library for Compose

### Testing
- **JUnit** 4.13.2 - Unit testing framework
- **Mockito** 5.12.0 - Mocking framework
- **Turbine** 1.1.0 - Flow testing library
- **Compose UI Test** - UI testing framework

### Code Quality
- **Detekt** 1.23.8 - Static code analysis

## Mock Server API

The app uses DummyJSON API for product data. The base URL is configured in `app/build.gradle.kts`:

```kotlin
val baseUrl = "https://dummyjson.com/"
buildConfigField("String", "BASE_URL", "\"${baseUrl}\"")
```

## 🔧 Key Implementation Details

### Pagination Strategy
- Custom pagination logic
- Automatic load-more when scrolling near bottom
- Prevents duplicate requests with state management
- Handles edge cases (no more pages, errors, already loading)

### Error Handling
- Comprehensive network error mapping
- User-friendly error messages
- Retry functionality for both initial and pagination errors


### Image Optimization
- Memory, disk, and network caching with Coil
- Placeholder and error states
- Optimized aspect ratio (0.75f) for product images

### Code Style
- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Run Detekt before committing: `./gradlew detekt`
- Ensure all tests pass: `./gradlew test connectedAndroidTest`

---

**Made with ❤️ using Kotlin and Jetpack Compose**