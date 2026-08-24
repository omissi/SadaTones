# Sada Tones

Sada Tones is an independent Android personalization app for ringtones, notification sounds, alarm tones, contact ringtones, SMS-style notification sounds, wallpapers, favorites, local downloads, request drafts, and prompt-based AI-style generated content.

The app is built with:

- Java
- Native Android Views
- Gradle / Android Gradle Plugin
- Android SDK 35 target
- GitHub Actions APK build workflow

## Implemented screens

- Home / Ringtones
- Wallpaper catalog
- Favorites
- AI Studio
- Request ringtone
- Settings
- Pro subscription mockup
- Tone detail dialog
- Wallpaper detail dialog
- More menu

## Implemented features

- Generated ringtone catalog across 20+ categories.
- Generated wallpaper catalog across multiple HD categories.
- Search and category filters.
- Preview tones with MediaPlayer.
- Generate original WAV tones on-device.
- Download tones for offline use.
- Set default ringtone.
- Set notification sound.
- Set alarm sound.
- Set SMS-style notification sound.
- Pick a contact and assign a custom ringtone.
- Share tone files with FileProvider.
- Favorite tones and wallpapers.
- Generate prompt-based AI-style tone files.
- Generate prompt-based AI-style wallpapers.
- Save wallpapers to Pictures.
- Set wallpaper using WallpaperManager.
- Share wallpapers.
- Save ringtone request drafts locally.
- Settings with app behavior toggles.
- Pro screen ready for Google Play Billing integration.

## Build with GitHub Actions

Push this project to GitHub, open Actions, and run `Build APK`.

The workflow uploads:

```text
SadaTones-debug-apk
```

APK path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Important compliance note

This project does not copy proprietary app code, music files, wallpaper files, icons, names, screenshots, or backend services from another app. It implements the same general product category and public feature set with original generated content and a separate identity.
