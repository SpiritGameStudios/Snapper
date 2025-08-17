@file:Suppress("UnstableApiUsage", "PropertyName")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.deftu.gradle.utils.GameSide
import dev.deftu.gradle.utils.version.MinecraftVersions

plugins {
	java
	id("xyz.wagyourtail.jvmdowngrader") version "1.3.3"
	kotlin("jvm")
	id("dev.deftu.gradle.multiversion") // Applies preprocessing for multiple versions of Minecraft and/or multiple mod loaders.
	id("dev.deftu.gradle.tools.configure")
	id("dev.deftu.gradle.tools.repo")
	id("dev.deftu.gradle.tools.resources") // Applies resource processing so that we can replace tokens, such as our mod name/version, in our resources.
	id("dev.deftu.gradle.tools.bloom") // Applies the Bloom plugin, which allows us to replace tokens in our source files, such as being able to use `@MOD_VERSION` in our source files.
	id("dev.deftu.gradle.tools.shadow") // Applies the Shadow plugin, which allows us to shade our dependencies into our mod JAR. This is NOT recommended for Fabric mods, but we have an *additional* configuration for those!
	id("dev.deftu.gradle.tools.minecraft.loom") // Applies the Loom plugin, which automagically configures Essential's Architectury Loom plugin for you.
	id("dev.deftu.gradle.tools.minecraft.releases") // Applies the Minecraft auto-releasing plugin, which allows you to automatically release your mod to CurseForge and Modrinth.
}

toolkitLoomHelper {
	useOneConfig {
		version = "1.0.0-alpha.134"
		loaderVersion = "1.1.0-alpha.48"

		usePolyMixin = true
		polyMixinVersion = "0.8.4+build.6"

		applyLoaderTweaker = true

		for (module in arrayOf("hud", "commands", "config", "config-impl", "events", "internal", "ui", "utils")) {
			+module
		}
	}

	useDevAuth("1.2.1")
	useMixinExtras("0.4.1")

	// Turns off the server-side run configs, as we're building a client-sided mod.
	disableRunConfigs(GameSide.SERVER)

	// Defines the name of the Mixin refmap, which is used to map the Mixin classes to the obfuscated Minecraft classes.
	if (!mcData.isNeoForge) {
		useMixinRefMap(modData.id)
	}

	if (mcData.isForge) {
		// Configures the Mixin tweaker if we are building for Forge.
		useForgeMixin(modData.id)
	}
}

val mod_id = property("mod.id").toString()

loom {
	if (mcData.version >= MinecraftVersions.VERSION_1_19) {
		splitEnvironmentSourceSets()

		mods.create(mod_id) {
			sourceSet(sourceSets["main"])
			sourceSet(sourceSets["client"])
		}
	}

	accessWidenerPath = rootProject.file("src/main/resources/snapper.accesswidener")
}

if (mcData.version < MinecraftVersions.VERSION_1_19) {
	sourceSets {
		main {
			java {
				srcDirs(rootProject.file("src/client/java"), rootProject.file("src/main/java"))
			}
			resources {
				srcDirs(rootProject.file("src/client/resources"), rootProject.file("src/main/resources"))
			}
		}
	}
}

repositories {
	maven("https://maven.spiritstudios.dev/releases/")
	maven("https://moehreag.duckdns.org/maven/releases") {
		content {
			includeGroup("io.github.axolotlclient.AxolotlClient")
			includeGroup("io.github.axolotlclient.AxolotlClient-config")
		}
	}
}

dependencies {
	fun Dependency?.applyExclusions() {
		check(this != null && this is ModuleDependency)
		exclude(module = "fabric-loader")
	}

	/**
	 * A pair of mappings used for the given environment.
	 *
	 * The first value is the mappings string, and the second value is whether these should be forced despite the requested configuration.
	 */
	val defaultMappings: Pair<String, Boolean> = when {
		mcData.isLegacyFabric -> "net.legacyfabric:yarn:${mcData.dependencies.legacyFabric.legacyYarnVersion}" to true
		mcData.isFabric -> "net.fabricmc:yarn:${mcData.dependencies.fabric.yarnVersion}:v2" to false
		mcData.isForge && mcData.version <= MinecraftVersions.VERSION_1_15_2 -> mcData.dependencies.forge.mcpDependency to true
		else -> "official" to false
	}
	val mappingsNotation = defaultMappings.first

	mappings(when(mappingsNotation) {
		"official", "mojang", "mojmap" -> loom.officialMojangMappings()

		"official-like" -> {
			if (mcData.version <= MinecraftVersions.VERSION_1_12_2) {
				if (mcData.isForge) {
					mcData.dependencies.forge.mcpDependency
				} else {
					repositories {
						maven("https://raw.githubusercontent.com/BleachDev/cursed-mappings/main/")
					}

					"net.legacyfabric:yarn:${mcData.version}+build.mcp"
				}
			} else loom.officialMojangMappings()
		}

		else -> mappingsNotation
	}).applyExclusions()

	//TODO remove the above once https://github.com/Deftu/Gradle-Toolkit/pull/24 is merged


	// Add (Legacy) Fabric API as dependencies (these are both optional but are particularly useful).
	if (mcData.isFabric) {
		if (mcData.isLegacyFabric) {
			// 1.8.9 - 1.13
			modImplementation("net.legacyfabric.legacy-fabric-api:legacy-fabric-api:${mcData.dependencies.legacyFabric.legacyFabricApiVersion}")
		} else {
			// 1.16.5+
			modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
		}
	}
	include(libs.bundles.specter)
	modImplementation(libs.bundles.specter)

	implementation(libs.objc.bridge)
}

tasks {
	fatJar {
		from(sourceSets.getByName("main").output, sourceSets.getByName("client").output)
	}
	downgradeJar {
		inputFile = this@tasks.named<ShadowJar>("fatJar").get().archiveFile
		archiveClassifier = "downgraded-8-shaded"
	}

	remapJar {
		inputFile.set(shadeDowngradedApi.get().archiveFile)
		dependsOn(shadeDowngradedApi)
	}
}

java {
	withSourcesJar()
}

tasks.jar { from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } } }
/*
modrinth {
	token.set(System.getenv("MODRINTH_TOKEN"))
	projectId.set(mod.id)
	versionNumber.set(mod.version)
	uploadFile.set(tasks.remapJar)
	gameVersions.addAll(libs.versions.minecraft.get(), "1.21.1")
	loaders.addAll("fabric", "quilt")
	syncBodyFrom.set(rootProject.file("README.md").readText())
	dependencies {
		required.version("fabric-api", libs.versions.fabric.api.get())
	}
}

 */