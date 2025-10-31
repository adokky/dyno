import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.quick.mpp)
    alias(libs.plugins.quick.publish)
}

repositories {
    google()
}

dependencies {
    commonMainApi(libs.kotlinx.serialization.json)
    commonMainImplementation(libs.karamelUtils.core)
    commonMainImplementation(libs.objectPool)
    commonTestImplementation(libs.equalsTester)
    jvmTestImplementation(libs.zeroJson.kotlinx)
}

mavenPublishing {
    pom {
        description = "Type-safe serializable heterogeneous map"
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
            useKarma {
                useFirefox()
            }
        }
    }
}

kover.reports.verify {
    rule("minimum coverage") {
        minBound(90, CoverageUnit.LINE)
        minBound(85, CoverageUnit.BRANCH)
    }
}

configurations.all {
    val ktxSerializationProvider = if (this == configurations.jvmTestImplementation) "io.github.adokky" else "org.jetbrains.kotlinx"
    resolutionStrategy.capabilitiesResolution.withCapability("org.jetbrains.kotlinx:kotlinx-serialization-json") {
        select(candidates.single { (it.id as? ModuleComponentIdentifier)?.group == ktxSerializationProvider })
    }
}