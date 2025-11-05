plugins {
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.quick.mpp)
    alias(libs.plugins.quick.publish)
}

dependencies {
    commonMainApi(project(":core"))
    commonMainImplementation(libs.karamelUtils.core)
}

mavenPublishing {
    pom {
        description = "A container for mapping KClass to instances"
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
            optIn("dyno.InternalDynoApi")
            optIn("dyno.ExperimentalDynoApi")
        }
    }

    js().browser {
        testTask {
            useMocha {
                timeout = "30000"
            }
        }
    }
}