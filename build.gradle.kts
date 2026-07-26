import java.util.Properties

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath("com.github.mwiede:jsch:0.2.17") }
}

plugins {
    java
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.6.1" apply false
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    group = "github.nighter"
    version = "1.7.1"

    repositories {
        mavenCentral()
        maven {
            name = "papermc-repo"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
        maven {
            name = "sonatype-public"
            url = uri("https://oss.sonatype.org/content/groups/public/")
        }
        maven {
            name = "opencollabRepositoryMain"
            url = uri("https://repo.opencollab.dev/main")
        }
        maven {
            name = "jitpack"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "enginehub"
            url = uri("https://maven.enginehub.org/repo/")
        }
        ivy {
            name = "townyGitHubReleases"
            url = uri("https://github.com/TownyAdvanced/Towny/releases/download")
            patternLayout {
                artifact("[revision]/[artifact]-[revision].[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("com.palmergames.bukkit.towny", "towny")
            }
        }
        maven {
            name = "bg-repo"
            url = uri("https://repo.bg-software.com/repository/api/")
        }
        maven {
            name = "codemc"
            url = uri("https://repo.codemc.io/repository/bentoboxworld/")
        }
        maven {
            name = "nightexpress-releases"
            url = uri("https://repo.nightexpressdev.com/releases")
        }
        ivy {
            name = "iridiumSkyblockGitHubReleases"
            url = uri("https://github.com/Iridium-Development/IridiumSkyblock/releases/download")
            patternLayout {
                artifact("[revision]/[artifact]-[revision].[ext]")
            }
            metadataSources {
                artifact()
            }
            content {
                includeModule("com.iridium", "IridiumSkyblock")
            }
        }
        maven {
            name = "lumineReleases"
            url = uri("https://mvn.lumine.io/repository/maven-public/")
            content {
                includeGroup("io.lumine")
            }
        }
        maven {
            name = "groupez"
            url = uri("https://repo.groupez.dev/releases")
        }
        maven {
            name = "minecodes-repository-releases"
            url = uri("https://maven.minecodes.pl/releases")
        }
        maven {
            name = "william278Releases"
            url = uri("https://repo.william278.net/releases")
        }
        maven {
            name = "factionsuuid"
            url = uri("https://dependency.download/releases")
            content {
                includeGroup("dev.kitteh")
            }
        }
        maven {
            name = "codemc-public"
            url = uri("https://repo.codemc.org/repository/maven-public/")
            content {
                includeGroup("nl.rutgerkok")
            }
        }
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        withJavadocJar()
        withSourcesJar()
    }
}

val targetJavaVersion = 25
allprojects {
    java {
        val javaVersion = JavaVersion.toVersion(targetJavaVersion)
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        if (JavaVersion.current() < javaVersion) {
            toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
            options.release.set(targetJavaVersion)
        }
    }
}

// ---------------------------------------------------------------------------
// SFTP deployment — configure servers in gitignored 'deploy.properties'
// ---------------------------------------------------------------------------

val deployPropsFile = rootProject.file("deploy.properties")
val deployProps = Properties()
if (deployPropsFile.exists()) deployPropsFile.reader().use { deployProps.load(it) }

val deployServerNames = (deployProps.getProperty("servers", ""))
    .split(",").map { it.trim() }.filter { it.isNotEmpty() }

fun ensureRemoteDir(channel: com.jcraft.jsch.ChannelSftp, remoteDir: String) {
    var path = ""
    remoteDir.split("/").filter { it.isNotEmpty() }.forEach { part ->
        path += "/$part"
        try { channel.mkdir(path) } catch (_: Exception) {}
    }
}

fun sftpUpload(host: String, port: Int, user: String, password: String, localFile: File, remoteDir: String) {
    val session = com.jcraft.jsch.JSch().getSession(user, host, port)
    session.setPassword(password)
    session.setConfig("StrictHostKeyChecking", "no")
    session.connect(15000)
    try {
        val channel = session.openChannel("sftp") as com.jcraft.jsch.ChannelSftp
        channel.connect(15000)
        try {
            ensureRemoteDir(channel, remoteDir)
            channel.put(localFile.absolutePath, "$remoteDir/${localFile.name}")
        } finally { channel.disconnect() }
    } finally { session.disconnect() }
}

val sftpDeployAll = tasks.register("sftpDeployAll") {
    group = "Pterodactyl deployment"
    description = "Builds and uploads the plugin jar to every server in deploy.properties."
}

tasks.register("deploy") { dependsOn("sftpDeployAll") }

deployServerNames.forEach { name ->
    val serverTask = tasks.register("sftpDeployTo${name.replaceFirstChar { it.uppercase() }}") {
        group = "Pterodactyl deployment"
        description = "Uploads RavenSpawners to '$name' via SFTP."
        dependsOn(":core:shadowJar")
        doLast {
            val host      = deployProps.getProperty("$name.host")
                ?: throw GradleException("Missing $name.host in deploy.properties")
            val port      = deployProps.getProperty("$name.port", "2022").toInt()
            val user      = deployProps.getProperty("$name.user")
                ?: throw GradleException("Missing $name.user in deploy.properties")
            val password  = deployProps.getProperty("$name.password")
                ?: throw GradleException("Missing $name.password in deploy.properties")
            val remoteDir = deployProps.getProperty("$name.remoteDir", "/plugins")

            val jarFile = project(":core").tasks.named("shadowJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class).get().archiveFile.get().asFile
            logger.lifecycle("Uploading ${jarFile.name} to '$name' ($host:$port$remoteDir)")
            sftpUpload(host, port, user, password, jarFile, remoteDir)
            logger.lifecycle("Uploaded ${jarFile.name} to '$name'")
        }
    }
    sftpDeployAll.configure { dependsOn(serverTask) }
}
