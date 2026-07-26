import java.net.URI
import java.time.LocalDate

plugins {
    id("com.gradleup.shadow")
}

val shade: Configuration by configurations.creating
configurations {
    implementation.get().extendsFrom(shade)
}

dependencies {
    api(project(":api"))
    compileOnly(fileTree("libs") { include("*.jar") })

    @Suppress("GradleDependency")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    shade("com.zaxxer:HikariCP:7.1.0")
    shade("org.mariadb.jdbc:mariadb-java-client:3.5.9")
    compileOnly("org.xerial:sqlite-jdbc:3.53.2.0")

    compileOnly("org.geysermc.floodgate:api:2.2.5-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
    compileOnly("com.github.brcdev-minecraft:shopgui-api:3.2.0") {
        exclude(group = "*")
    }
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("su.nightexpress.excellenteconomy:ExcellentEconomy:2.8.0")
    compileOnly("su.nightexpress.nightcore:main:2.16.4")
    compileOnly("com.github.Gypopo:EconomyShopGUI-API:1.10.1")
    compileOnly("fr.maxlego08.shop:zshop-api:3.3.4")
    compileOnly("fr.maxlego08.menu:zmenu-api:1.1.1.5")
    compileOnly(fileTree(mapOf("dir" to rootProject.file("libs"), "include" to listOf("*.jar"))))

    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    shade("org.bstats:bstats-bukkit:3.2.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-nowarn", "-Xlint:-deprecation"))
}

tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

// Don't use 'jar' task to build plugin jar, use 'shadowJar' task instead
tasks.jar {
    archiveBaseName.set("RavenSpawnersJar")
    archiveVersion.set(version.toString())

    from(project(":api").sourceSets["main"].output)
    from(sourceSets["main"].output)
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
}

tasks.shadowJar {

    archiveBaseName.set("RavenSpawners")
    archiveVersion.set(version.toString())
    archiveClassifier.set("")
    from(project(":api").sourceSets["main"].output)
    configurations = listOf(shade)

    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    exclude("META-INF/maven/**")
    exclude("META-INF/MANIFEST.MF")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
    from(sourceSets["main"].output)
    exclude("org/slf4j/**")

    relocate("com.zaxxer.hikari", "github.nighter.ravenspawners.libs.hikari")
    relocate("org.mariadb.jdbc", "github.nighter.ravenspawners.libs.mariadb")
    relocate("org.bstats", project.group.toString())
    mergeServiceFiles()

    // destinationDirectory.set(file("C:\\Users\\Admin\\Desktop\\TestServer\\plugins"))
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand(props)
    }
}

