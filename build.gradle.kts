plugins {
    id("java")
}

group = "io.wesner.robert.cb1060.clamvote"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.robert.wesner.io/repository/johnymuffin-maven-public/")
}

dependencies {
    implementation("com.legacyminecraft.poseidon:poseidon-craftbukkit:1.+")

    compileOnly("org.projectlombok:lombok:1.18.48")
    annotationProcessor("org.projectlombok:lombok:1.18.48")
    compileOnly("org.jspecify:jspecify:1.0.0")
}

sourceSets {
    create("compat") {
        java.srcDir("src/compat/java")
    }

    main {
        compileClasspath += sourceSets["compat"].output
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(project.properties)
    }
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(tasks.named("compileCompatJava"))
}
