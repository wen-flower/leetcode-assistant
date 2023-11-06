import org.jetbrains.changelog.markdownToHTML
import java.util.zip.ZipInputStream

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.serialization) // Gradle Kotlinx Serialization Plugin
}

group = properties("plugin.group")
version = properties("plugin.version")

repositories {
    maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
}

dependencies {
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientJava)
    implementation(libs.ktorClientContentNegotitation)
    implementation(libs.ktorClientLogging)
    implementation(libs.ktorKotlinxJson)
    implementation(libs.kotlinSerializationJson)
    implementation(libs.kotlinCoroutinesCore)
    implementation("io.ktor:ktor-client-logging-jvm:2.3.5")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

intellij {
    pluginName = properties("plugin.name")
    version = properties("platform.version")
    type = properties("platform.type")

    plugins = properties("platform.plugins").map { it.split(",").filter(String::isNotBlank).map(String::trim) }

    if (environment("DEFAULT_IDEA_SANDBOX").orNull != "true") {
        sandboxDir.set("${projectDir.path}/idea-sandbox")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }
    buildSearchableOptions {
        enabled = false
    }
    patchPluginXml {
        pluginId = properties("plugin.group")
        version = properties("plugin.version")

        sinceBuild = properties("plugin.since.build")
        untilBuild = properties("plugin.until.build")

        pluginDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asBytes
            .map { String(it, Charsets.UTF_8) }
            .map {
                val start = "[//]: # (PLUGIN DESCRIPTION START)"
                val end = "[//]: # (PLUGIN DESCRIPTION END)"

                with(it.lines()) {
                    if (!containsAll(listOf(start, end)))
                        throw GradleException("Plugin description section not found in README.md: \n$start...$end")
                    subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
                }
            }
    }
    runIde {
        doFirst {
            val destinationDir = File(intellij.sandboxDir.get() + "/plugins")
            destinationDir.listFiles()?.forEach { it.deleteRecursively() }
            ZipInputStream(File(environment("DYNAMIC_PLUGIN_PATH").get()).inputStream()).use {
                var entry = it.nextEntry
                val buffer = ByteArray(1024)

                while (entry != null) {
                    val entryFile = File(destinationDir, entry.name)

                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        val outputStream = entryFile.outputStream()
                        var bytesRead: Int
                        while (it.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                        }
                        outputStream.close()
                    }

                    entry = it.nextEntry
                }
            }
        }
        autoReloadPlugins.set(false)
//        jvmArgs = listOf(
//            "-XX:+UnlockDiagnosticVMOptions",
            // 启用热重载功能，使用Build菜单编译项目后无需重启调试进程即可完成, 仅支持JBR
//            "-XX:+AllowEnhancedClassRedefinition"
//        )
    }
}

kotlin {
    jvmToolchain(17)
}