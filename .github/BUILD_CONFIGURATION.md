# GitHub Actions Build Configuration

This document explains how to set up GitHub secrets for building signed release APKs.

## Required Secrets

To build signed release APKs, you need to configure the following secrets in your GitHub repository:

### 1. KEY_STORE (required)
The keystore file encoded in base64.

**How to create:**
```bash
# If you don't have a keystore, create one:
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias

# Encode your keystore to base64:
base64 my-release-key.jks > keystore-base64.txt
# On macOS:
base64 -i my-release-key.jks -o keystore-base64.txt
```

Copy the contents of `keystore-base64.txt` and add it as the `KEY_STORE` secret.

### 2. KEY_STORE_PASSWORD (required)
The password for the keystore file.

### 3. ALIAS (required)
The key alias used when creating the keystore.

### 4. KEY_PASSWORD (required)
The password for the key alias.

### 5. BOT_TOKEN (optional)
Telegram bot token for posting releases to a Telegram channel.

### 6. CHANNEL_ID (optional)
Telegram channel ID where releases should be posted.

## Setting Up GitHub Secrets

1. Go to your repository on GitHub
2. Click on **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret with its corresponding value

## Workflows

### Build Signed Release APK (build-release.yml)

This workflow builds signed release APKs and can be triggered in three ways:

1. **Manual trigger**: Go to Actions → Build Signed Release APK → Run workflow
2. **On tag push**: Push a tag like `v1.5.2`
   ```bash
   git tag v1.5.2
   git push origin v1.5.2
   ```
3. **On release branch push**: Push to a branch named `release/*`
   ```bash
   git checkout -b release/1.5.2
   git push origin release/1.5.2
   ```

**Outputs:**
- Signed WhatsApp variant APK
- Signed Business variant APK
- SHA256 checksums file
- GitHub Release (for tag pushes)
- Telegram notification (if configured)

### Android CI (android.yml)

The existing workflow builds debug APKs on every push to master.

## Build Process

The release workflow performs the following steps:

1. Checkout code
2. Set up JDK 17
3. Decode keystore from base64
4. Configure signing properties
5. Build release APKs (both WhatsApp and Business variants)
6. Generate SHA256 checksums
7. Upload APKs as artifacts (retained for 90 days)
8. Create GitHub Release (for tag pushes)
9. Post to Telegram (if configured)
10. Clean up keystore file

## Manual Build

To build signed APKs locally:

1. Create or use an existing keystore
2. Add the signing configuration to `gradle.properties`:
   ```properties
   androidStoreFile=/path/to/your/keystore.jks
   androidStorePassword=your_store_password
   androidKeyAlias=your_key_alias
   androidKeyPassword=your_key_password
   ```
3. Build the release APKs:
   ```bash
   ./gradlew assembleWhatsappRelease assembleBusinessRelease
   ```

The signed APKs will be in:
- `app/build/outputs/apk/whatsapp/release/app-whatsapp-release.apk`
- `app/build/outputs/apk/business/release/app-business-release.apk`

## Security Notes

- **Never commit** your keystore file or signing passwords to the repository
- Store keystore files securely - losing it means you can't update your app
- Keep your keystore password safe - it cannot be recovered if lost
- The workflow automatically cleans up the keystore file after building
- GitHub secrets are encrypted and not exposed in logs

## Troubleshooting

### "KEY_STORE secret is not set"
Make sure you've added the `KEY_STORE` secret in your repository settings.

### "Keystore was tampered with, or password was incorrect"
Check that your `KEY_STORE_PASSWORD` is correct.

### "Certificate chain not found for: alias"
Verify that the `ALIAS` matches the alias you used when creating the keystore.

### Build fails with signing errors
Ensure all four required secrets (`KEY_STORE`, `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD`) are set correctly.
