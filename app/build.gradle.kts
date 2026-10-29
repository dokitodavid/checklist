plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Versão da release (1.0, 1.1, …), passada pelo GitHub Actions com -PversionCode e -PversionName
val codigoVersao = (findProperty("versionCode") as String?)?.toInt() ?: 1
val nomeVersao = (findProperty("versionName") as String?) ?: "1.0"

android {
    namespace = "com.dkt.todo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dkt.todo"
        minSdk = 26
        targetSdk = 35
        versionCode = codigoVersao
        versionName = nomeVersao
    }

    // Chave fixa: assim cada APK novo instala por cima do anterior sem perder as tarefas
    signingConfigs {
        create("fixa") {
            storeFile = file("tarefas.keystore")
            storePassword = "tarefas123"
            keyAlias = "tarefas"
            keyPassword = "tarefas123"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("fixa")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("fixa")
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
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.14.1")
    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    // Segurar e arrastar para reordenar a lista
    implementation("sh.calvin.reorderable:reorderable:2.4.3")
}
