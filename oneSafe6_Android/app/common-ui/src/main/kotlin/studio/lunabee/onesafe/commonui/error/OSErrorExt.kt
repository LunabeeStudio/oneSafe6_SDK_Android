package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.BuildConfig
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.BubblesCryptoError
import studio.lunabee.onesafe.error.BubblesDomainError
import studio.lunabee.onesafe.error.BubblesDoubleRatchetError
import studio.lunabee.onesafe.error.BubblesMessagingError
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSDriveError
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSImeError
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.error.OSMigrationError
import studio.lunabee.onesafe.error.OSRemoteError
import studio.lunabee.onesafe.error.OSRepositoryError
import studio.lunabee.onesafe.error.OSStorageError

fun Throwable?.title(fallback: LbcTextSpec = LbcTextSpec.StringResource(id = OSString.error_defaultTitle)): LbcTextSpec {
    return (this as? OSError)?.title() ?: fallback
}

fun Throwable?.description(fallback: LbcTextSpec = LbcTextSpec.StringResource(id = OSString.error_defaultMessage)): LbcTextSpec {
    return (this as? OSError)?.description()
        ?: this?.localizedMessage?.let(LbcTextSpec::Raw)
        ?: fallback
}

fun Throwable?.codeText(): LbcTextSpec {
    return (this as? OSError)?.code?.name?.let(LbcTextSpec::Raw)
        ?: this?.localizedMessage?.let(LbcTextSpec::Raw)
        ?: this?.javaClass?.simpleName?.let(LbcTextSpec::Raw)
        ?: LbcTextSpec.StringResource(id = OSString.common_error_unknown)
}

fun OSError?.title(): LbcTextSpec {
    return when (this) {
        is OSDomainError -> this.localizedTitle()
        is OSCryptoError -> this.localizedTitle()
        is OSStorageError -> this.localizedTitle()
        is OSAppError -> this.localizedTitle()
        is OSRemoteError -> this.localizedTitle()
        is OSImportExportError -> this.localizedTitle()
        is OSMigrationError -> this.localizedTitle()
        is OSDriveError -> this.localizedTitle()
        is OSRepositoryError -> this.localizedTitle()
        is OSImeError -> this.localizedTitle()
        is BubblesCryptoError -> this.localizedTitle()
        is BubblesDomainError -> this.localizedTitle()
        is BubblesDoubleRatchetError -> this.localizedTitle()
        is BubblesMessagingError -> this.localizedTitle()
        null -> null
    } ?: LbcTextSpec.StringResource(id = OSString.error_defaultTitle)
}

fun OSError?.description(): LbcTextSpec {
    return if (this != null) {
        val mainDescription = when (this) {
            is OSDomainError -> localizedDescription()
            is OSCryptoError -> localizedDescription()
            is OSStorageError -> localizedDescription()
            is OSAppError -> localizedDescription()
            is OSRemoteError -> localizedDescription()
            is OSImportExportError -> localizedDescription()
            is OSMigrationError -> localizedDescription()
            is OSDriveError -> localizedDescription()
            is OSRepositoryError -> localizedDescription()
            is OSImeError -> localizedDescription()
            is BubblesCryptoError -> localizedDescription()
            is BubblesDomainError -> localizedDescription()
            is BubblesDoubleRatchetError -> localizedDescription()
            is BubblesMessagingError -> localizedDescription()
        }
            ?: if (localizedMessage != null) {
                LbcTextSpec.Raw(
                    "%s\n\n$localizedMessage",
                    LbcTextSpec.StringResource(OSString.error_defaultMessage),
                )
            } else {
                LbcTextSpec.StringResource(OSString.error_defaultMessage)
            }

        if (BuildConfig.DEBUG) {
            val causeBuilder = StringBuilder()
            var cause: Throwable? = this.cause
            if (cause != null) {
                causeBuilder.appendLine()
            }
            while (cause != null) {
                causeBuilder.appendLine(
                    cause.localizedMessage,
                ) // directly call localizedMessage instead of description() to avoid recursion
                cause = cause.cause
            }

            LbcTextSpec.Raw("%s$causeBuilder", mainDescription)
        } else {
            mainDescription
        }
    } else {
        LbcTextSpec.StringResource(id = OSString.error_defaultMessage)
    }
}
