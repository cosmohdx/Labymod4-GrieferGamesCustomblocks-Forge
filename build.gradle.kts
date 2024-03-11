import net.labymod.gradle.core.addon.info.AddonMeta
import net.labymod.gradle.core.addon.info.dependency.AddonDependency

plugins {
    id("java-library")
    id("net.labymod.gradle")
    id("net.labymod.gradle.addon")
}

group = "net.griefergames"
version = "1.0.5"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

labyMod {
    defaultPackageName = "net.griefergames.customblocks" //change this to your main package name (used by all modules)
    addonInfo {
        namespace = "customblocks_forge"
        displayName = "GrieferGames CustomBlocks Forge"
        author = "GrieferGames CosmoHDx"
        description = "Provides CustomBlocks for GrieferGames via LabyMod Forge Loader"
        minecraftVersion = "1.8.9,1.12.2"
        version = System.getenv().getOrDefault("VERSION", project.version.toString())
        iconUrl = "textures/icon.png"
        addonDependencies = mutableListOf(
                AddonDependency("labyforge", false)
        )
        metas = mutableListOf(
                AddonMeta.RESTART_REQUIRED
        )
    }

    minecraft {
        registerVersions(
                "1.8.9",
                "1.12.2"
        ) { version, provider ->
            configureRun(provider, version)
        }

        subprojects.forEach {
            if (it.name != "game-runner") {
                filter(it.name)
            }
        }
    }

    addonDev {
        productionRelease()
    }
}

subprojects {
    plugins.apply("java-library")
    plugins.apply("net.labymod.gradle")
    plugins.apply("net.labymod.gradle.addon")

    repositories {
        maven("https://libraries.minecraft.net/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
}

fun configureRun(provider: net.labymod.gradle.core.minecraft.provider.VersionProvider, gameVersion: String) {
    provider.runConfiguration {
        mainClass = "net.minecraft.launchwrapper.Launch"
        jvmArgs("-Dnet.labymod.running-version=${gameVersion}")
        jvmArgs("-Dmixin.debug=true")
        jvmArgs("-Dnet.labymod.debugging.all=true")
        jvmArgs("-Dmixin.env.disableRefMap=true")

        args("--tweakClass", "net.labymod.core.loader.vanilla.launchwrapper.LabyModLaunchWrapperTweaker")
        args("--labymod-dev-environment", "true")
        args("--addon-dev-environment", "true")
    }

    provider.javaVersion = when (gameVersion) {
        else -> {
            JavaVersion.VERSION_17
        }
    }

    provider.mixin {
        val mixinMinVersion = when (gameVersion) {
            "1.8.9", "1.12.2", "1.16.5" -> {
                "0.6.6"
            }
            else -> {
                "0.8.2"
            }
        }

        minVersion = mixinMinVersion
    }
}
