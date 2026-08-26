import org.jreleaser.model.Active
import com.android.build.api.dsl.LibraryExtension

plugins {
    id("core-publish-convention")
    id("signing")
    id("org.jreleaser")
}

// Get project properties for JReleaser
val libraryName: String by project
val projectGithubUrl: String by project
val projectDescription: String by project
val developerName: String by project
val versionName: String by project

// Create a custom javadoc task for multiplatform
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    // Create an empty javadoc jar for now (common practice for KMP)
}

// Configure Android publishing if Android plugin is applied
plugins.withId("com.android.library") {
    extensions.configure<LibraryExtension> {
        publishing {
            singleVariant("release") {
                withSourcesJar()
                // Temporarily disable javadoc jar due to ASM compatibility issue
                // withJavadocJar()
            }
        }
    }
}

// Maven Central specific enhancements
afterEvaluate {
    publishing {
        publications.withType<MavenPublication> {
            // Add javadoc JAR to all publications (Maven Central requirement)
            artifact(javadocJar.get())

            // Override license for Maven Central compatibility
            pom {
                withXml {
                    // Clear existing licenses and set Maven Central compatible license
                    val licensesNode = asNode().get("licenses")
                    if (licensesNode is groovy.util.NodeList && licensesNode.isNotEmpty()) {
                        (licensesNode[0] as groovy.util.Node).children().clear()
                        val licenseNode = (licensesNode[0] as groovy.util.Node).appendNode("license")
                        licenseNode.appendNode("name", "The Apache License, Version 2.0")
                        licenseNode.appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        licenseNode.appendNode("distribution", "repo")
                    }
                }
            }
        }

        // Add staging repository for JReleaser
        repositories {
            maven {
                name = "staging"
                url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
            }
        }
    }
}

// JReleaser configuration
jreleaser {
    project {
        name = libraryName
        description = projectDescription
        longDescription = projectDescription
        website = projectGithubUrl
        authors = listOf(developerName)
        license = "Apache-2.0"
        inceptionYear = "2024"
        version = versionName
    }

    gitRootSearch = true

    signing {
        active = Active.ALWAYS
        armored = true
        verify = true
        publicKey = providers.environmentVariable("JRELEASER_GPG_PUBLIC_KEY").getOrElse("")
        secretKey = providers.environmentVariable("JRELEASER_GPG_SECRET_KEY").getOrElse("")
        passphrase = providers.environmentVariable("JRELEASER_GPG_PASSPHRASE").getOrElse("")
    }

    release {
        gitlab {
            skipTag = true
            skipRelease = true
        }
    }

    deploy {
        maven {
            mavenCentral.create("sonatype") {
                active = Active.ALWAYS
                url = "https://central.sonatype.com/api/v1/publisher"
                stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                username = providers.environmentVariable("JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME").getOrElse("")
                password = providers.environmentVariable("JRELEASER_MAVENCENTRAL_SONATYPE_TOKEN").getOrElse("")
                applyMavenCentralRules = false // Wait for fix: https://github.com/kordamp/pomchecker/issues/21
                sign = true
                checksums = true
                sourceJar = true
                javadocJar = true
                retryDelay = 60
                dryrun = false
            }
        }
    }
}

// Helper task to publish to staging before JReleaser
tasks.register("publishToStaging") {
    description = "Publishes all publications to the staging repository for JReleaser"
    group = "publishing"
    dependsOn("publishAllPublicationsToStagingRepository")
}

// Helper task to run the full Maven Central release process
tasks.register("releaseMavenCentral") {
    description = "Publishes to staging and runs JReleaser full release"
    group = "publishing"
    dependsOn("publishToStaging")
    finalizedBy("jreleaserFullRelease")
}