tasks.register("generateLanguageChangelog") {
    group       = "documentation"
    description = "Diffs en_US language keys vs the latest GitHub release and prepends a changelog entry."

    val changelogFile = project.file("src/main/resources/language/CHANGELOG.txt")

    inputs.property("projectVersion", project.version.toString())
    outputs.file(changelogFile)

    doLast {
        val currentVersion = project.version.toString()
        val locale    = "en_US"
        val langFiles = listOf("messages.yml", "gui.yml", "command_gui.yml", "items.yml", "formatting.yml", "command_messages.yml", "hologram.yml")

        // ── 1. Fetch latest GitHub release tag ───────────────────────────────
        val githubVersion: String = try {
            val conn = URI.create(
                "https://api.github.com/repos/OpenVdra/RavenSpawners/releases/latest"
            ).toURL().openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
            conn.setRequestProperty("User-Agent", "RavenSpawners-Changelog-Bot/1.0")
            conn.connectTimeout = 6_000
            conn.readTimeout    = 6_000
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().readText()
                Regex(""""tag_name"\s*:\s*"v?([^"]+)"""").find(body)
                    ?.groupValues?.get(1) ?: "0.0.0"
            } else {
                println("[changelog] GitHub API returned HTTP ${conn.responseCode} – skipping.")
                return@doLast
            }
        } catch (e: Exception) {
            println("[changelog] ⚠ Cannot reach GitHub API (${e.message}) – skipping changelog update.")
            return@doLast
        }

        // ── 2. Compare versions ──────────────────────────────────────────────
        fun parseVer(v: String): List<Int> =
            v.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

        val cur = parseVer(currentVersion)
        val gh  = parseVer(githubVersion)
        var isNewer = false
        for (i in 0 until maxOf(cur.size, gh.size)) {
            val a = cur.getOrElse(i) { 0 }
            val b = gh.getOrElse(i) { 0 }
            if (a > b) { isNewer = true; break }
            if (a < b) break
        }

        if (!isNewer) {
            println("[changelog] Up-to-date (build=$currentVersion, github=$githubVersion) – nothing to add.")
            return@doLast
        }

        // ── 3. Guard against duplicates ──────────────────────────────────────
        val existing = if (changelogFile.exists()) changelogFile.readText() else ""
        if (existing.contains("── v$currentVersion")) {
            println("[changelog] Version $currentVersion already present – skipping.")
            return@doLast
        }

        // ── 3b. Resolve the "to" ref: use the version tag if it exists, else main ──
        val toRef: String = run {
            listOf(currentVersion, "v$currentVersion").firstOrNull { tag ->
                try {
                    val tagConn = URI.create(
                        "https://api.github.com/repos/OpenVdra/RavenSpawners/git/refs/tags/$tag"
                    ).toURL().openConnection() as java.net.HttpURLConnection
                    tagConn.requestMethod = "GET"
                    tagConn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    tagConn.setRequestProperty("User-Agent", "RavenSpawners-Changelog-Bot/1.0")
                    tagConn.connectTimeout = 6_000
                    tagConn.readTimeout    = 6_000
                    tagConn.responseCode == 200
                } catch (e: Exception) { false }
            } ?: "main"
        }
        println("[changelog] Compare range: $githubVersion...$toRef")

        // ── 4. YAML flat-key extractor ───────────────────────────────────────
        fun extractKeys(yaml: String): LinkedHashMap<String, Pair<Int, String>> {
            val result = LinkedHashMap<String, Pair<Int, String>>()
            val stack  = ArrayDeque<Pair<Int, String>>()
            var blockScalarIndent = -1

            yaml.lines().forEachIndexed { idx, raw ->
                val lineNo  = idx + 1
                val trimmed = raw.trimStart()
                if (trimmed.isEmpty() || trimmed.startsWith('#')) return@forEachIndexed

                val indent = raw.length - trimmed.length

                if (blockScalarIndent >= 0) {
                    if (indent > blockScalarIndent) return@forEachIndexed
                    else blockScalarIndent = -1
                }

                if (trimmed.startsWith("- ") || trimmed == "-") return@forEachIndexed

                val colonIdx = trimmed.indexOf(':')
                if (colonIdx < 0) return@forEachIndexed

                val key = trimmed.substring(0, colonIdx).trim()
                if (key.isEmpty() || key.startsWith('#')) return@forEachIndexed

                var value = trimmed.substring(colonIdx + 1).trim()

                if (!value.startsWith('"') && !value.startsWith('\'')) {
                    val ci = value.indexOf(" #")
                    if (ci >= 0) value = value.substring(0, ci).trim()
                }

                while (stack.isNotEmpty() && stack.last().first >= indent) stack.removeLast()

                val path = if (stack.isEmpty()) key
                           else stack.joinToString(".") { it.second } + ".$key"

                if (value == "|" || value == ">" || value == "|-" || value == ">-"
                        || value == "|+" || value == ">+") {
                    result[path] = lineNo to value
                    blockScalarIndent = indent
                } else {
                    result[path] = lineNo to value
                }

                stack.addLast(indent to key)
            }
            return result
        }

        fun truncate(s: String, max: Int = 70) = if (s.length > max) s.take(max) + "…" else s

        // ── 5. Fetch the old language files from GitHub and diff ─────────────
        fun fetchRaw(tag: String, file: String): String? = try {
            val url = "https://raw.githubusercontent.com/OpenVdra/" +
                      "RavenSpawners/$tag/core/src/main/resources/language/$locale/$file"
            val conn = URI.create(url).toURL().openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "RavenSpawners-Changelog-Bot/1.0")
            conn.connectTimeout = 8_000
            conn.readTimeout    = 8_000
            if (conn.responseCode == 200) conn.inputStream.bufferedReader().readText() else null
        } catch (e: Exception) { null }

        val addedPerFile   = mutableMapOf<String, List<String>>()
        val changedPerFile = mutableMapOf<String, List<String>>()
        val removedPerFile = mutableMapOf<String, List<String>>()

        for (file in langFiles) {
            val oldYaml = fetchRaw(githubVersion, file)
            if (oldYaml == null) {
                println("[changelog] ⚠ Could not fetch $file @ $githubVersion – skipping diff.")
                continue
            }
            val newFile = project.file("src/main/resources/language/$locale/$file")
            if (!newFile.exists()) continue

            val oldKeys = extractKeys(oldYaml)
            val newKeys = extractKeys(newFile.readText())

            val added = newKeys.entries
                .filter { it.key !in oldKeys }
                .sortedBy { it.key }
                .map { (k, e) -> "      - $k (L${e.first}): ${truncate(e.second)}" }

            val removed = oldKeys.entries
                .filter { it.key !in newKeys }
                .sortedBy { it.key }
                .map { (k, e) -> "      - $k (L${e.first}): ${truncate(e.second)}" }

            val changed = newKeys.entries
                .filter { it.key in oldKeys && oldKeys[it.key]!!.second != it.value.second }
                .sortedBy { it.key }
                .map { (k, newE) ->
                    val oldVal = truncate(oldKeys[k]!!.second)
                    val newVal = truncate(newE.second)
                    "      - $k (L${newE.first}): $oldVal → $newVal"
                }

            if (added.isNotEmpty())   addedPerFile[file]   = added
            if (changed.isNotEmpty()) changedPerFile[file] = changed
            if (removed.isNotEmpty()) removedPerFile[file] = removed

            println("[changelog]   $file – +${added.size} ~${changed.size} -${removed.size}")
        }

        // ── 6. Build the CHANGELOG entry ─────────────────────────────────────
        fun formatSection(label: String, perFile: Map<String, List<String>>): String {
            if (perFile.isEmpty()) return "  $label:\n    (none)"
            val sb = StringBuilder("  $label:\n")
            for ((file, lines) in perFile) {
                sb.appendLine("    $file:")
                lines.forEach { sb.appendLine(it) }
            }
            return sb.toString().trimEnd()
        }

        val today     = LocalDate.now().toString()
        val separator = "─".repeat(80 - "── v$currentVersion ($today) ".length)
        val newEntry  = buildString {
            appendLine("── v$currentVersion ($today) $separator")
            appendLine()
            appendLine("  Summary: Version $currentVersion released")
            appendLine("           Compare: https://github.com/OpenVdra/RavenSpawners/compare/$githubVersion...$toRef")
            appendLine()
            appendLine(formatSection("ADDED",   addedPerFile))
            appendLine()
            appendLine(formatSection("CHANGED", changedPerFile))
            appendLine()
            appendLine(formatSection("REMOVED", removedPerFile))
            appendLine()
        }

        // ── 7. Insert before the first existing version entry ────────────────
        val marker  = "\n──"
        val updated = if (existing.contains(marker)) {
            existing.replaceFirst(marker, "\n$newEntry──")
        } else {
            "$existing\n$newEntry"
        }

        changelogFile.writeText(updated)
        println("[changelog] ✓ Prepended entry for v$currentVersion into language/CHANGELOG.txt")
    }
}
