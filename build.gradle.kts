plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij") version "1.17.4"
}

group = "io.github.melomei"
version = "1.3.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

intellij {
    version.set("2024.1")
    type.set("IC")
    updateSinceUntilBuild.set(false)
    sameSinceUntilBuild.set(false)
    plugins.set(listOf())
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
        options.encoding = "UTF-8"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    processResources {
        filteringCharset = "UTF-8"
    }
    patchPluginXml {
        sinceBuild.set("231")
    }
    test {
        useJUnitPlatform()
    }
    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }
    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
