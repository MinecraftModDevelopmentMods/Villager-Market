pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        exclusiveContent {
            forRepository {
                maven("https://maven.fabricmc.net") { name = "Fabric" }
            }
            filter {
                includeGroupAndSubgroups("net.fabricmc")
            }
        }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    // see https://stonecutter.kikugie.dev for new versions
    id("dev.kikugie.stonecutter") version "0.9.8"
}

// The root src/ is the common code. Its versions live in versions/<mc>, and each loader branch in <loader>/versions/<mc>.
stonecutter {
    kotlinController = true
    centralScript = "build.gradle"
    create(rootProject) {
        versions("1.21.1", "26.3")
        // Each version node runs its branch's build.gradle (centralScript above).
        branch("fabric") { versions("26.3") }
        branch("neoforge") { versions("1.21.1", "26.3") }
        vcsVersion = "26.3"
    }
}

// This should match the folder name of the project, or else IDEA may complain (see https://youtrack.jetbrains.com/issue/IDEA-317606)
rootProject.name = "Villager-Market"
includeBuild("build-logic")
