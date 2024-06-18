plugins {
    id("java")
    id("fabric-loom") version "1.6-SNAPSHOT"
    id("maven-publish")
    id("com.modrinth.minotaur") version "2.+"
}

version = "${property("mod_version")}+${property("minecraft_version")}"

repositories {
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
    }
    maven("https://maven.terraformersmc.com/releases/") {
        name = "TerraformersMC"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API
    fun addFabricModule(name: String) {
        val module = fabricApi.module(name, "${property("fabric_version")}")
        modImplementation(module)
    }
    addFabricModule("fabric-resource-loader-v0")
    addFabricModule("fabric-command-api-v2")
    addFabricModule("fabric-lifecycle-events-v1")
    addFabricModule("fabric-key-binding-api-v1")
    addFabricModule("fabric-screen-api-v1")

    // ModMenu
    modImplementation("com.terraformersmc:modmenu:${property("modmenu_version")}")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
            expand(mapOf("loader_version" to "${property("loader_version")}"))
            expand(mapOf("mc_version" to "${property("minecraft_version")}"))
            expand(mapOf("java_version" to "${property("java_version")}"))
        }
    }

    jar {
        from("LICENSE")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))

    projectId.set("${property("archives_base_name")}")
    versionName.set("[${property("minecraft_version")}] CyanLib ${property("mod_version")}")
    versionNumber.set("${property("version")}")
    versionType.set("release")

    uploadFile.set(tasks.jar)
    //additionalFiles.set(arrayOf(remapSourcesJar))

    //gameVersions.addAll(arrayOf("${property("minecraft_version")}"))
    loaders.add("fabric")

    dependencies {
        required.version("fabric-api", "${property("fabric_version")}")
        required.version("modmenu", "${property("modmenu_version")}")
    }

    //changelog = file("changelogs/latest.md").exists() ? file("changelogs/latest.md").getText() : "No changelog provided"
    //syncBodyFrom = rootProject.file("README.md").text

    debugMode = true
}
//tasks.modrinth.dependsOn(tasks.modrinthSyncBody)
