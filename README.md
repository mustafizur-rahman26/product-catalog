# Product Catalog - Android Application

[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.21-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2025.12.00-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Min API](https://img.shields.io/badge/API-24%2B-orange.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

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
- **Material 3**: Modern UI with Material Design 3 components

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
   - Jetpack Compose UI
   - ViewModels with StateFlow
   - UI State management

2. **Domain Layer** (`domain/`)
   - Domain models
   - Repository interfaces

3. **Data Layer** (`data/`)
   - Repository implementations
   - API services
   - Data mappers
   - Network error handling

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

## 🚀 Getting Started

### Configuration

The app uses DummyJSON API for product data. The base URL is configured in `app/build.gradle.kts`:

```kotlin
val baseUrl = "https://dummyjson.com/"
buildConfigField("String", "BASE_URL", "\"${baseUrl}\"")
```

## 🎨 UI Components

### ProductsScreen
- **ProductsContent**: Main composable displaying product grid
- **ProductGridItem**: Individual product card with image, brand, name, and price
- **Error States**: Empty state, initial error, and pagination error handling
- **Loading States**: Initial loading and load-more indicators

### ProductDetailScreen
- **Detailed Product View**: Full product information display
- **Product Information**: Description, category, brand, and pricing
- **Navigation**: Back navigation to product list

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
- Error state preservation during retry

### State Management
```kotlin
data class UiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 0,
    val nextPage: Int? = null
)
```

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
