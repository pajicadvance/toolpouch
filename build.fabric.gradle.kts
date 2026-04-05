plugins {
	id("mod-platform")
	id("net.fabricmc.fabric-loom")
}

platform {
	loader = "fabric"
	dependencies {
		required("minecraft") {
			versionRange = ">=${prop("deps.minecraft")}"
		}
		required("fabric-api") {
			slug("fabric-api")
			versionRange = ">=${prop("deps.fabric-api")}"
		}
		required("fabricloader") {
			versionRange = ">=${libs.fabric.loader.get().version}"
		}
		required("fzzy_config") {
			slug("fzzy-config")
			versionRange = "*"
		}
		optional("immersiveoverlays") {
			slug("immersive-overlays")
			versionRange = "*"
		}
		optional("lambdynlights") {
			slug("lambdynamiclights")
			versionRange = "*"
		}
		optional("modmenu") {}
	}
}

loom {
	accessWidenerPath = rootProject.file("src/main/resources/aw/${stonecutter.current.version}.accesswidener")
	runs.named("client") {
		client()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "client"
		programArgs("--username=Dev")
		configName = "Fabric Client"
	}
	runs.named("server") {
		server()
		ideConfigGenerated(true)
		runDir = "run/"
		environment = "server"
		configName = "Fabric Server"
	}
}

repositories {
	mavenCentral()
	strictMaven("https://maven.fzzyhmstrs.me/", "me.fzzyhmstrs") { name = "Fzzy Config" }
	strictMaven("https://maven.terraformersmc.com/", "com.terraformersmc") { name = "TerraformersMC" }
	strictMaven("https://jitpack.io") { name = "Jitpack" }
	strictMaven("https://maven.gegy.dev") { name = "Gegy" }
	strictMaven("https://api.modrinth.com/maven", "maven.modrinth") { name = "Modrinth" }
}

dependencies {
	minecraft("com.mojang:minecraft:${prop("deps.minecraft")}")
	implementation(libs.fabric.loader)
	implementation(libs.moulberry.mixinconstraints)
	include(libs.moulberry.mixinconstraints)
	implementation("net.fabricmc.fabric-api:fabric-api:${prop("deps.fabric-api")}")
	localRuntime("com.terraformersmc:modmenu:${prop("deps.modmenu")}")
	implementation("me.fzzyhmstrs:fzzy_config:${prop("deps.fzzy_config")}")
	compileOnly("dev.lambdaurora.lambdynamiclights:lambdynamiclights-api:${prop("deps.ldl")}")
	compileOnly("com.github.pajicadvance:aileron:1.2.1")
	compileOnly("maven.modrinth:raised:${prop("deps.raised")}")
	compileOnly("maven.modrinth:shulkerboxtooltip:${prop("deps.sbt")}-fabric")
	compileOnly(rootProject.files("ext/spyglass_astronomy-1.0.20-mc26.1.1.jar"))
}
