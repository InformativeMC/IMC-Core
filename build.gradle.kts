import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("fabric-loom")
    kotlin("jvm").version(System.getProperty("kotlinVersion"))
    kotlin("plugin.serialization") version System.getProperty("kotlinVersion")
}

base {
    archivesName.set(project.property("archivesBaseName") as String)
}

version = project.property("modVersion") as String
group = project.property("mavenGroup") as String

repositories {
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraftVersion")}")
    mappings("net.fabricmc:yarn:${project.property("yarnMappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loaderVersion")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabricVersion")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabricKotlinVersion")}")

    implementation("io.javalin:javalin:4.3.0")

    // https://mvnrepository.com/artifact/org.bouncycastle/bcprov-jdk15on
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    // https://mvnrepository.com/artifact/org.bouncycastle/bcpkix-jdk15on
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")

    // define a BOM and its version
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3"))

    // define any required OkHttp artifacts without version
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")

    // Google guava
    implementation("com.google.guava:guava:31.1-jre")

    // SQLite
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
}

tasks {
    val javaVersion = JavaVersion.VERSION_17

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        if (JavaVersion.current().isJava9Compatible) {
            options.release.set(javaVersion.toString().toInt())
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            freeCompilerArgs = freeCompilerArgs + listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName}" }
        }
    }

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(mutableMapOf("version" to project.version))
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion.toString()))
        }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}
