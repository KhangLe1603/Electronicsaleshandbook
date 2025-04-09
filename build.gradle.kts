plugins {
    alias(libs.plugins.android.application) apply false
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2") // Thay đổi phiên bản nếu cần
        classpath("com.google.gms:google-services:4.4.0") // Thay đổi phiên bản nếu cần
    }
}

allprojects {
    repositories {
    }
}