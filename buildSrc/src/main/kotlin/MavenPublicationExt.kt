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

import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering

private val Project.android: LibraryExtension
    get() = (this as ExtensionAware).extensions.getByName("android") as LibraryExtension

/**
 * Set additional artifacts to upload
 * - sources
 * - javadoc
 * - aar
 *
 * @param project project current project
 */
fun MavenPublication.setAndroidArtifacts(
    project: Project,
    flavorAarSuffix: String? = null,
) {
    val sourceJar by project.tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(project.android.sourceSets.getByName("main").java.srcDirs)
    }

    val javadocJar by project.tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from(project.android.sourceSets.getByName("main").java.srcDirs)
    }

    artifact(sourceJar)
    artifact(javadocJar)
    val aarBasePath = project.layout.buildDirectory.dir("outputs/aar").get().asFile.path
    val filename = "${project.name.lowercase()}${flavorAarSuffix?.let { "-$it" }.orEmpty()}-release.aar"
    artifact("$aarBasePath/$filename")
}

/**
 * Set additional artifacts to upload
 * - sources
 * - javadoc
 * - jar
 *
 * @param project project current project
 */
fun MavenPublication.setJavaArtifacts(project: Project) {
    artifact(project.layout.buildDirectory.dir("libs/${project.name}-${project.version}.jar").get().asFile)
    artifact(project.tasks.named("sourcesJar"))
    artifact(project.tasks.named("javadocJar"))
}
