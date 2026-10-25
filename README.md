# Kuroro

A fast, lightweight Android client for my personal manga collection. Built with Kotlin and Jetpack Compose, it talks to my self-hosted FastAPI backend, [MangaAPIServer](https://github.com/IAbrahamI/MangaAPIServer).

## 📱 Features

- **Immersive OLED UI:** True-black theme with purple accents.
- **Manga List:** Shows the collection from the server, newest updates first.
- **Add & Delete:** Add a manga by name (`POST /mangas/{name}`) or remove it with the trash icon (`DELETE /mangas/{name}`).
- **Pull-to-Refresh:** Swiping down makes the server scrape every manga for new chapters (`PUT /mangas`) and then reloads the list (`GET /mangas`). The current list stays visible meanwhile; errors show up as a snackbar.
- **Offline List:** The last list is saved on the phone and shown instantly on start. When the server is unreachable (e.g. VPN off) it stays visible with an "Offline · saved list from …" note instead of an error page.
- **New-Chapter Notifications:** Every ~3 h a light background check reads the list (`GET /mangas` only) and notifies about mangas whose latest chapter changed. It only sees chapters the server has already scraped.
- **One-Click Reading:** "Read Now" opens the manga's page in the browser.

## 🛠️ Tech Stack

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- **Networking:** [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/), JSON via [Moshi](https://github.com/square/moshi)
- **Images:** [Coil 3](https://coil-kt.github.io/coil/)
- **Background work:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Android:** minSdk 26, targetSdk 36, compileSdk 37
- Versions live in `gradle/libs.versions.toml`.

## ⚙️ Configuration

The server address is `BASE_URL` in `app/src/main/java/ch/privat_network/manga_app/data/ApiClient.kt`:
```kotlin
private const val BASE_URL = "https://your-api-url.com/"
```

## 🔨 Build

Everything needed for building lives in the git-ignored `.android-build/` folder: a project-local Android SDK (platform `android-37.0`, build-tools `37.0.0`, platform-tools) plus the Gradle caches. `.android-build/env.sh` points `ANDROID_HOME`, `ANDROID_SDK_ROOT`, `ANDROID_USER_HOME` and `GRADLE_USER_HOME` into that folder and uses the system Java 21.

```bash
. .android-build/env.sh && ./gradlew assembleRelease
```

The APK ends up in `app/build/outputs/apk/release/app-release.apk` (about 1.8 MB, minified with R8).

The release build is signed with the debug key, since the app is only installed on my own phone. That key lives in `.android-build/android-user/debug.keystore` and must be kept: updates only install over the existing app when they are signed with the same key.

## 📲 Install

The release folder is shared on my tailnet only:
```bash
tailscale serve --bg --set-path /kuroro "$PWD/app/build/outputs/apk/release"
```
Then open `https://cachyos.tail6e41f8.ts.net/kuroro/app-release.apk` on the phone (Tailscale on) and install it. After `./gradlew clean` the mapping may need to be set again.

Coming from v0.0.1: uninstall the old app first, because it was signed with a different key. Nothing is lost, the list lives on the server.

---
*Developed with ❤️ as a personal project to learn Kotlin and modern Android development.*
