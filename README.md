# Library

AMOLED dark music player for Android. Plays `.opus` files sourced from a password-protected ZIP archive.

## Features

- **Material 3** dark / AMOLED UI — true black backgrounds, dynamic color on Android 12+
- **3-column grid** with rounded cover arts and per-track red progress rings
- **Spinning vinyl disk** player with embedded album art, play/pause/skip, seekable progress bar
- **Password-protected AES-256 ZIP** as the music source — passphrase stored in `EncryptedSharedPreferences`
- Extracts embedded Vorbis comment metadata (title, artist, album, cover art) via `MediaMetadataRetriever`
- Min Android 8.0 (API 26), targets API 34

## Preparing your ZIP

Pack your `.opus` files into an AES-256 encrypted ZIP:

```bash
# Using 7-Zip (recommended — proper AES-256)
7z a -tzip -mem=AES256 -p"your_passphrase" library.zip *.opus

# Using zip on Linux/macOS (standard encryption)
zip --encrypt -r library.zip *.opus
```

Transfer `library.zip` to your Android device (Downloads, Files app, etc.).

## Building

### Prerequisites

| Tool | Version |
|------|---------|
| JDK | 17+ |
| Android SDK | API 34 (via Android Studio or `sdkmanager`) |
| Gradle | 8.7 (auto-downloaded) |

### First-time setup — generate Gradle wrapper

```bash
gradle wrapper --gradle-version 8.7
chmod +x gradlew
```

### Debug APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

Install via ADB:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (signed)

1. **Generate a keystore** (one-time):

```bash
keytool -genkey -v \
  -keystore release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias library
```

2. **Add signing config** to `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile     = file(System.getenv("KEY_STORE_FILE") ?: "release.jks")
        storePassword = System.getenv("KEY_STORE_PASSWORD")
        keyAlias      = System.getenv("KEY_ALIAS")
        keyPassword   = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

3. **Build**:

```bash
export KEY_STORE_FILE=release.jks
export KEY_STORE_PASSWORD=your_store_pass
export KEY_ALIAS=library
export KEY_PASSWORD=your_key_pass

./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

## CI / GitHub Releases

Push a `v*` tag to trigger `.github/workflows/release.yml`. It builds a debug APK and attaches it to a GitHub Release automatically.

```bash
git tag v1.0.0
git push origin v1.0.0
```

Or trigger manually via **Actions → Release → Run workflow** and enter a tag name.

### Signed releases from CI

Add these secrets to your GitHub repo (**Settings → Secrets → Actions**):

| Secret | Value |
|--------|-------|
| `KEY_STORE_BASE64` | `base64 release.jks` |
| `KEY_STORE_PASSWORD` | keystore password |
| `KEY_ALIAS` | key alias |
| `KEY_PASSWORD` | key password |

Then update the workflow to decode the keystore and pass env vars to Gradle.

## Using the app

1. Launch → **Settings** tab
2. Tap the folder icon → pick your `.zip` file
3. Enter your decrypt passphrase → **Unlock & Load Library**
4. Browse the grid → tap any track to open the player
5. Vinyl spins, progress ring updates as you listen

## Project structure

```
app/src/main/java/dev/c4g7/library/
├── data/
│   ├── Track.kt                  — data model
│   ├── ZipExtractor.kt           — decrypts & extracts .opus from ZIP
│   ├── MetadataExtractor.kt      — reads title/artist/cover via MediaMetadataRetriever
│   └── SecurePreferences.kt      — EncryptedSharedPreferences wrapper
├── viewmodel/
│   ├── LibraryViewModel.kt       — loads tracks, tracks listen progress
│   └── PlayerViewModel.kt        — ExoPlayer wrapper, playback state
└── ui/
    ├── theme/                    — Material 3 AMOLED colors, typography
    ├── navigation/NavGraph.kt    — bottom nav + NavHost
    ├── screens/
    │   ├── LibraryScreen.kt      — 3-column grid + ZIP unlock dialog
    │   ├── PlayerScreen.kt       — vinyl disk + controls
    │   └── SettingsScreen.kt     — source path + passphrase + toggles
    └── components/
        ├── VinylDisk.kt          — spinning record with cover art (Compose Canvas + Animatable)
        ├── TrackCard.kt          — grid cell with cover art + ProgressRing
        └── ProgressRing.kt       — SVG arc progress indicator
```
