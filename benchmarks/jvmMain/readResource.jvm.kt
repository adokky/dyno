package dev.dokky.dyno

actual fun readResource(name: String): ByteArray? {
    return SingleThreadedReadBenchmark::class.java.classLoader.getResourceAsStream(name)?.readAllBytes()
}