plugins {
    java

    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.modpublish)
}

val mappingsAttribute = Attribute.of("net.minecraft.mappings", String::class.java)!!

val modVersion = "1.1.1"
val modId = "snapper"
val modName = "Snapper"

val modrinthProject = "snapper"
val githubRepository = "SpiritGameStudios/Snapper"

group = "dev.spiritstudios"
base.archivesName = modId

version = "$modVersion+${libs.versions.minecraft.get()}"

repositories {
    maven {
        name = "Spirit Studios Releases"
        url = uri("https://maven.spiritstudios.dev/releases/")

        content {
            @Suppress("UnstableApiUsage")
            includeGroupAndSubgroups("dev.spiritstudios")
        }
    }

    maven {
        name = "ParchmentMC"
        url = uri("https://maven.parchmentmc.org")

        content {
            @Suppress("UnstableApiUsage")
            includeGroupAndSubgroups("org.parchmentmc")
        }
    }

    maven("https://moehreag.duckdns.org/maven/releases") {
        content {
            includeGroup("io.github.axolotlclient.AxolotlClient")
            includeGroup("io.github.axolotlclient.AxolotlClient-config")
        }
    }

    maven("https://maven.greenhouse.lgbt/releases/")
    maven("https://maven.greenhouse.lgbt/snapshots/")

    mavenCentral()
}

loom {
    splitEnvironmentSourceSets()

    mods.create(modId) {
        sourceSet(sourceSets["main"])
        sourceSet(sourceSets["client"])
    }

    accessWidenerPath = file("src/main/resources/snapper.classtweaker")
}

dependencies {
    minecraft(libs.minecraft)
    @Suppress("UnstableApiUsage")
    mappings(
        loom.layered {
            officialMojangMappings()
            parchment(libs.parchment)
        }
    )

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)

    modCompileOnlyApi(libs.greenhouse.config.api) {
        attributes { attribute(mappingsAttribute, "intermediary") }
    }

    modRuntimeOnly(libs.greenhouse.config)
    include(libs.greenhouse.config)

    implementation(libs.objc.bridge)
}

tasks.processResources {
    val map = mapOf(
        "version" to modVersion,
        "loader_version" to libs.versions.fabric.loader.get()
    )

    inputs.properties(map)

    filesMatching("fabric.mod.json") { expand(map) }
}

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}

tasks.jar {
    from("LICENSE") { rename { "${it}_$modId" } }
}

publishMods {
    file = tasks.remapJar.get().archiveFile
    modLoaders.add("fabric")

    version = modVersion
    type = STABLE
    displayName = "$modName $modVersion for Minecraft ${libs.versions.minecraft.get()}"

    modrinth {
        accessToken = providers.gradleProperty("secrets.modrinth_token")
        projectId = modrinthProject
        minecraftVersions.add(libs.versions.minecraft.get())

        projectDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText

        requires("fabric-api")
    }
}
