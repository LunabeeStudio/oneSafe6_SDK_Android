/*
 * Copyright (c) 2025 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 2/7/2025 - for the oneSafe6 SDK.
 * Last modified 2/7/25, 12:14 PM
 */

import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskAction

abstract class CleanProtoTask : DefaultTask() {
    private val protoDir: Provider<Directory> = project.layout.buildDirectory.dir("generated/source/proto/")

    @TaskAction
    fun deleteProtoFiles() {
        protoDir.get().asFile.deleteRecursively()
    }
}
