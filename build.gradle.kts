plugins {
    java

    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.modpublish)
}

val modVersion = "1.1.1"
val modId = "snapper"
val modName = "Snapper"

val modrinthProject = "snapper"
val githubRepository = "SpiritGameStudios/Snapper"

group = "dev.spiritstudios"
base.archivesName = modId

version = "$modVersion+${libs.versions.minecraft.get()}"

@Suppress("UnstableApiUsage")
repositories {
    maven("https://maven.terraformersmc.com/") {
        name = "Terraformers"
        content { includeGroupAndSubgroups("com.terraformersmc") }
    }

    maven("https://moehreag.duckdns.org/maven/releases/") {
        name = "AxolotlClient Releases"
        content { includeGroupAndSubgroups("io.github.axolotlclient") }
    }

    maven("https://maven.greenhouse.lgbt/releases/") {
        name = "Greenhouse Releases"
        content { includeGroupAndSubgroups("lgbt.greenhouse") }
    }

    maven("https://maven.greenhouse.lgbt/snapshots/") {
        name = "Greenhouse Snapshots"
        content { includeGroupAndSubgroups("lgbt.greenhouse") }
    }

    mavenCentral()
}

val debugArgs = listOf(
    "-enableassertions",

    // Mixin debugging, should make failures happen quicker
    "-Dmixin.debug.verify=true",
    "-Dmixin.debug.strict=true",
    "-Dmixin.debug.countInjections=true",

    // Memory Usage Optimization
    "-XX:+UseZGC",
    "-XX:+UseCompactObjectHeaders",
    "-XX:+UseStringDeduplication",

    "-XX:+AlwaysPreTouch" // Apparently makes startup faster
)

loom {
    runtimeOnlyLog4j = true

    splitEnvironmentSourceSets()

    mods.create(modId) {
        sourceSet(sourceSets["main"])
        sourceSet(sourceSets["client"])
    }

    accessWidenerPath = file("src/main/resources/snapper.classtweaker")

    runs.configureEach { jvmArguments.addAll(debugArgs) }
}

dependencies {
    minecraft(libs.minecraft)

    implementation(libs.fabric.loader)
    implementation(libs.fabric.api)

    compileOnlyApi(libs.greenhouse.config.api)

    runtimeOnly(libs.greenhouse.config.fabric)
    include(libs.greenhouse.config.fabric)

    compileOnly(libs.modmenu)

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

    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks.jar {
    from("LICENSE") { rename { "${it}_$modId" } }
}

publishMods {
    file = tasks.jar.get().archiveFile
    modLoaders.add("fabric")

    version = modVersion
    type = STABLE
    displayName = "$modName $modVersion for Minecraft ${libs.versions.minecraft.get()}"

    changelog = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText

    modrinth {
        accessToken = providers.gradleProperty("secrets.modrinth_token")
        projectId = "MZQyESDC"
        minecraftVersions.add(libs.versions.minecraft.get())

        projectDescription = providers.fileContents(layout.projectDirectory.file("README.md")).asText

        requires("fabric-api")
        embeds("greenhouse-config")
    }

    github {
        accessToken = providers.gradleProperty("secrets.github_token")
        repository = githubRepository
        commitish = "main"

        tagName = modVersion
    }
}
