import java.util.Properties

plugins {
    id("java-library")
    id("eclipse")
    id("idea")
    id("maven-publish")
    id("net.neoforged.gradle.userdev") version "7.0.80"
}

val mod_version: String by project
val mod_group_id: String by project
val mod_id: String by project

project.version = mod_version
project.group = mod_group_id

repositories {
    mavenLocal()
}

base {
    archivesName.set(mod_id)
}

// Mojang ships Java 17 to end users in 1.18+, so your mod should target Java 17.
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

//minecraft.accessTransformers.file rootProject.file("src/main/resources/META-INF/accesstransformer.cfg")
//minecraft.accessTransformers.entry public net.minecraft.client.Minecraft textureManager # textureManager

// Default run configurations.
// These can be tweaked, removed, or duplicated as needed.
runs {
    // applies to all the run configs below
    configureEach {
        // Recommended logging data for a userdev environment
        // The markers can be added/remove as needed separated by commas.
        // "SCAN": For mods scan.
        // "REGISTRIES": For firing of registry events.
        // "REGISTRYDUMP": For getting the contents of all registries.
        systemProperty("forge.logging.markers", "REGISTRIES")

        // Recommended logging level for the console
        // You can set various levels here.
        // Please read: https://stackoverflow.com/questions/2031163/when-to-use-the-different-log-levels
        systemProperty("forge.logging.console.level", "debug")

        modSource(project.sourceSets.getByName("main"))
    }

    create("client") {
        // Comma-separated list of namespaces to load gametests from. Empty = all namespaces.
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
    }

    create("server") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
        programArgument("--nogui")
    }

    // This run config launches GameTestServer and runs all registered gametests, then exits.
    // By default, the server will crash when no gametests are provided.
    // The gametest system is also enabled by default for other run configs under the /test command.
    create("gameTestServer") {
        systemProperty("forge.enabledGameTestNamespaces", mod_id)
    }

    create("data") {
        // example of overriding the workingDirectory set in configureEach above, uncomment if you want to use it
        // workingDirectory project.file("run-data")

        // Specify the modid for data generation, where to output the resulting resource, and where to look for existing resources.
        programArguments.addAll(listOf("--mod", mod_id, "--all", "--output", file("src/generated/resources/").absolutePath, "--existing", file("src/main/resources/").absolutePath))
    }
}

// Include resources generated by data generators.
sourceSets {
    main {
        resources.srcDir("src/generated/resources")
    }
}


dependencies {
    val neo_version: String by project
    // Specify the version of Minecraft to use.
    // Depending on the plugin applied there are several options. We will assume you applied the userdev plugin as shown above.
    // The group for userdev is net.neoforged, the module name is neoforge, and the version is the same as the neoforge version.
    // You can however also use the vanilla plugin (net.neoforged.gradle.vanilla) to use a version of Minecraft without the neoforge loader.
    // And its provides the option to then use net.minecraft as the group, and one of; client, server or joined as the module name, plus the game version as version.
    // For all intends and purposes: You can treat this dependency as if it is a normal library you would use.
    implementation("net.neoforged:neoforge:$neo_version")

    // Example mod dependency with JEI
    // The JEI API is declared for compile time use, while the full JEI artifact is used at runtime
    // val jei_vesion: String by project
    // compileOnly("mezz.jei:jei-${minecraft_version}-common-api:${jei_version}")
    // compileOnly("mezz.jei:jei-${minecraft_version}-forge-api:${jei_version}")
    // runtimeOnly("mezz.jei:jei-${minecraft_version}-forge:${jei_version}")

    // Example mod dependency using a mod jar from ./libs with a flat dir repository
    // This maps to ./libs/coolmod-${minecraft_version}-${coolmod_version}.jar
    // The group id is ignored when searching -- in this case, it is "blank"
    // val coolmod_version: String by project
    // implementation("blank:coolmod-${minecraft_version}:${coolmod_version}")

    // Example mod dependency using a file as dependency
    // implementation(files("libs/coolmod-${minecraft_version}-${coolmod_version}.jar"))

    // Example project dependency using a sister or child project:
    // implementation(project(":myproject"))

    // For more info:
    // http://www.gradle.org/docs/current/userguide/artifact_dependencies_tutorial.html
    // http://www.gradle.org/docs/current/userguide/dependency_management.html
}

// This block of code expands all declared replace properties in the specified resource targets.
// A missing property will result in an error. Properties are expanded using ${} Groovy notation.
// When "copyIdeResources" is enabled, this will also run before the game launches in IDE environments.
// See https://docs.gradle.org/current/dsl/org.gradle.language.jvm.tasks.ProcessResources.html
tasks.withType<ProcessResources>().configureEach {
    val loadedProperties = Properties().apply {
        load(project.rootProject.file("gradle.properties").inputStream())
    }.toMutableMap() as MutableMap<String, Any>

    inputs.properties(loadedProperties)

    filesMatching("META-INF/mods.toml") {
        expand(loadedProperties)
        expand(mutableMapOf("project" to project))
    }
}

// Example configuration to allow publishing using the maven-publish plugin
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven(url = "file://${project.projectDir}/repo")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation
}
