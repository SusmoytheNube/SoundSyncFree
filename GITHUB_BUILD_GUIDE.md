# GitHub build — no Android Studio required

## Create repository

On GitHub, create a new empty repository, for example:

`SoundSyncFree`

Do not add another README if this project already contains one.

## Upload

Upload the contents of this folder, including:

- `.github/workflows/build-apk.yml`
- `gradlew`
- `gradle/wrapper/...`
- `app/...`

The `.github` directory may be hidden by some file pickers, so make sure it is included.

## Build

Go to:

**GitHub → repository → Actions → Build Android APK → Run workflow**

After completion:

**workflow run → Artifacts → SoundSyncFree-debug-apk**

Download and extract the artifact to get the APK.

## If the workflow fails

The most common reason with an older MVP project is an outdated/missing Gradle wrapper or Android Gradle Plugin. The workflow log will show the exact error. Do not change the SDK versions blindly; use the failing step to identify the compatibility issue.
