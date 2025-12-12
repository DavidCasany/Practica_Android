plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // AFEGIT: Plugin necessari per a Room (Base de Dades)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.uvic.tf_202526.atarazaga_dcasany"
    compileSdk = 34 // Recomanat: fer servir 34 (UpsideDownCake) o 35, el 36 és molt nou (beta)

    defaultConfig {
        applicationId = "com.uvic.tf_202526.atarazaga_dcasany"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8 // Room sol demanar Java 8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // Llibreries per defecte
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- AFEGITS DEL PLA DE TREBALL ---

    // ROOM (Base de Dades) - Sessió 12
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler) // Processador d'anotacions

    // GSON (JSON) - Sessió 12
    implementation(libs.gson)

    // QR (ZXing) - Sessió 11
    implementation(libs.zxing.embedded)
    implementation(libs.zxing.core)
}