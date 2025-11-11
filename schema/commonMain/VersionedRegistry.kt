package dyno

import kotlin.jvm.JvmInline

@JvmInline
internal value class VersionedRegistry<T: Any>(val nameToVersions: HashMap<String, Info<T>> = HashMap()) {
    class Info<T: Any>(
        var version: Int,
        var latest: T,
        val versions: HashMap<Int, T>
    )

    fun get(name: String, version: Int): T? {
        val info = nameToVersions[name] ?: return null
        return when {
            version >= 0 -> info.versions[version]
            else -> info.latest
        }
    }

    fun tryPut(
        name: String,
        version: Int,
        data: T
    ): Boolean {
        checkVersionPositive(version)
        val info = nameToVersions[name]
        if (info == null) {
            nameToVersions[name] = Info(version, data, HashMap(4)).apply {
                versions[version] = data
            }
            return true
        }

        if (version in info.versions) return false

        info.versions[version] = data
        info.updateLatest(version, data)
        return true
    }

    fun overwrite(
        name: String,
        version: Int,
        data: T
    ): T? {
        checkVersionPositive(version)
        val info = nameToVersions.getOrPut(name) {
            Info(version.coerceAtLeast(0), data, HashMap(4))
        }
        info.updateLatest(version, data)
        return info.versions.put(version, data)
    }

    fun remove(name: String, version: Int): T? {
        checkVersionPositive(version)
        val info = nameToVersions[name] ?: return null
        val removed = info.versions.remove(version) ?: return null
        if (info.versions.isEmpty()) {
            nameToVersions.remove(name)
        } else {
            val (maxVersion, latest) = info.versions.entries.maxBy { it.key }
            info.version = maxVersion
            info.latest = latest
        }
        return removed
    }

    private fun Info<T>.updateLatest(version: Int, data: T) {
        if (version < this.version) return
        this.version = version
        latest = data
    }

    private fun checkVersionPositive(version: Int) {
        require(version >= 0) { "'version' should be positive: $version" }
    }
}