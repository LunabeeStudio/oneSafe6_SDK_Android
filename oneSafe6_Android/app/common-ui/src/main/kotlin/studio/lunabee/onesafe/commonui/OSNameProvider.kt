package studio.lunabee.onesafe.commonui

import com.lunabee.lbextensions.remove
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.extension.startEmojiOrNull

private val logger = LBLogger.get<OSNameProvider>()

interface OSNameProvider {
    val name: LbcTextSpec
    val truncatedName: LbcTextSpec
    val placeholderName: LbcTextSpec

    companion object {
        fun fromName(name: String?, hasIcon: Boolean): OSNameProvider = try {
            if (!hasIcon && name?.startEmojiOrNull() != null) {
                EmojiNameProvider(rawName = name)
            } else {
                DefaultNameProvider(name)
            }
        } catch (e: IllegalStateException) {
            // Can occur if EmojiCompact is not initialized
            logger.e(e)
            DefaultNameProvider(name)
        }
    }
}

object RemovedNameProvider : OSNameProvider {
    override val name: LbcTextSpec
        get() = LbcTextSpec.StringResource(OSString.common_itemDeleted)
    override val truncatedName: LbcTextSpec
        get() = LbcTextSpec.StringResource(OSString.common_itemDeleted)
    override val placeholderName: LbcTextSpec
        get() = LbcTextSpec.StringResource(id = OSString.common_corruptedPlaceholder)
}

object ErrorNameProvider : OSNameProvider {
    override val name: LbcTextSpec
        get() = LbcTextSpec.StringResource(OSString.common_corrupted)
    override val truncatedName: LbcTextSpec
        get() = LbcTextSpec.StringResource(OSString.common_corrupted)
    override val placeholderName: LbcTextSpec
        get() = LbcTextSpec.StringResource(id = OSString.common_corruptedPlaceholder)
}

open class DefaultNameProvider(private val rawName: String?) : OSNameProvider {

    override val name: LbcTextSpec = rawName?.let(LbcTextSpec::Raw) ?: LbcTextSpec.StringResource(id = OSString.common_noName)

    override val truncatedName: LbcTextSpec = when {
        rawName.isNullOrEmpty() -> LbcTextSpec.StringResource(id = OSString.common_noName)
        rawName.length <= ItemNameTruncateLength -> LbcTextSpec.Raw(rawName)
        else -> LbcTextSpec.Raw("${rawName.take(ItemNameTruncateLength)}…")
    }

    override val placeholderName: LbcTextSpec = if (rawName.isNullOrEmpty()) {
        LbcTextSpec.StringResource(id = OSString.common_noNamePlaceholder)
    } else {
        val split = rawName.split("\\s+".toRegex(), 2).filter { it.isNotEmpty() && it.startEmojiOrNull() == null }
        when {
            split.isEmpty() -> LbcTextSpec.StringResource(id = OSString.common_noNamePlaceholder)
            else -> LbcTextSpec.Raw(split.joinToString("") { it.take(1).uppercase() })
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DefaultNameProvider

        if (rawName != other.rawName) return false

        return true
    }

    override fun hashCode(): Int {
        return rawName?.hashCode() ?: 0
    }
}

class EmojiNameProvider(private val rawName: String?) : DefaultNameProvider(rawName) {
    override val name: LbcTextSpec
        get() = rawName?.startEmojiOrNull()
            ?.let { emoji -> LbcTextSpec.Raw(rawName.remove(emoji).trim()) }
            ?: super.name

    override val placeholderName: LbcTextSpec
        get() = rawName?.startEmojiOrNull()
            ?.let { emoji -> LbcTextSpec.Raw(emoji) }
            ?: super.placeholderName
}

private const val ItemNameTruncateLength: Int = 20
