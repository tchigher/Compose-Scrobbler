plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)
    }

    val lastfmKey: String? by project
    val lastFmSecret: String? by project
    val spotifyAuth: String? by project

    buildTypes {
        buildTypes.forEach {
            it.buildConfigField("String", "LASTFM_API_KEY", lastfmKey ?: "\"\"")
            it.buildConfigField("String", "LASTFM_SECRET", lastFmSecret ?: "\"\"")
            it.buildConfigField("String", "SPOTIFY_AUTH", spotifyAuth ?: "\"\"")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(Kotlin.stdlib.jdk7)
    api(JakeWharton.timber)
}