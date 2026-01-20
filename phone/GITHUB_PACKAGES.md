# GitHub Packages Publishing Guide

## Requirements

- JDK 17 or higher
- Android SDK

## Setup

1. **Generate GitHub Personal Access Token**:
   - Go to GitHub Settings → Developer settings → Personal access tokens
   - Generate new token with `write:packages` scope
   - Copy the token

2. **Set Environment Variable**:
   ```bash
   export GITHUB_TOKEN=your_personal_access_token
   ```

## Publishing Methods

### Method 1: Manual Publishing
```bash
./publish-github-packages.sh
```

### Method 2: Direct Gradle Command
```bash
./gradlew :phone:publish
```

### Method 3: GitHub Actions (Automatic)
- Create a GitHub release
- The workflow will automatically publish to GitHub Packages

## Using the Published Package

In your consuming project's `build.gradle.kts`:

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/mimoccc/doorbellAssistant")
        credentials {
            username = "your_github_username"
            password = "your_github_token"  // or use GITHUB_TOKEN env var
        }
    }
}

dependencies {
    implementation("org.mjdev:phone:1.0.0")
}
```

## Benefits of GitHub Packages

- ✅ No domain verification required
- ✅ Integrated with GitHub ecosystem
- ✅ Automatic version management
- ✅ Private package support
- ✅ Free for public repositories

## Package Location

Published packages will be available at:
```
https://maven.pkg.github.com/mimoccc/doorbellAssistant/org/mjdev/phone
```

Users can browse packages at:
```
https://github.com/mimoccc/doorbellAssistant/packages
```