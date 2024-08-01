/*
 * Copyright (c) 2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

import java.net.URI
import java.util.Locale

plugins {
    `maven-publish`
}

val isAndroidLibrary = project.plugins.hasPlugin("android-library")
val isCryptoModule = project.name == "crypto-android"

if (!isAndroidLibrary) {
    project.extensions.configure<JavaPluginExtension>("java") {
        withJavadocJar()
        withSourcesJar()
    }
}

project.extensions.configure<PublishingExtension>("publishing") {
    setupMavenRepository()
    setupPublication()
}

tasks.named("publish${project.name.capitalized()}PublicationToMavenRepository") {
    when {
        isCryptoModule -> dependsOn(tasks.named("assembleJceRelease"))
        isAndroidLibrary -> dependsOn(tasks.named("assembleRelease"))
        else -> dependsOn(tasks.named("jar"))
    }
}

/**
 * Set repository destination depending version suffix
 * Credentials should be stored in your root gradle.properties, in a non source controlled file.
 */
fun PublishingExtension.setupMavenRepository() {
    repositories {
        maven {
            authentication {
                credentials.username = System.getenv(EnvConfig.ENV_ARTIFACTORY_USER)
                    ?: project.properties["artifactory_deployer_release_username"] as? String
                credentials.password = System.getenv(EnvConfig.ENV_ARTIFACTORY_API_KEY)
                    ?: project.properties["artifactory_deployer_release_api_key"] as? String
            }
            val repository = if (version.toString().endsWith("SNAPSHOT")) {
                "sdk-onesafe-snapshot-local"
            } else {
                "sdk-onesafe-local"
            }
            url = URI.create("https://artifactory.lunabee.studio/artifactory/$repository/")
        }
    }
}

/**
 * Entry point for setting publication detail.
 */
fun PublishingExtension.setupPublication() {
    publications { setPublication() }
}

fun PublicationContainer.setPublication() {
    this.create<MavenPublication>(project.name) {
        setProjectDetails()
        when {
            isCryptoModule -> setAndroidArtifacts(project, AndroidConfig.CRYPTO_BACKEND_FLAVOR_DEFAULT)
            isAndroidLibrary -> setAndroidArtifacts(project)
            else -> setJavaArtifacts(project)
        }

        setPom()
    }
}

/**
 * Set project details:
 * - groupId will be [ProjectConfig.GROUP_ID]
 * - artifactId will take the name of the current project
 * - version will be set in each submodule gradle file
 */
fun MavenPublication.setProjectDetails() {
    groupId = ProjectConfig.GROUP_ID
    artifactId = project.name
    version = project.version.toString()
}

/**
 * Set POM file details.
 */
fun MavenPublication.setPom() {
    pom {
        name.set(project.name.capitalized())
        description.set(project.description)
        url.set(ProjectConfig.LIBRARY_URL)

        scm {
            connection.set("git@github.com:LunabeeStudio/oneSafeRevival_Android.git")
            developerConnection.set("git@github.com:LunabeeStudio/oneSafeRevival_Android.git")
            url.set("https://github.com/LunabeeStudio/oneSafeRevival_Android")
        }

        developers {
            developer {
                id.set("Publisher")
                name.set("Publisher Lunabee")
                email.set("publisher@lunabee.com")
            }
        }

        withXml {
            asNode().appendNode("dependencies").apply {
                fun Dependency.write(scope: String) = appendNode("dependency").apply {
                    appendNode("groupId", group)
                    appendNode("artifactId", name)
                    version?.let { appendNode("version", version) }
                    appendNode("scope", scope)
                }

                configurations["api"].dependencies.forEach { dependency ->
                    dependency.write("implementation")
                }

                configurations["implementation"].dependencies.forEach { dependency ->
                    dependency.write("runtime")
                }

                if (isCryptoModule) {
                    configurations["jceApi"]?.dependencies?.forEach { dependency ->
                        dependency.write("implementation")
                    }
                    configurations["jceImplementation"]?.dependencies?.forEach { dependency ->
                        dependency.write("runtime")
                    }
                }
            }
        }
    }
}

private fun String.capitalized(): String = if (this.isEmpty()) this else this[0].titlecase(Locale.getDefault()) + this.substring(1)
