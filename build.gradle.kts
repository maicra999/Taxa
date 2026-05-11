plugins {
    java
    alias(libs.plugins.spotless)
}

/* Project Properties */
val projectGroup    = project.property("project_group")     as String
val projectId       = project.property("project_id")        as String
val projectVersion  = project.property("project_version")   as String
val projectName     = project.property("project_name")      as String

group = projectGroup
version = projectVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

spotless {
    java {
        palantirJavaFormat()
    }
}

repositories {
    mavenCentral()
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly(libs.geyser.api)
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        filteringCharset = "UTF-8"

        inputs.property("version", project.version)
        inputs.property("id", projectId)
        inputs.property("name", projectName)

        filesMatching("extension.yml") {
            expand(
                "version" to project.version,
                "id" to projectId,
                "name" to projectName
            )
        }
    }

    build {
        dependsOn(spotlessApply)
    }
}
