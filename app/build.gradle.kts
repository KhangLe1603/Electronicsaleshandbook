plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.customerlistapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.customerlistapp"
        minSdk = 24
        targetSdk = 35
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

    packaging {
        resources.excludes.add("META-INF/INDEX.LIST")
        resources.excludes.add("META-INF/DEPENDENCIES") // Thêm dòng này để loại trừ DEPENDENCIES
    }
}

dependencies {
    // Các thư viện cơ bản
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Các thư viện của Google Sheets API
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.http-client:google-http-client-gson:1.42.3")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.apis:google-api-services-sheets:v4-rev20220927-2.0.0")
    implementation ("com.google.http-client:google-http-client-jackson2:1.43.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.23.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okio:okio:3.0.0")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.20.0") // Hoặc phiên bản mới nhất
    implementation("com.google.auth:google-auth-library-credentials:1.20.0") // Hoặc phiên bản mới nhất
    implementation("com.google.apis:google-api-services-sheets:v4-rev20230222-1.32.1") // Hoặc phiên bản mới nhất
    implementation(platform("com.google.firebase:firebase-bom:32.7.0")) // Thay đổi phiên bản nếu cần
    implementation("com.google.firebase:firebase-analytics")
    implementation("log4j:log4j:1.2.17") // Hoặc phiên bản mới nhất
    implementation("com.google.api-client:google-api-client:1.32.1") // Hoặc phiên bản mới nhất
    implementation("com.google.apis:google-api-services-sheets:v4-rev581-1.25.0") // Hoặc phiên bản mới nhất
    implementation("com.google.auth:google-auth-library-oauth2-http:1.17.0") // Hoặc phiên bản mới nhất
    implementation("com.google.api-client:google-api-client-jackson2:1.32.1") // Hoặc phiên bản mới nhất
    implementation("com.google.auth:google-auth-library-oauth2-http")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0") // Hoặc phiên bản mới nhất
    implementation("androidx.appcompat:appcompat:1.7.0") // Ví dụ
    implementation("androidx.core:core-ktx:1.12.0") // Ví dụ
}