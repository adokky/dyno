plugins {
    alias(libs.plugins.quick.mpp)
    alias(libs.plugins.kotlinx.benchmark)
    alias(libs.plugins.allopen)
}

dependencies {
    commonMainImplementation(project(":dyno-core"))
    commonMainImplementation(libs.karamelUtils.core)
    commonMainImplementation(libs.kotlinx.benchmark)
}

kotlin {
//    js {
//        nodejs()
//    }
    @Suppress("OPT_IN_USAGE")
    wasmJs {
        nodejs()
    }
}

benchmark {
    targets {
        register("jvm")
        register("linuxX64")
        register("wasmJs")
//        register("js")
    }
    configurations {
        named("main") {
            advanced("jvmForks", 1)
        }
    }
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}