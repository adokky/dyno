plugins {
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.quick.mpp)
    alias(libs.plugins.quick.publish)
}

repositories {
    google()
}

dependencies {
    commonMainApi(project(":dyno-core"))
    commonMainApi(libs.zeroJson.core)
    commonMainImplementation(libs.karamelUtils.core)
}

mavenPublishing {
    pom {
        description = "Zero JSON integration for dyno"
        inceptionYear = "2025"
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = false
        // In order to use kotlin.internal inference annotations
        // we need to suppress `INVISIBLE_REFERENCE` error
        freeCompilerArgs.add("-Xdont-warn-on-error-suppression")
    }

    sourceSets.configureEach {
        languageSettings {
            optIn("dev.dokky.dyno.InternalDynoApi")
            optIn("dev.dokky.dyno.ExperimentalDynoApi")
        }
    }

    js().browser {
        testTask {
            useKarma {
                useFirefox()
            }
        }
    }
}