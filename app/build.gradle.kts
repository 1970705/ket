plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.22"
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android") version "2.48"
    // Static analysis
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
    // Code coverage - JaCoCo plugin
    jacoco
}

android {
    compileSdk = 34
    namespace = "com.wordland"

    defaultConfig {
        applicationId = "com.wordland"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Enable Compose compiler metrics for stability analysis
        freeCompilerArgs +=
            listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.metrics:destination=" +
                    "${project.projectDir}/build/compose_metrics",
            )
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Disable built-in coverage to use JaCoCo manually
            // This prevents double instrumentation issues
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
        }
        release {
            isMinifyEnabled = false
        }
        // Benchmark build type for Macrobenchmark and Microbenchmark tests
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }

    testOptions {
        // Disable coverage for unit tests to prevent JaCoCo double instrumentation
        // We'll use JaCoCo manually through jacocoTestReport task
        unitTests {
            isIncludeAndroidResources = false
            isReturnDefaultValues = true
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }

    // Room schema export directory for KSP
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.activity:activity-compose:1.6.1")

    // Core KTX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // AppCompat (required for AppCompatActivity)
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Serialization for Achievement system
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Performance Monitoring & Benchmarking
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // LeakCanary for memory leak detection (debug only)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    testImplementation("com.google.truth:truth:1.1.5")
    // Robolectric for local Android testing - Epic #12 Task #10: Upgraded to 4.16.1 for Compose compatibility
    testImplementation("org.robolectric:robolectric:4.16.1")
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")
    // Use same version as androidTest for consistency
    testImplementation("androidx.compose.ui:ui-test-junit4:1.5.0")
    testImplementation("androidx.compose.ui:ui-test-manifest:1.5.0")

    // Android Testing
    androidTestImplementation("androidx.test.ext:junit:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.7.6")
    androidTestImplementation("androidx.benchmark:benchmark-macro-junit4:1.2.3")
}

// KtLint configuration
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set("1.0.1")
    debug.set(false)
    verbose.set(true)
    android.set(true)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)

    // Disable rules that are too strict
    disabledRules.set(setOf("no-wildcard-imports", "discouraged-comment-location"))
}

// JaCoCo configuration for test coverage
tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest", "testReleaseUnitTest", "testBenchmarkUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter =
        listOf(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "android/**/*.*",
            "**/databinding/**",
            "**/android/databinding/**",
            "**/androidx/databinding/**",
            "**/di/**",
            "**/inject/**",
            // Exclude Kotlin value classes and inline classes that JaCoCo cannot instrument
            "**/DeviceInfo.*",
            "**/DeviceInfo\$*.*",
            "**/performance/DeviceInfo*",
        )

    val debugTree =
        fileTree("${project.buildDir}/tmp/kotlin-classes/debug") {
            exclude(fileFilter)
        }

    val mainSrc = "${project.projectDir}/src/main/java"

    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))

    // Use both possible exec file locations
    executionData.setFrom(
        files(
            fileTree("${project.buildDir}/jacoco/") {
                include("*.exec")
            },
            fileTree("${project.buildDir}/outputs/unit_test_code_coverage/") {
                include("**/*.exec")
            },
        ),
    )
}
