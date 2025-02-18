package studio.lunabee.onesafe

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.PagingConfig
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.MainOSColorPalette
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.FormatStyle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

object AppConstants {
    object Ui {
        object Item {
            val ErrorColor: Color = MainOSColorPalette.Error30
            const val ThumbnailFilePixelSize: Int = 200
            val ThumbnailFileDpSize: Dp = 44.dp
            val ThumbnailFileCornerRadius: Dp = 8.dp
            val ProgressIndicatorSize: Dp = 24.dp
        }

        object FileViewer {
            val UnknownFileSize: Dp = 48.dp
        }

        object Animation {
            object AddButton {
                const val ScaleInDurationMs: Int = 200
                const val ScaleOutDurationMs: Int = 100
            }

            object Breadcrumb {
                const val ExpandDurationMs: Int = 300
                const val ExpandDelayMs: Int = 200
                const val ShrinkDurationMs: Int = 200
                const val ShrinkDelayMs: Int = 300
            }

            object BottomSheet {
                const val AppearanceDelay: Long = 300L
            }
        }

        object HomeFavorite {
            const val MaxShowAmount: Int = 7
            const val ItemPerRow: Float = 3.25f
        }

        object HomeConversation {
            const val MaxShowAmount: Int = 4
        }

        object HomeDeleted {
            const val MaxShowAmount: Int = 7
        }

        object HomeEmpty {
            const val MaxWidthRatioImageEmptyScreen: Float = 0.7f
        }

        object AppPresentation {
            const val MinHeightRatioImageEmptyScreen: Float = 0.2f
            const val ResetFBASizeDurationMs: Int = 300

            enum class LogoTextSize(val width: Dp, val height: Dp) {
                Small(81.dp, 23.dp),
                Large(176.dp, 50.dp),
            }
        }

        object DelayedLoading {
            val MinDuration: Duration = (UiConstants.Shimmer.AnimDurationMs / 2).milliseconds
            val DelayBeforeShow: Duration = 100.milliseconds
            val DelayBeforeFetchingMetadata: Duration = 1000.milliseconds
            val DelayMinimumExport: Duration = 500.milliseconds
        }

        object TimeRelatedFieldFormatter {
            val DateFormatter: DateTimeFormatter = DateTimeFormatter
                .ofLocalizedDate(FormatStyle.SHORT)
                .withZone(ZoneOffset.UTC)

            val TimeFormatter: DateTimeFormatter = DateTimeFormatter
                .ofLocalizedTime(FormatStyle.SHORT)
                .withZone(ZoneOffset.UTC)

            val YearMonthDateFormatter: DateTimeFormatter = DateTimeFormatter
                .ofPattern("MM/yy")
                .withZone(ZoneOffset.UTC)

            val DateAndTimeFormatter: DateTimeFormatter = DateTimeFormatter
                .ofLocalizedDateTime(FormatStyle.SHORT)
                .withZone(ZoneOffset.UTC)

            // This actually works on API 24, https://issuetracker.google.com/issues/327670482 ?
            @SuppressLint("NewApi")
            val UnzonedLocalDateTimeParser: DateTimeFormatter = DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .optionalStart()
                .appendZoneOrOffsetId()
                .optionalEnd()
                .toFormatter()
        }

        object Bin {
            const val MaxWidthRatioImageEmptyScreen: Float = 0.4f
            const val MinHeightRatioImageEmptyScreen: Float = 0.2f
        }

        const val ItemNameTruncateLength: Int = 20
    }

    object Pagination {
        private const val DefaultInitialPageMultiplier: Int = 3
        private const val DefaultMaxSizeMultiplier: Int = 5
        private const val DefaultJumpMultiplier: Int = 3

        private const val DefaultPageItemSize: Int = 36
        private const val DefaultPageOsItemPrefetch: Int = DefaultPageItemSize
        private const val DefaultPageOsItemInitialLoad: Int = DefaultPageItemSize * DefaultInitialPageMultiplier
        const val DefaultPageLoadingSize: Int = DefaultPageOsItemInitialLoad
        private const val DefaultPageOsMaxSize: Int = DefaultPageItemSize * DefaultMaxSizeMultiplier
        private const val DefaultPageOsJumpThreshold: Int = DefaultPageItemSize * DefaultJumpMultiplier

