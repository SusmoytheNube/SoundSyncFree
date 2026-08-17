# SoundSyncFree

A clean-room Android project intended as a local Wi-Fi synchronized audio playback MVP.

## Build without Android Studio

This repository includes GitHub Actions.

1. Create a GitHub repository.
2. Upload all files from this project.
3. Commit/push to `main` (or `master`).
4. Open **Actions** in GitHub.
5. Select **Build Android APK**.
6. Run **Run workflow** if it has not run automatically.
7. Open the completed workflow run.
8. Under **Artifacts**, download `SoundSyncFree-debug-apk`.
9. Extract the downloaded artifact and install the APK on your Android device.

## Important

This is a development/MVP project. It is not yet a feature-complete replacement for SoundSeeder.

The intended roadmap is:

- Reliable LAN device discovery
- Host/receiver roles
- Multi-device synchronized playback
- Configurable receiver count (not hard-coded to 16)
- Buffering and clock synchronization
- Synchronized play/pause/seek
- Individual receiver volume
- Local audio playback
- Properly permitted YouTube playback integration

YouTube functionality must comply with YouTube's applicable API, playback, and content-use requirements. The app should not attempt to download or bypass YouTube restrictions.

## Local development

Android Studio or a compatible Android/Gradle toolchain can be used locally. The GitHub Actions workflow is the recommended build path if you do not have Android Studio installed.
