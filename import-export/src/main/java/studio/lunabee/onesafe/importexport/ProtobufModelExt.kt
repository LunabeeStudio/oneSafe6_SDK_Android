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

package studio.lunabee.onesafe.importexport

import com.google.protobuf.ByteString
import com.google.protobuf.kotlin.toByteString
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.proto.OSExportProto.ArchiveMetadata.ArchiveKind

fun ByteString?.toByteArrayOrNull(): ByteArray? {
    return this?.takeUnless { it.isEmpty }?.toByteArray()
}

fun String.nullIfEmpty(): String? {
    return takeIf { it.isNotEmpty() }
}

fun ByteArray?.byteStringOrEmpty(): ByteString {
    return (this ?: byteArrayOf()).toByteString()
}

fun OSArchiveKind.toProtoArchiveKind(): ArchiveKind =
    when (this) {
        OSArchiveKind.Backup -> ArchiveKind.BACKUP
        OSArchiveKind.Sharing -> ArchiveKind.SHARING
        OSArchiveKind.Unknown -> ArchiveKind.UNSPECIFIED
    }

fun ArchiveKind.toOSArchiveKind(): OSArchiveKind = when (this) {
    ArchiveKind.UNSPECIFIED,
    ArchiveKind.BACKUP,
    -> OSArchiveKind.Backup
    ArchiveKind.SHARING -> OSArchiveKind.Sharing
    ArchiveKind.UNRECOGNIZED -> OSArchiveKind.Unknown
}
