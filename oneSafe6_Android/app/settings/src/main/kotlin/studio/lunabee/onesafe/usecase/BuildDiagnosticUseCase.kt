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
 * Created by Lunabee Studio / Date - 11/18/2025 - for the oneSafe6 SDK.
 * Last modified 11/18/25, 7:49 PM
 */

package studio.lunabee.onesafe.usecase

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemKey
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.InternalDir
import studio.lunabee.onesafe.domain.qualifier.VersionName
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.utils.CrossSafeData
import studio.lunabee.onesafe.error.OSError
import java.io.File
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

private val logger = LBLogger.get<BuildDiagnosticUseCase>()

data class DiagnosticResultData(
    val file: File,
    val date: Instant,
)

private const val DiagnosticPrefix = "oneSafe6_diagnostic"

class BuildDiagnosticUseCase @Inject constructor(
    private val itemRepository: SafeItemRepository,
    private val fieldRepository: SafeItemFieldRepository,
    private val keyRepository: SafeItemKeyRepository,
    private val safeRepository: SafeRepository,
    private val fileRepository: FileRepository,
    private val iconRepository: IconRepository,
    private val cryptoRepository: MainCryptoRepository,
    @param:InternalDir(InternalDir.Type.Logs) private val logDir: File,
    @param:ApplicationContext private val context: Context,
    private val clock: Clock,
    @param:VersionName private val versionName: String,
    @param:BuildNumber private val versionCode: Int,
) {
    suspend operator fun invoke(extraHeader: String? = null): LBResult<DiagnosticResultData> = OSError.runCatching {
        logDir.mkdirs()
        logDir.listFiles { it.name.startsWith(DiagnosticPrefix) }?.forEach { it.delete() } // clear previous diagnostics (txt & zip)

        val diagnostics = buildString {
            extraHeader?.let {
                appendLine("=== EXTRA INFO ===")
                appendLine(it)
            }
            systemInfo(context)
            itemDebugInfo()
            safeInfo()
        }
        val now = clock.now()
        val file = File(logDir, "${DiagnosticPrefix}_$now.txt")
        file.writeText(diagnostics)
        DiagnosticResultData(
            file = file,
            date = now,
        )
    }

    private fun StringBuilder.systemInfo(context: Context) {
        fun bytesToMB(bytes: Long) = bytes / (1024 * 1024)

        // Internal storage
        val internalDir = context.filesDir
        val internalStat = StatFs(internalDir.path)
        val internalAvailable = internalStat.blockSizeLong * internalStat.availableBlocksLong

        // External app-specific storage
        val extDir = context.getExternalFilesDir(null)
        val extAvailable = if (extDir != null) {
            val extStat = StatFs(extDir.path)
            extStat.blockSizeLong * extStat.availableBlocksLong
        } else {
            null
        }

        // RAM
        val memInfo = ActivityManager.MemoryInfo().apply {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            am.getMemoryInfo(this)
        }

        // Display info
        val dm = context.resources.displayMetrics

        appendLine("=== DEVICE INFO ===")
        appendLine("Brand: ${Build.BRAND}")
        appendLine("Device: ${Build.DEVICE}")
        appendLine("Model: ${Build.MODEL}")
        appendLine("Manufacturer: ${Build.MANUFACTURER}")

        appendLine("\n=== OS ===")
        appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        appendLine("Security Patch: ${Build.VERSION.SECURITY_PATCH}")
        appendLine("Kernel: ${System.getProperty("os.version")}")

        appendLine("\n=== APP ===")
        appendLine("Package: ${context.packageName}")
        appendLine("Version: $versionName ($versionCode)")

        appendLine("\n=== STORAGE ===")
        appendLine("Internal available: ${bytesToMB(internalAvailable)} MB")
        if (extAvailable != null) {
            appendLine("External (app dir) available: ${bytesToMB(extAvailable)} MB")
        }

        appendLine("\n=== RAM ===")
        appendLine("Total RAM: ${bytesToMB(memInfo.totalMem)} MB")
        appendLine("Avail RAM: ${bytesToMB(memInfo.availMem)} MB")
        appendLine("Low RAM device: ${memInfo.lowMemory}")

        appendLine("\n=== DISPLAY ===")
        appendLine("Resolution: ${dm.widthPixels} x ${dm.heightPixels}")
        appendLine("Density: ${dm.densityDpi} dpi")

        appendLine("\n=== LOCALE ===")
        appendLine("Locale: ${Locale.getDefault()}")

        appendLine("\n=== TIMESTAMP ===")
        appendLine("Generated: ${java.util.Date()}")
    }

    private suspend fun StringBuilder.safeInfo() {
        val safeId = safeRepository.currentSafeId()
        val safeItems = itemRepository.getAllSafeItems(safeId)
        val safeItemFields = fieldRepository.getAllSafeItemFields(safeId)
        val safeItemKeys = keyRepository.getAllSafeItemKeys(safeId)
        val files = fileRepository.getFiles(safeId)

        appendItems(safeItems)
        appendFields(safeItemFields)
        appendKeys(safeItemKeys)
        appendFiles(files)
    }

    private fun StringBuilder.appendFiles(files: Set<File>) {
        appendLine("\nFile count: ${files.size}")
        files.forEach {
            appendLine("\t${it.name}")
        }
    }

    private fun StringBuilder.appendKeys(safeItemKeys: List<SafeItemKey>) {
        appendLine("\nKey count: ${safeItemKeys.size}")
        safeItemKeys.forEach {
            appendLine("\t${it.id}")
        }
    }

    private fun StringBuilder.appendFields(safeItemFields: List<SafeItemField>) {
        appendLine("\nField count: ${safeItemFields.size}")
        safeItemFields.forEach {
            appendLine("\t${it.id}")
        }
    }

    fun StringBuilder.appendItems(
        items: List<SafeItem>,
        sortBy: (SafeItem) -> Comparable<*> = { it.id },
    ) {
        appendLine("\n=== SAFE CONTENT ===")
        appendLine("Item count: ${items.size}")

        // Convert the sort function into a comparator
        val comparator = Comparator<SafeItem> { a, b ->
            @Suppress("UNCHECKED_CAST")
            (sortBy(a) as Comparable<Any>).compareTo(sortBy(b))
        }

        // Group children by parentId
        val childrenMap = items.groupBy { it.parentId }

        // Items with no parent = roots
        val roots = childrenMap[null].orEmpty().sortedWith(comparator)

        fun walk(item: SafeItem, prefix: String, isLast: Boolean) {
            val connector = when {
                prefix.isEmpty() -> ""
                isLast -> "└── "
                else -> "├── "
            }

            append("\t")
                .append(prefix)
                .append(connector)
                .append(item.id.toString()) // Customize output here if needed
                .append("\n")

            val children = childrenMap[item.id].orEmpty().sortedWith(comparator)

            val newPrefix = prefix + if (isLast) "    " else "│   "

            children.forEachIndexed { index, child ->
                walk(child, newPrefix, index == children.lastIndex)
            }
        }

        roots.forEachIndexed { index, root ->
            walk(root, "", index == roots.lastIndex)
        }
    }

    private suspend fun StringBuilder.itemDebugInfo() {
        appendLine("\n=== SAFE SANITY CHECK ===")
        safeRepository.currentSafeIdOrNull()?.let { safeId ->
            val filesFromFileTable = fileRepository.getFiles(safeId).mapTo(hashSetOf()) { file -> file.name }
            val iconsFromFileTable = iconRepository.getIcons(safeId).mapTo(hashSetOf()) { icon -> icon.name }
            val filesFromField = hashSetOf<String>()
            val iconsFromItem = hashSetOf<String>()

            @OptIn(CrossSafeData::class)
            val storageFiles = fileRepository.getAllFiles().mapTo(hashSetOf()) { file -> file.name }

            @OptIn(CrossSafeData::class)
            val storageIcons = iconRepository.getAllIcons().mapTo(hashSetOf()) { file -> file.name }

            val items = itemRepository.getAllSafeItems(safeId)
            items.forEach { item ->
                val key = keyRepository.getSafeItemKey(item.id)
                val fields = fieldRepository.getSafeItemFields(item.id)
                val kindEncEntries = fields.map { field ->
                    field.encKind?.let { DecryptEntry(it, SafeItemFieldKind::class) }
                }

                @Suppress("UNCHECKED_CAST")
                val kinds = cryptoRepository.decrypt(key, kindEncEntries) as List<SafeItemFieldKind>
                val valueEncEntries = fields
                    .zip(kinds)
                    .mapNotNull { (field, kind) ->
                        if (kind.isKindFile()) {
                            field.encValue?.let { DecryptEntry(it, String::class) }
                        } else {
                            null
                        }
                    }

                if (valueEncEntries.isNotEmpty()) {
                    @Suppress("UNCHECKED_CAST")
                    val values = cryptoRepository.decrypt(key, valueEncEntries) as List<String>
                    values.forEach { fileValue ->
                        val fileId = fileValue.substringBefore(Constant.FileTypeExtSeparator)
                        filesFromField += fileId
                    }
                }

                item.iconId?.let { iconId ->
                    iconsFromItem += iconId.toString()
                }
            }

            if (filesFromFileTable.containsAll(filesFromField)) {
                appendLine("Every files from fields are referenced in file table ✅")
            } else {
                val error = "${(filesFromField - filesFromFileTable).size} files are referenced in fields but not in file table ❌"
                appendLine(error)
                logger.e(error)
            }

            if (filesFromField.containsAll(filesFromFileTable)) {
                appendLine("Every files from file table are referenced in fields ✅")
            } else {
                val error = "${(filesFromFileTable - filesFromField).size} files are referenced in file table but not in fields ❌"
                appendLine(error)
                logger.e(error)
            }

            if (iconsFromFileTable.containsAll(iconsFromItem)) {
                appendLine("Every icons from item are referenced in file table ✅")
            } else {
                val error = "${(iconsFromItem - iconsFromFileTable).size} icons are referenced in item but not in file table ❌"
                appendLine(error)
                logger.e(error)
            }

            if (iconsFromItem.containsAll(iconsFromFileTable)) {
                appendLine("Every icons from file table are referenced in item ✅")
            } else {
                val error = "${(iconsFromFileTable - iconsFromItem).size} icons are referenced in file table but not in item ❌"
                appendLine(error)
                logger.e(error)
            }

            if (storageFiles.containsAll(filesFromField)) {
                appendLine("All files referenced by field are in storage ✅")
            } else {
                val missingFiles = filesFromField.count { it !in storageFiles }
                val error = "$missingFiles files are missing in storage ❌"
                appendLine(error)
                logger.e(error)
            }

            if (storageIcons.containsAll(iconsFromItem)) {
                appendLine("All icons referenced by items are in storage ✅")
            } else {
                val missingIcons = iconsFromItem.count { it !in storageIcons }
                val error = "$missingIcons icons are missing in storage ❌"
                appendLine(error)
                logger.e(error)
            }
        }
    }
}
