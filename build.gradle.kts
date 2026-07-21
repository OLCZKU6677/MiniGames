plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.panda-lang.org/releases")
    maven("https://repo.okaeri.cloud/releases")
    maven("https://repo.nexomc.com/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation("dev.rollczi:litecommands-bukkit:3.11.0")
    implementation("me.devnatan:inventory-framework-platform-paper:3.7.1")
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.13")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.13")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.encoding = "UTF-8"
}

tasks {
    runServer {
        minecraftVersion("1.21.11")
        jvmArgs("-Xms2G", "-Xmx2G")
    }
    processResources {
        val props = mapOf("version" to version)
        filesMatching("plugin.yml") { expand(props) }
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("dev.rollczi.litecommands", "pl.olczku.miniGames.libs.litecommands")
        relocate("me.devnatan.inventoryframework", "pl.olczku.miniGames.libs.inventoryframework")
        relocate("com.tcoded.folialib", "pl.olczku.miniGames.libs.folialib")
        relocate("eu.okaeri.configs", "pl.olczku.miniGames.libs.okaericonfigs")
    }
    build { dependsOn(shadowJar) }
}
