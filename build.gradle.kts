// Top-level build file
plugins {
    id("com.android.application") version "8.2.0" apply false
    kotlin("android") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.18" apply false
    // Static analysis tools
    id("io.gitlab.arturbosch.detekt") version "1.23.7" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1" apply false
}
