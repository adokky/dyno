pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":dyno-core")
project(":dyno-core").projectDir = file("./core")

include(":dyno-classmap")
project(":dyno-classmap").projectDir = file("./classmap")

include(":dyno-schema")
project(":dyno-schema").projectDir = file("./schema")