// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap/") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.0-alpha04")
        classpath(kotlin("gradle-plugin", version = "1.4.0-rc"))
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
        classpath("net.saliman:gradle-properties-plugin:1.5.1")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
        maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap/") }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.FlowPreview"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}