buildscript {
    ext {
        compileSdkVersion = 34
        minSdk = 26
        versionCode = 1
        versionName = "1.0"
        blackReflection = "2.0.0"
    }
}

plugins {
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
}
