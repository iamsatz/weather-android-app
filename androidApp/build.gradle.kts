plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

import java.net.URL
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.kosmos.android"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.kosmos.android"
        minSdk = 26
        targetSdk = 34
        versionCode = 18
        versionName = "6.0.0"
        buildConfigField(
            "String",
            "TOMORROW_API_KEY",
            "\"${localProperties.getProperty("TOMORROW_API_KEY", "")}\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            outputImpl.outputFileName = when (buildType.name) {
                "debug" -> "KosmosAlpha.apk"
                else -> "KosmosAlpha-${buildType.name}.apk"
            }
        }
    }
}

dependencies {
    implementation(project(":shared"))

    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.android.billingclient:billing-ktx:6.1.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.compose.ui:ui-viewbinding")

    implementation("androidx.glance:glance-appwidget:1.0.0")
    implementation("androidx.glance:glance-material3:1.0.0")

    implementation("io.ktor:ktor-client-okhttp:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("io.coil-kt:coil-compose:2.5.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

val fontDir = file("src/main/res/font")

fun isValidTtf(file: java.io.File): Boolean {
    if (!file.exists() || file.length() < 4) return false
    file.inputStream().use { stream ->
        val header = ByteArray(4)
        if (stream.read(header) != 4) return false
        val isTrueType = header[0] == 0x00.toByte() && header[1] == 0x01.toByte()
        val isOpenType = header[0] == 'O'.code.toByte() && header[1] == 'T'.code.toByte()
        val isTrue = header[0] == 0x74.toByte() && header[1] == 0x72.toByte()
        return isTrueType || isOpenType || isTrue
    }
}

tasks.register("downloadFonts") {
    group = "kosmos"
    description = "Download Inter + Playfair TTFs from Fontsource CDN"
    outputs.dir(fontDir)
    doLast {
        fontDir.mkdirs()
        val fonts = linkedMapOf(
            "inter_regular.ttf" to "https://cdn.jsdelivr.net/fontsource/fonts/inter@5.0.16/latin-400-normal.ttf",
            "inter_medium.ttf" to "https://cdn.jsdelivr.net/fontsource/fonts/inter@5.0.16/latin-500-normal.ttf",
            "inter_semibold.ttf" to "https://cdn.jsdelivr.net/fontsource/fonts/inter@5.0.16/latin-600-normal.ttf",
            "playfair_display_regular.ttf" to "https://cdn.jsdelivr.net/fontsource/fonts/playfair-display@5.0.19/latin-400-normal.ttf",
            "playfair_display_bold.ttf" to "https://cdn.jsdelivr.net/fontsource/fonts/playfair-display@5.0.19/latin-700-normal.ttf",
        )
        fonts.forEach { (name, url) ->
            val dest = fontDir.resolve(name)
            if (dest.exists() && isValidTtf(dest)) return@forEach
            val bytes = URL(url).readBytes()
            dest.writeBytes(bytes)
            check(isValidTtf(dest)) { "Downloaded font is invalid: $name" }
        }
    }
}

tasks.register("verifyFonts") {
    group = "kosmos"
    description = "Fail the build if bundled fonts are missing or corrupt"
    dependsOn("downloadFonts")
    doLast {
        val required = listOf(
            "inter_regular.ttf",
            "inter_medium.ttf",
            "inter_semibold.ttf",
            "playfair_display_regular.ttf",
            "playfair_display_bold.ttf",
        )
        required.forEach { name ->
            val file = fontDir.resolve(name)
            check(file.exists()) { "Missing font: $name — run ./gradlew downloadFonts" }
            check(isValidTtf(file)) {
                "Corrupt font: $name (likely an HTML error page). Run ./gradlew downloadFonts"
            }
        }
    }
}

tasks.named("preBuild") {
    dependsOn("verifyFonts")
}
