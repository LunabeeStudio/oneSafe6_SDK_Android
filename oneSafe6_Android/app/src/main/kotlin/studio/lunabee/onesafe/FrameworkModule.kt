package studio.lunabee.onesafe

import android.content.ComponentName
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import androidx.work.WorkManager
import com.lunabee.lbcore.helper.LBLoadingVisibilityDelayDelegate
import com.lunabee.lbloading.DelayedLoadingManager
import com.lunabee.lbloading.LoadingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import studio.lunabee.onesafe.common.extensions.getCacheImageFolder
import studio.lunabee.onesafe.commonui.usecase.AndroidResizeIconUseCaseFactory
import studio.lunabee.onesafe.domain.LoadFileCancelAllUseCase
import studio.lunabee.onesafe.domain.common.DuplicatedNameTransform
import studio.lunabee.onesafe.domain.manager.IsAppBlockedUseCase
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.qualifier.ForceUpgradeUrl
import studio.lunabee.onesafe.domain.qualifier.InternalBackupMimetype
import studio.lunabee.onesafe.domain.qualifier.RemoteDir
import studio.lunabee.onesafe.domain.qualifier.StoreBetaTrack
import studio.lunabee.onesafe.domain.qualifier.VersionName
import studio.lunabee.onesafe.domain.repository.ClipboardRepository
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.usecase.ResizeIconUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardClearUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardContainsSafeDataUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardCopyTextUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardScheduleClearUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardShouldClearUseCase
import studio.lunabee.onesafe.domain.usecase.settings.DefaultSafeSettingsProvider
import studio.lunabee.onesafe.feature.autofill.AutoFillActivity
import studio.lunabee.onesafe.feature.camera.CameraActivity
import studio.lunabee.onesafe.feature.clipboard.AndroidClearClipboardUseCase
import studio.lunabee.onesafe.feature.clipboard.AndroidClipboardContainsSafeDataUseCase
import studio.lunabee.onesafe.feature.clipboard.AndroidClipboardScheduleClearUseCase
import studio.lunabee.onesafe.feature.clipboard.AndroidCopyFieldUseCase
import studio.lunabee.onesafe.feature.fileviewer.loadfile.AndroidLoadFileCancelAllUseCase
import studio.lunabee.onesafe.help.main.HelpActivity
import studio.lunabee.onesafe.ime.ui.biometric.BiometricActivity
import studio.lunabee.onesafe.qualifier.AppScope
import studio.lunabee.onesafe.qualifier.ImageCacheDirectory
import studio.lunabee.onesafe.usecase.AndroidDuplicatedNameTransform
import studio.lunabee.onesafe.usecase.HardCodedDefaultSafeSettingsProvider
import studio.lunabee.onesafe.usecase.LoadingManagerIsAppBlockedUseCase
import java.io.File
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object FrameworkModule {

    private const val ITEM_ICON_SIZE_DP: Float = 90f

    @Provides
    fun provideResizeIconUseCase(
        @ApplicationContext context: Context,
        @ImageCacheDirectory iconFileDir: File,
        androidResizeIconUseCaseFactory: AndroidResizeIconUseCaseFactory,
    ): ResizeIconUseCase {
        val r: Resources = context.resources
        val px = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            ITEM_ICON_SIZE_DP,
            r.displayMetrics,
        ).toInt()

        return androidResizeIconUseCaseFactory.create(
            width = px,
            height = px,
            tmpDir = iconFileDir,
        )
    }

    @Provides
    fun provideDuplicatedNameTransform(@ApplicationContext context: Context): DuplicatedNameTransform {
        return AndroidDuplicatedNameTransform(context)
    }

    @Provides
    fun provideLoadFileCancelAllUseCase(
        fileRepository: FileRepository,
        workManager: WorkManager,
    ): LoadFileCancelAllUseCase {
        return AndroidLoadFileCancelAllUseCase(fileRepository, workManager)
    }

    @ImageCacheDirectory
    @Provides
    fun provideImageCacheDirectory(@ApplicationContext context: Context): File {
        return context.getCacheImageFolder()
    }

    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Import)
    @Provides
    fun provideArchiveExtractedDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, AppConstants.FileProvider.ArchiveExtractedDirectoryName)
    }

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Export)
    fun provideArchiveExportedDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, AppConstants.FileProvider.ArchiveExportedDirectoryName)
    }

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.AutoBackup)
    fun provideArchiveAutoBackupDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, AppConstants.FileProvider.ArchiveAutoBackupDirectoryName)
    }

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Message)
    fun provideArchiveMessageDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, AppConstants.FileProvider.ArchiveMessageDirectoryName)
    }

    @Provides
    @ForceUpgradeUrl
    fun provideForceUpgradeUrl(): String = AppConstants.UrlConstant.ForceUpgradeUrl

    @Provides
    @BuildNumber
    fun provideBuildNumber(): Int = BuildConfig.VERSION_CODE

    @Provides
    @VersionName
    fun provideVersionName(): String = BuildConfig.VERSION_NAME

    @Provides
    @InternalBackupMimetype
    fun provideInternalBackupMimetype(): String {
        return BuildConfig.CUSTOM_BACKUP_MIMETYPE
    }

    @Provides
    @RemoteDir(RemoteDir.Type.Backups)
    fun provideRemoteBackupsDir(): String = if (BuildConfig.IS_DEV) {
        "OS6 Dev auto-backups"
    } else {
        "oneSafe 6 auto-backups"
    }

    @Provides
    @StoreBetaTrack
    fun provideStoreBetaTrack(): Boolean = BuildConfig.IS_BETA

    @Provides
    @Singleton
    fun provideLoadingManager(): LoadingManager = DelayedLoadingManager(LBLoadingVisibilityDelayDelegate())

    @Provides
    fun provideIsAppBlockedUseCase(loadingManager: LoadingManager): IsAppBlockedUseCase = LoadingManagerIsAppBlockedUseCase(loadingManager)

    @Provides
    fun provideClipboardClearUseCase(
        @ApplicationContext context: Context,
        clipboardShouldClearUseCase: ClipboardShouldClearUseCase,
        clipboardRepository: ClipboardRepository,
    ): ClipboardClearUseCase {
        return AndroidClearClipboardUseCase(context, clipboardShouldClearUseCase, clipboardRepository)
    }

    @Provides
    fun provideDefaultSafeSettingsProvider(
        clock: Clock,
    ): DefaultSafeSettingsProvider = HardCodedDefaultSafeSettingsProvider(clock)

    @OptIn(DelicateCoroutinesApi::class)
    @Singleton
    @AppScope
    @Provides
    fun provideAppScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + defaultDispatcher)
    }

    @Provides
    fun provideWorkManager(
        @ApplicationContext context: Context,
    ): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideAppComponents(
        @ApplicationContext context: Context,
    ): Array<ComponentName> {
        return arrayOf(
            ComponentName(context.packageName, MainActivity::class.qualifiedName!!),
            ComponentName(context.packageName, HelpActivity::class.qualifiedName!!),
            ComponentName(context.packageName, CameraActivity::class.qualifiedName!!),
            ComponentName(context.packageName, AutoFillActivity::class.qualifiedName!!),
            ComponentName(context.packageName, BiometricActivity::class.qualifiedName!!),
            ComponentName(context.packageName, FinishSetupDatabaseActivity::class.qualifiedName!!),
        )
    }

    @Provides
    fun provideClipboardShouldClearUseCase(
        @ApplicationContext context: Context,
    ): ClipboardContainsSafeDataUseCase {
        return AndroidClipboardContainsSafeDataUseCase(context)
    }

    @Provides
    fun provideClipboardScheduleClearUseCase(
        @ApplicationContext context: Context,
    ): ClipboardScheduleClearUseCase {
        return AndroidClipboardScheduleClearUseCase(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FrameworkGlobalModule {

    @Provides
    fun provideClipboardCopyTextUseCase(
        @ApplicationContext context: Context,
        clipboardRepository: ClipboardRepository,
        clipboardScheduleClearUseCase: ClipboardScheduleClearUseCase,
    ): ClipboardCopyTextUseCase {
        return AndroidCopyFieldUseCase(context, clipboardRepository, clipboardScheduleClearUseCase)
    }

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Migration)
    fun provideArchiveMigratedDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, AppConstants.FileProvider.ArchiveMigratedDirectoryName)
    }
}
