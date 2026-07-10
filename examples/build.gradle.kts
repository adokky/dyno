plugins {
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.quick.mpp)
}

repositories {
    google()
}

dependencies {
    commonMainImplementation(project(":dyno-core"))
    commonMainImplementation(libs.zeroJson.core)
    commonMainImplementation(libs.zeroJson.kotlinx)
    commonMainImplementation(libs.karamelUtils.core)
}

configurations.configureEach {
    resolutionStrategy {
        exclude("org.jetbrains.kotlinx", "kotlinx-serialization-json")
    }
//    resolutionStrategy.dependencySubstitution {
//        val zjson = libs.zeroJson.kotlinx.get()
//        substitute(module("org.jetbrains.kotlinx:kotlinx-serialization-json"))
//            .using(module("${zjson.group}:${zjson.module.name}:${zjson.version}"))
//    }
}

kotlin {
    js().browser {
        testTask {
            useKarma {
                useFirefox()
            }
        }
    }
}