plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

// ponytail: aplica o google-services só quando o google-services.json existir,
// para o projeto compilar enquanto a configuração do Console (ação do usuário) não chega.
if (file("google-services.json").exists()) {
  apply(plugin = "com.google.gms.google-services")
}

android {
  namespace = "com.example"
  compileSdk = 36

  defaultConfig {
    // ponytail: id sem o "e" final para bater com o pacote registrado no Firebase Console.
    applicationId = "com.example.checklistplaystor"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
  }
}

// Dependências mínimas: Compose, Material 3, DataStore e Lifecycle/ViewModel.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.kotlinx.coroutines.play.services)

  testImplementation(libs.junit)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
