# Kuroro

A modern, fast, and lightweight Android application for managing and reading your personal manga collection. This app is built with Kotlin and Jetpack Compose, designed to connect seamlessly with a private FastAPI backend.

## 📱 Features

- **Immersive OLED UI:** A smooth, true-black theme with vibrant purple accents, optimized for modern displays.
- **Dynamic Manga List:** Fetches your collection from a self-hosted API and displays it in a responsive, high-performance list.
- **Smart Sorting:** Automatically sorts mangas by the latest release date so you never miss an update.
- **Pull-to-Refresh:** Easily update your library with a standard swipe-down gesture.
- **One-Click Reading:** Jump directly to your manga's main page in the browser with the "Read Now" button.
- **High Performance:** Uses `LazyColumn` for efficient list rendering and **Coil** for smooth, asynchronous image loading.

## 🛠️ Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Networking:** [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
- **JSON Parsing:** [Moshi](https://github.com/square/moshi)
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
- **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (or newer)
- Android SDK 36 (Minimum SDK 26)
- A running instance of the [Manga FastAPI Backend](https://github.com/IAbrahamI/MangaMobileApp)

### Configuration

1. Clone the repository.
2. Open the project in Android Studio.
3. Update the `baseUrl` in `MangaRepository.kt` to point to your FastAPI server:
   ```kotlin
   // app/src/main/java/ch/privat_network/manga_app/data/MangaRepository.kt
   .baseUrl("https://your-api-url.com/")
   ```
4. Build and run the app on your device or emulator!

## 📸 UI Preview

| Home Screen | Manga Card Detail |
| :---: | :---: |
| *Fully immersive dark mode* | *Clear release info & quick access* |

---
*Developed with ❤️ as a personal project to learn Kotlin and modern Android development.*
