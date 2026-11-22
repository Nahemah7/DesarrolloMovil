import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// La firma se lee de keystore.properties, que no va al repo: el almacén de claves se entrega junto
// al APK, no en el historial de Git. Si el archivo no está (una compañera clona el repo), el build
// de release falla con un mensaje claro en vez de producir un APK sin firmar.
val archivoFirma = rootProject.file("keystore.properties")
val datosFirma = Properties().apply {
    if (archivoFirma.exists()) archivoFirma.inputStream().use { load(it) }
}

android {
    namespace = "com.example.rinconsur"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.rinconsur"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Dirección del gateway. Por defecto va por el túnel de adb (adb reverse tcp:8090 tcp:8090),
        // que sirve igual en el emulador y en un teléfono por USB. Se cambia desde la pantalla de
        // conexión, sin recompilar, cuando se prueba por la red local.
        buildConfigField("String", "API_BASE", "\"http://localhost:8090/\"")
    }

    signingConfigs {
        if (datosFirma.isNotEmpty()) {
            create("release") {
                storeFile = rootProject.file(datosFirma.getProperty("storeFile"))
                storePassword = datosFirma.getProperty("storePassword")
                keyAlias = datosFirma.getProperty("keyAlias")
                keyPassword = datosFirma.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
            // Sin minificar: el APK de la entrega tiene que poder leerse en la defensa.
            isMinifyEnabled = false
            if (datosFirma.isNotEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        // Las pruebas de ViewModel corren en la JVM con Robolectric: necesitan los recursos y los
        // assets .sql para abrir la base igual que en el teléfono.
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    // Pantalla de arranque con el tema del sistema (API 31) y su equivalente en versiones anteriores.
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    // ViewModels en la JVM: Application real y SQLite de verdad, sin emulador.
    testImplementation("org.robolectric:robolectric:4.16")
    testImplementation("androidx.test:core-ktx:1.6.1")
    // Robolectric instrumenta clases con ASM: sin una versión reciente no entiende el bytecode
    // de las dependencias nuevas y todas las pruebas fallan con "major version".
    testImplementation("org.ow2.asm:asm:9.9.1")
    testImplementation("org.ow2.asm:asm-commons:9.9.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navegacion entre pantallas, iconos y ViewModel con estado observable
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.icons.extended)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.1")

    // Cliente HTTP contra los microservicios: Retrofit con Gson y OkHttp con tiempos de espera cortos.
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}