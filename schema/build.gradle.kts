plugins {
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.quick.mpp)
    alias(libs.plugins.quick.publish)
}

dependencies {
    commonMainApi(project(":dyno-core"))
    commonMainImplementation(libs.karamelUtils.core)
    commonMainImplementation(libs.bitvector)
}

mavenPublishing {
    pom {
        description = "Schema definition DSL for dyno"
        inceptionYear = "2025"
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = false
        // In order to use kotlin.internal inference annotations
        // we need to suppress `INVISIBLE_REFERENCE` error
        freeCompilerArgs.add("-Xdont-warn-on-error-suppression")
        freeCompilerArgs.add("-Xnested-type-aliases")
    }

    sourceSets.configureEach {
        languageSettings {
            optIn("dyno.InternalDynoApi")
            optIn("dyno.ExperimentalDynoApi")
            optIn("dyno.UnsafeDynoApi")
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