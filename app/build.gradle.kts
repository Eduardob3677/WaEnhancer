import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.Locale

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.materialthemebuilder)
    alias(libs.plugins.kotlinAndroid)
}

fun getGitHashCommit(): String {
    return try {
        val processBuilder = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        val process = processBuilder.start()
        process.inputStream.bufferedReader().readText().trim()
    } catch (e: Exception) {
        "unknown"
    }
}

val gitHash: String = getGitHashCommit().uppercase(Locale.getDefault())

android {
    namespace = "com.wmods.wppenhacer"
    compileSdk = 36
    ndkVersion = "27.0.11902837 rc2"

    flavorDimensions += "version"

    productFlavors {
        create("whatsapp") {
            dimension = "version"
            applicationIdSuffix = ""
        }
        create("business") {
            dimension = "version"
            applicationIdSuffix = ".w4b"
            resValue("string", "app_name", "Wa Enhancer Business")
        }
    }

    defaultConfig {
        applicationId = "com.wmods.wppenhacer"
        minSdk = 28
        targetSdk = 34
        versionCode = 152
        versionName = "1.5.2-DEV ($gitHash)"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        signingConfigs.create("config") {
            val androidStoreFile = project.findProperty("androidStoreFile") as String?
            if (!androidStoreFile.isNullOrEmpty()) {
                storeFile = rootProject.file(androidStoreFile)
                storePassword = project.property("androidStorePassword") as String
                keyAlias = project.property("androidKeyAlias") as String
                keyPassword = project.property("androidKeyPassword") as String
            }
        }

        ndk {
            abiFilters.add("armeabi-v7a")
            abiFilters.add("arm64-v8a")
            abiFilters.add("x86_64")
            abiFilters.add("x86")
        }

    }

    packaging {
        resources {
            excludes += "META-INF/**"
            excludes += "okhttp3/**"
            excludes += "kotlin/**"
            excludes += "org/**"
            excludes += "**.properties"
            excludes += "**.bin"
        }
    }

    buildTypes {
        all {
            signingConfig =
                if (signingConfigs["config"].storeFile != null) signingConfigs["config"] else signingConfigs["debug"]
        }
        release {
            isMinifyEnabled = project.hasProperty("minify") && project.properties["minify"].toString().toBoolean()
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
        aidl = true
    }


    lint {
        disable += "SelectedPhotoAccess"
    }

    /**
     * Material Theme Builder Configuration
     *
     * This plugin generates Material3 (Material Design 3) color themes from a primary color.
     * It creates both light and dark theme overlays that can be applied on top of the base theme.
     *
     * Generated themes:
     * - ThemeOverlay.Light.MaterialGreen: Light variant with green primary color
     * - ThemeOverlay.Dark.MaterialGreen: Dark variant with green primary color
     *
     * These overlays are automatically selected based on the current theme mode:
     * - Day mode (values/theme_customs.xml): Uses ThemeOverlay.Light.MaterialGreen
     * - Night mode (values-night/theme.xml): Uses ThemeOverlay.Dark.MaterialGreen
     *
     * Material3 Dynamic Colors (Android 12+):
     * The base themes (Theme.Material3.DynamicColors.Light/Dark.Rikka) already support
     * dynamic colors from the user's wallpaper. The MaterialGreen overlay provides
     * a fallback/accent color for devices without dynamic color support.
     *
     * @see res/values/themes.xml for base theme definitions
     * @see res/values/theme_customs.xml for light theme overlay selection
     * @see res/values-night/theme.xml for dark theme overlay selection
     */
    materialThemeBuilder {
        themes {
            for ((name, color) in listOf(
                "Green" to "4FAF50"
            )) {
                create("Material$name") {
                    // Light theme overlay format - used in day mode
                    lightThemeFormat = "ThemeOverlay.Light.%s"
                    // Dark theme overlay format - used in night mode
                    darkThemeFormat = "ThemeOverlay.Dark.%s"
                    // Primary color for Material3 color scheme generation
                    primaryColor = "#$color"
                }
            }
        }
        // Add Material Design 3 color tokens (such as palettePrimary100) in generated theme
        // rikka.material >= 2.0.0 provides such attributes
        // These tokens enable full Material3 color system support
        generatePalette = true
    }

}

dependencies {
    implementation(libs.colorpicker)
    implementation(libs.dexkit)
    compileOnly(libs.libxposed.legacy)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.rikkax.appcompat)
    implementation(libs.rikkax.core)
    implementation(libs.material)
    implementation(libs.rikkax.material)
    implementation(libs.rikkax.material.preference)
    implementation(libs.rikkax.widget.borderview)
    implementation(libs.jstyleparser)
    implementation(libs.okhttp)
    implementation(libs.filepicker)
    implementation(libs.betterypermissionhelper)
    implementation(libs.bcpkix.jdk18on)
    implementation(libs.arscblamer)
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

configurations.all {
    exclude("org.jetbrains", "annotations")
    exclude("androidx.appcompat", "appcompat")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk7")
    exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
}

interface InjectedExecOps {
    @get:Inject val execOps: ExecOperations
}


afterEvaluate {
    listOf("installWhatsappDebug", "installBusinessDebug").forEach { taskName ->
        tasks.findByName(taskName)?.doLast {
            runCatching {
                val injected  = project.objects.newInstance<InjectedExecOps>()
                runBlocking {
                    injected.execOps.exec {
                        commandLine(
                            "adb",
                            "shell",
                            "am",
                            "force-stop",
                            project.properties["debug_package_name"]?.toString()
                        )
                    }
                    delay(500)
                    injected.execOps.exec {
                        commandLine(
                            "adb",
                            "shell",
                            "monkey",
                            "-p",
                            project.properties["debug_package_name"].toString(),
                            "1"
                        )
                    }
                }
            }
        }
    }
}