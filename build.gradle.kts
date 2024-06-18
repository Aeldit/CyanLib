plugins {
    id("java")
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.+"
}

repositories {
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
    }
    maven("https://maven.terraformersmc.com/releases/") {
        name = "TerraformersMC"
    }
}

val modVersion = property("mod_version").toString()

val mcVersion = property("minecraft_version").toString()
val loaderVersion = property("loader_version").toString()
val javaVersion = property("java_version").toString()

val fabricVersion = property("fabric_version").toString()
val modmenuVersion = property("modmenu_version").toString()

val fullVersion = "${modVersion}+${mcVersion}"

dependencies {
    minecraft("com.mojang:minecraft:${mcVersion}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")

    // Fabric API
    fun addFabricModule(name: String) {
        val module = fabricApi.module(name, fabricVersion)
        modImplementation(module)
    }
    addFabricModule("fabric-resource-loader-v0")
    addFabricModule("fabric-command-api-v2")
    addFabricModule("fabric-lifecycle-events-v1")
    addFabricModule("fabric-key-binding-api-v1")
    addFabricModule("fabric-screen-api-v1")

    // ModMenu
    modImplementation("com.terraformersmc:modmenu:${modmenuVersion}")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    processResources {
        inputs.property("version", fullVersion)
        inputs.property("loader_version", loaderVersion)
        inputs.property("mc_version", mcVersion)
        inputs.property("java_version", javaVersion)

        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to fullVersion,
                    "loader_version" to loaderVersion,
                    "mc_version" to mcVersion,
                    "java_version" to javaVersion
                )
            )
        }
    }

    val releaseMod by registering {
        group = "mod"
        dependsOn("modrinth")
    }

    jar {
        from("LICENSE")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and just create separate worlds
    }
}

java {
    withSourcesJar()
    val is21 = javaVersion == "21"
    sourceCompatibility = if (is21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = if (is21) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))

    projectId.set("${property("archives_base_name")}")
    versionName.set("[${mcVersion}] CyanLib $modVersion")
    versionNumber.set(fullVersion)
    versionType.set("release")

    uploadFile.set(tasks.remapJar.get().archiveFile)
    additionalFiles.addAll(tasks.remapSourcesJar.get().archiveFile)

    gameVersions.addAll(mcVersion)
    loaders.add("fabric")

    dependencies {
        required.version("fabric-api", fabricVersion)
        required.version("modmenu", modmenuVersion)
    }

    changelog.set(
        rootProject.file("changelogs/latest.md")
            .takeIf { it.exists() }
            ?.readText()
            ?: "No changelog provided."
    )
    if (rootProject.file("README.md").exists()) {
        syncBodyFrom.set(rootProject.file("README.md").readText())
    }

    debugMode = false
}
