plugins {
    // failed to apply kotlin plugin in subprojects without applying it in root first
    kotlin("multiplatform") version libs.versions.kotlin apply false
    alias(libs.plugins.kotlinx.serialization) apply false
}

group = "io.github.adokky"
version = "0.8"

subprojects {
    group = rootProject.group
    version = rootProject.version
}