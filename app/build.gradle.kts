// build.gradle.kts (Module: app)
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.damn_practica6"
    compileSdk = 35 // Actualizado a 35 según tu archivo

    defaultConfig {
        applicationId = "com.example.damn_practica6"
        minSdk = 24
        targetSdk = 35 // Actualizado a 35 según tu archivo
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // vectorDrawables ya no es necesario aquí con Compose y minSdk 24
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11" // Actualizado a "11" según tu archivo
    }
    // Habilitar Jetpack Compose
    buildFeatures {
        compose = true
    }
    composeOptions {
        // Asegúrate de que esta versión sea compatible con tu versión de Kotlin
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Dependencias por defecto de AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat) // Mantener si usas vistas tradicionales en alguna parte
    implementation(libs.material) // Mantener si usas vistas tradicionales en alguna parte
    implementation(libs.androidx.constraintlayout) // Mantener si usas vistas tradicionales en alguna parte

    // Dependencias de Jetpack Compose UI
    // BOM (Bill of Materials) para gestionar versiones de Compose
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3") // Para Material Design 3

    // Para usar Compose en Activity
    implementation("androidx.activity:activity-compose:1.7.0")

    // Dependencia para iconos extendidos de Material Design (incluye LightMode y DarkMode)
    implementation("androidx.compose.material:material-icons-extended")
    // Para WebView en Jetpack Compose
    implementation("com.google.accompanist:accompanist-webview:0.34.0") // O la versión más reciente


    // Dependencias de prueba
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
