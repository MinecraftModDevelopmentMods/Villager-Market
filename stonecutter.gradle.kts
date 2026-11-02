plugins {
    id("dev.kikugie.stonecutter")
    // see https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml for new versions
    id("net.fabricmc.fabric-loom") version "1.18.1" apply false
    // see https://projects.neoforged.net/neoforged/moddevgradle for new versions
    id("net.neoforged.moddev") version "2.0.147" apply false
}

stonecutter active "26.3"

// Lets code check the loader with `//? if fabric` or `//? if neoforge`.
stonecutter parameters {
    constants.match(branch.id.ifEmpty { "common" }, "common", "fabric", "neoforge")

    // The source is written for 26.3; older versions get the old names back. Each replacement turns the
    // first name into the second at and after its version, and back again before it. The version thresholds
    // are only checked against 1.21.1 and 26.3: confirm them before adding a version in between.
    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
            // ResourceKey.location() was renamed with it. Other classes still have a location() on 26.3; CI's
            // switch round trip fails if shared code ever calls one.
            replace(".location()", ".identifier()")
        }
        string(current.parsed >= "26.1") {
            replace("net.minecraft.world.entity.npc.Villager;", "net.minecraft.world.entity.npc.villager.Villager;")
            // The EntityType constants moved to EntityTypes. The import keeps a marker so it can turn back.
            replace("import net.minecraft.world.entity.EntityType /* EntityTypes */;", "import net.minecraft.world.entity.EntityTypes;")
        }
        regex(current.parsed >= "26.1") {
            replace(
                """\bEntityType\.(?!(?:CODEC|STREAM_CODEC|NO_UPDATE_INTERVAL)\b)([A-Z][A-Z0-9_]*)\b""", "EntityTypes.\$1",
                """\bEntityTypes\.([A-Z][A-Z0-9_]*)\b""", "EntityType.\$1"
            )
        }
    }
}

// Decompiling Minecraft for several versions at once can run the machine out of memory, so MDG does one at a time.
interface MinecraftArtifactsMutex : BuildService<BuildServiceParameters.None>

val mutex = gradle.sharedServices.registerIfAbsent("createMinecraftArtifactsMutex", MinecraftArtifactsMutex::class.java) {
    maxParallelUsages.set(1)
}

// Version projects share names across branches (:26.3, :fabric:26.3) and Gradle tells projects apart by
// group and name, so each branch's versions get their own group before anything resolves them.
subprojects {
    if (parent != rootProject) group = "${rootProject.group}.${parent!!.name}"
    else if (name.first().isDigit()) group = "${rootProject.group}.common"

    tasks.matching { it.name == "createMinecraftArtifacts" }.configureEach {
        usesService(mutex)
    }

    // The active version's merge writes this branch's shared sources. Other versions read those folders
    // while generating, so they have to wait for that write. Their own merge is not a dependency: running
    // it would copy the inactive version back over the shared sources.
    if (parent != null) {
        val activeNode = parent!!.project("26.3")
        if (path != activeNode.path) {
            val activeMerge = activeNode.tasks.named("stonecutterMerge")
            val activeTestMerge = activeNode.tasks.named("stonecutterMergeTest")
            tasks.matching { it.name == "stonecutterPrepare" || it.name == "stonecutterGenerate" }.configureEach {
                dependsOn(activeMerge)
            }
            tasks.matching { it.name == "stonecutterPrepareTest" || it.name == "stonecutterGenerateTest" }.configureEach {
                dependsOn(activeTestMerge)
            }
        }
    }
}

// Builds every loader jar and copies them to build/libs/<mod version>/ (the common jars aren't mods).
tasks.register<Copy>("collectJars") {
    group = "build"
    val jars = stonecutter.tasks.named<Jar>("jar") { branch.id.isNotEmpty() }
    dependsOn(jars.map { it.values })
    from(jars.map { it.values.map { jar -> jar.flatMap(Jar::getArchiveFile) } })
    into(layout.buildDirectory.dir("libs/${project.version}"))
}