        val DefaultPagingConfig: PagingConfig
            get() = PagingConfig(
                pageSize = DefaultPageItemSize,
                prefetchDistance = DefaultPageOsItemPrefetch,
                initialLoadSize = DefaultPageOsItemInitialLoad,
                enablePlaceholders = true,
                maxSize = DefaultPageOsMaxSize,
                jumpThreshold = DefaultPageOsJumpThreshold,
            )

        private const val RowPageItemSize: Int = 15
        private const val RowPageOsItemPrefetch: Int = 10
        private const val RowPageOsItemInitialLoad: Int = RowPageItemSize
        const val RowPageLoadingSize: Int = RowPageOsItemInitialLoad
        private const val RowPageOsMaxSize: Int = RowPageItemSize * DefaultMaxSizeMultiplier
        private const val RowPageOsJumpThreshold: Int = RowPageItemSize * DefaultJumpMultiplier

        val RowPagingConfig: PagingConfig
            get() = PagingConfig(
                pageSize = RowPageItemSize,
                prefetchDistance = RowPageOsItemPrefetch,
                initialLoadSize = RowPageOsItemInitialLoad,
                enablePlaceholders = true,
                maxSize = RowPageOsMaxSize,
                jumpThreshold = RowPageOsJumpThreshold,
            )
    }

    object Reflection {
        const val tabRowKt: String = "androidx.compose.material3.TabRowKt"
        const val scrollableTabRowMinimumTabWidth: String = "ScrollableTabRowMinimumTabWidth"
    }

    object FileProvider {
        const val ImageCacheFolderName: String = "itemImages"
        const val ArchiveExtractedDirectoryName: String = "archiveExtracted"
        const val ArchiveExportedDirectoryName: String = "archiveExported"
        const val ArchiveAutoBackupDirectoryName: String = "archiveAutoBackup"
        const val ArchiveMessageDirectoryName: String = "archiveMessage"
        const val ArchiveMigratedDirectoryName: String = "archiveMigrated"
        const val FileProviderAuthoritySuffix: String = "cameraiconprovider"
        const val LauncherImageFilter: String = "image/*"
        const val LauncherAllFileFilter: String = "*/*"
        const val SchemeProvider: String = "content"
        const val MimeTypeZip: String = "application/zip"
        const val PdfExtension: String = "pdf"
        const val ImageMimeTypePrefix: String = "image"
        const val AudioMimeTypePrefix: String = "audio"
        const val VideoMimeTypePrefix: String = "video"

        fun getAuthority(packageName: String): String = "$packageName.$FileProviderAuthoritySuffix"
    }

    object DefaultValue {
        const val NoSafeItemKindId: String = "No_Id"
    }

    object Migration {
        const val NewOneSafeMigrationIntentAction: String = "${BuildConfig.APPLICATION_ID}.MIGRATION"
        const val OldOneSafePackage: String = BuildConfig.ONESAFE_5_PACKAGE
        const val OldOneSafeSignature: String = "GTUZcxSK6FowiapcZlZjh3AFozBrlodWRnKTy4WuU6k="
        const val OldOneSafeService: String = "$OldOneSafePackage.MIGRATION_SERVICE"
        const val OldOneSafeServicePermission: String = "$OldOneSafePackage.permission.MIGRATION"
        const val MsgPublicKeyWhat: Int = 1
        const val MsgEncPasswordWhat: Int = 2
    }

    object UrlConstant {
        private const val ForceUpgradeBaseUrl: String = "https://app-maintenance.lunabee.studio/oneSafe"
        private val ForceUpgradeFlavorFolder: String = if (BuildConfig.IS_DEV) "/dev" else "/prod"
        val ForceUpgradeUrl: String = "$ForceUpgradeBaseUrl$ForceUpgradeFlavorFolder"
    }

    object FontFeature {
        const val mono: String = "tnum"
    }
}
