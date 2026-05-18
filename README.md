# Library

AMOLED dark music player for Android. Plays `.opus` files sourced from a password-protected ZIP archive.

## Features

- Material 3 dark / AMOLED UI (true black backgrounds, Material You dynamic color on Android 12+)
- 3-column library grid with rounded cover art and per-track red progress rings
- Spinning vinyl disk player with embedded album art, play/pause/skip, seekable progress bar
- Opens a **password-protected AES-256 ZIP** containing `.opus` files — password stored in EncryptedSharedPreferences
- Extracts embedded Vorbis comment metadata (title, artist, album, cover art)
- Min Android 8.0 (API 26), targets API 34

## Preparing your ZIP

```bash
# Create a password-protected ZIP with AES-256 encryption
zip --encrypt -r library.zip *.opus
```

On macOS / Linux, use `7z` for AES-256:
```bash
7z a -tzip -mem=AES256 -p"your_passphrase" library.zip *.opus
```

Transfer `library.zip` to your Android device (Downloads, Google Drive, etc.).

## Building

### Prerequisites
- JDK 17+
- Android SDK (API 34) — via Android Studio or `sdkmanager`
- Gradle 8.7 (downloaded automatically by the wrapper)

### First time — set up Gradle wrapper
```bash
gradle wrapper --gradle-version 8.7
chmod +x gradlew
```

### Debug APK
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (signed)

1. Generate a keystore:
```bash
keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias library
```

2. Set env vars:
```bash
export KEY_STORE_FILE=/path/to/release.jks
export KEY_STORE_PASSWORD=your_store_pass
export KEY_ALIAS=library
export KEY_PASSWORD=your_key_pass
```

3. Add to `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = file(System.getenv("KEY_STORE_FILE") ?: "")
        storePassword = System.getenv("KEY_STORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    release { signingConfig = signingConfigs.getByName("release") }
}
```

4. Build:
```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### Install to device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## CI / Releases

Pushing a `v*` tag triggers `.github/workflows/release.yml`:
- Builds a debug APK
- Creates a GitHub Release and attaches the APK

```bash
git tag v1.0.0
git push origin v1.0.0
```

## Project structure

```
app/src/main/java/dev/c4g7/library/
├── data/              # Track model, ZipExtractor, MetadataExtractor, SecurePreferences
├── viewmodel/         # LibraryViewModel, PlayerViewModel
└── ui/
    ├── theme/         # Material 3 AMOLED color scheme, typography
    ├── navigation/    # NavGraph + BottomNav
    ├── screens/       # LibraryScreen, PlayerScreen, SettingsScreen
    └── components/    # VinylDisk, TrackCard, ProgressRing
```
