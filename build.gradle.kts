plugins {
	java
	alias(libs.plugins.fabric.loom)
	alias(libs.plugins.minotaur)
}

val mappingsAttribute = Attribute.of("net.minecraft.mappings", String::class.java)!!

class ModInfo {
	val id = property("mod.id").toString()
	val group = property("mod.group").toString()
	val version = property("mod.version").toString()
}

val mod = ModInfo()

version = "${mod.version}+${libs.versions.minecraft.get()}"
group = mod.group

base.archivesName = mod.id

loom {
	splitEnvironmentSourceSets()

	mods.create(mod.id) {
		sourceSet(sourceSets["main"])
		sourceSet(sourceSets["client"])
	}

	accessWidenerPath = file("src/main/resources/snapper.accesswidener")
}

repositories {
	mavenLocal() // TODO: Remove me after Greenhouse Config releases.
	mavenCentral()
	maven("https://maven.parchmentmc.org/")
	maven("https://maven.spiritstudios.dev/releases/")
	maven("https://moehreag.duckdns.org/maven/releases") {
		content {
			includeGroup("io.github.axolotlclient.AxolotlClient")
			includeGroup("io.github.axolotlclient.AxolotlClient-config")
		}
	}
	maven("https://maven.greenhouse.lgbt/releases/")
}

dependencies {
	minecraft(libs.minecraft)
	mappings(loom.layered {
		officialMojangMappings()
		parchment(libs.parchment)
	})
	modImplementation(libs.fabric.loader)

	modImplementation(libs.fabric.api)

	include(libs.bundles.specter)
	modImplementation(libs.bundles.specter)

	modCompileOnlyApi(libs.greenhouse.config.api) {
		attributes {
			attribute(mappingsAttribute, "mojmap") // Use Mojmap at runtime.
		}
	}
	modRuntimeOnly(libs.greenhouse.config)
	include(libs.greenhouse.config)

	implementation(libs.objc.bridge)
}

tasks.processResources {
	val map = mapOf(
		"mod_id" to mod.id,
		"mod_version" to mod.version,
		"fabric_loader_version" to libs.versions.fabric.loader.get(),
		"minecraft_version" to libs.versions.minecraft.get()
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

tasks.jar { from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } } }

modrinth {
	token.set(System.getenv("MODRINTH_TOKEN"))
	projectId.set(mod.id)
	versionNumber.set(mod.version)
	uploadFile.set(tasks.remapJar)
	gameVersions.addAll(libs.versions.minecraft.get(), "1.21.8")
	loaders.addAll("fabric", "quilt")
	syncBodyFrom.set(rootProject.file("README.md").readText())
	dependencies {
		required.version("fabric-api", libs.versions.fabric.api.get())
	}
}