plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.tfg.charmreader"
    compileSdk = 36


    viewBinding{
        enable = true
    }
    defaultConfig {
        applicationId = "com.tfg.charmreader"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    //Esto lo añado para saltar de inicio de sesion a registro
    //buildFeatures {
        //viewBinding true
    //}
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-crashlytics-ndk")
    implementation("com.google.firebase:firebase-analytics")
    //Libreria Epub
    implementation("nl.siegmann.epublib:epublib-core:3.1"){
        exclude(group = "xmlpull", module = "xmlpull")
    }
    //Para hacer el menu a ver si funciona
    //implementation ("com.google.android.material:material:1.11.0") // o la versión más reciente
    implementation("com.google.android.material:material:1.12.0") //La version más reciente

    //Para hacer peticiones HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Trabajar API externa
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    //Pantalla de carga con mi logo
    implementation("androidx.core:core-splashscreen:1.0.1")




}