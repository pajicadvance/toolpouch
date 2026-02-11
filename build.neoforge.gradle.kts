plugins {
	id("mod-platform")
	id("net.neoforged.moddev")
	id("dev.kikugie.fletching-table") version "0.1.0-alpha.22"
	kotlin("jvm") version "2.2.10"
	id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

platform {
	loader = "neoforge"
	dependencies {
		required("minecraft") {
			forgeVersionRange = "[${prop("deps.minecraft")}]"
		}
		required("neoforge") {
			forgeVersionRange = "[1,)"
		}
		required("fzzy_config") {
			slug("fzzy-config")
			forgeVersionRange = "[0,)"
		}
		optional("immersiveoverlays") {
			slug("immersive-overlays")
			forgeVersionRange = "[0,)"
		}
		optional("lambdynlights") {
			slug("lambdynamiclights")
			forgeVersionRange = "[0,)"
		}
	}
}

fletchingTable {
	mixins.create("main") {
		mixin("default", "${prop("mod.id")}.mixins.json")
	}
}

neoForge {
	version = property("deps.neoforge") as String
	accessTransformers.from(rootProject.file("src/main/resources/aw/${stonecutter.current.version}.cfg"))
	validateAccessTransformers = true

	if (hasProperty("deps.parchment")) parchment {
		val (mc, ver) = (property("deps.parchment") as String).split(':')
		mappingsVersion = ver
		minecraftVersion = mc
	}

	runs {
		register("client") {
			client()
			gameDirectory = file("run/")
			ideName = "NeoForge Client (${stonecutter.active?.version})"
			programArgument("--username=Dev")
		}
		register("server") {
			server()
			gameDirectory = file("run/")
			ideName = "NeoForge Server (${stonecutter.active?.version})"
		}
	}

	mods {
		register(property("mod.id") as String) {
			sourceSet(sourceSets["main"])
		}
	}
}

repositories {
	maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
	maven("https://maven.fzzyhmstrs.me/") { name = "Fzzy Config" }
	maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "KotlinForForge" }
	maven("https://maven.gegy.dev") { name = "Gegy" }
	maven("https://jitpack.io") { name = "Jitpack" }
	exclusiveContent {
		forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
		filter { includeGroup("maven.modrinth") }
	}
}

dependencies {
	val mappingsAttribute = Attribute.of("net.minecraft.mappings", String::class.java)
	attributesSchema {
		attribute(mappingsAttribute)
	}

	implementation( "me.fzzyhmstrs:fzzy_config:${prop("deps.fzzy_config")}+neoforge")
	implementation("com.moulberry:mixinconstraints:${prop("deps.mixinconstraints")}")
	jarJar("com.moulberry:mixinconstraints:${prop("deps.mixinconstraints")}")
	//runtimeOnly("maven.modrinth:immersive-overlays:${prop("deps.io")}-neoforge")
	compileOnly("dev.lambdaurora.lambdynamiclights:lambdynamiclights-api:${prop("deps.ldl")}") {
		attributes {
			attribute(mappingsAttribute, "mojmap")
		}
	}
	runtimeOnly("dev.lambdaurora.lambdynamiclights:lambdynamiclights-runtime:${prop("deps.ldl")}") {
		attributes {
			attribute(mappingsAttribute, "mojmap")
		}
	}
	implementation("maven.modrinth:shulkerboxtooltip:${prop("deps.sbt")}-neoforge")
	compileOnly("maven.modrinth:aileron:${prop("deps.aileron")}")
	compileOnly("maven.modrinth:raised:${prop("deps.raised")}")
}

tasks.named("createMinecraftArtifacts") {
	dependsOn(tasks.named("stonecutterGenerate"))
}
