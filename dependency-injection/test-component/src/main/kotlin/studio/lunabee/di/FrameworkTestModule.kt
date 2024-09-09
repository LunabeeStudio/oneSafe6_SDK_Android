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
 * Created by Lunabee Studio / Date - 9/29/2023 - for the oneSafe6 SDK.
 * Last modified 9/29/23, 9:58 PM
 */

package studio.lunabee.di

import android.content.ComponentName
import android.content.Context
import com.lunabee.lbcore.helper.LBLoadingVisibilityDelayDelegate
import com.lunabee.lbloading.DelayedLoadingManager
import com.lunabee.lbloading.LoadingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.lunabee.onesafe.SettingsDefaults
import studio.lunabee.onesafe.commonui.usecase.AndroidResizeIconUseCaseFactory
import studio.lunabee.onesafe.domain.LoadFileCancelAllUseCase
import studio.lunabee.onesafe.domain.manager.IsAppBlockedUseCase
import studio.lunabee.onesafe.domain.model.safe.SafeSettings
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.BuildNumber
import studio.lunabee.onesafe.domain.qualifier.InternalBackupMimetype
import studio.lunabee.onesafe.domain.qualifier.RemoteDir
import studio.lunabee.onesafe.domain.qualifier.StoreBetaTrack
import studio.lunabee.onesafe.domain.qualifier.VersionName
import studio.lunabee.onesafe.domain.repository.FileRepository
import studio.lunabee.onesafe.domain.usecase.ResizeIconUseCase
import studio.lunabee.onesafe.domain.usecase.clipboard.ClipboardClearUseCase
import studio.lunabee.onesafe.domain.usecase.settings.DefaultSafeSettingsProvider
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.OSTestConfig.clock
import java.io.File
import java.util.UUID
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Module
@InstallIn(SingletonComponent::class)
object FrameworkTestModule {

    const val RESIZE_ICON_SIZE: Int = 50
    const val ICON_DIR: String = "testDir"

    @Provides
    @Singleton
    internal fun provideResizeIconUseCase(
        @ApplicationContext context: Context,
        androidResizeIconUseCaseFactory: AndroidResizeIconUseCaseFactory,
    ): ResizeIconUseCase {
        val tmpDir = File(context.cacheDir, ICON_DIR)
        return androidResizeIconUseCaseFactory.create(
            width = RESIZE_ICON_SIZE,
            height = RESIZE_ICON_SIZE,
            tmpDir = tmpDir,
        )
    }

    @Provides
    @BuildNumber
    fun provideBuildNumber(): Int = 0

    @Provides
    @VersionName
    fun provideVersionName(): String = "6.0.0.0"

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.AutoBackup)
    fun provideArchiveAutoBackupDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, "keep_archiveAutoBackup")
    }

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Message)
    fun provideArchiveMessageDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, "keep_archivemessage")
    }

    @Provides
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Export)
    fun provideArchiveExportedDirectory(@ApplicationContext context: Context): File {
        return File(context.cacheDir, "keep_archiveExported")
    }

    @Provides
    fun provideLoadFileCancelAllUseCase(
        fileRepository: FileRepository,
    ): LoadFileCancelAllUseCase {
        return object : LoadFileCancelAllUseCase {
            override suspend operator fun invoke(itemId: UUID) {
                fileRepository.deleteItemDir(itemId)
            }

            override suspend operator fun invoke() {
                fileRepository.deletePlainFilesCacheDir()
            }
        }
    }

    @Provides
    @InternalBackupMimetype
    fun provideInternalBackupMimetype(): String {
        return "application/onesafe6_debug"
    }

    @Provides
    @RemoteDir(RemoteDir.Type.Backups)
    fun provideRemoteBackupsDir(): String = "OS6 Test auto-backups"

    @Provides
    @StoreBetaTrack
    fun provideStoreBetaTrack(): Boolean = false

    @Provides
    @Singleton
    fun provideLoadingManager(): LoadingManager = DelayedLoadingManager(LBLoadingVisibilityDelayDelegate())

    @Provides
    fun provideIsAppBlockedUseCase(loadingManager: LoadingManager): IsAppBlockedUseCase = object : IsAppBlockedUseCase {
        override fun flow(): Flow<Boolean> = loadingManager.loadingState.map { it.isBlocking }
        override suspend operator fun invoke(): Boolean = loadingManager.loadingState.value.isBlocking
    }

    @Provides
    fun provideClipboardClearUseCase(): ClipboardClearUseCase {
        return object : ClipboardClearUseCase {
            override suspend fun invoke() {
                /* no-op */
            }
        }
    }

    @Provides
    fun provideDefaultSafeSettingsProvider(): DefaultSafeSettingsProvider = object : DefaultSafeSettingsProvider {
        override fun invoke(): SafeSettings =
            SafeSettings(
                version = Int.MAX_VALUE,
                materialYou = SettingsDefaults.MaterialYouSettingDefault,
                automation = SettingsDefaults.AutomationSettingDefault,
                displayShareWarning = SettingsDefaults.DisplayShareWarningDefault,
                allowScreenshot = SettingsDefaults.AllowScreenshotSettingDefault,
                shakeToLock = SettingsDefaults.ShakeToLockSettingDefault,
                bubblesPreview = SettingsDefaults.BubblesPreviewDefault,
                cameraSystem = OSTestConfig.cameraSystem,
                autoLockOSKHiddenDelay = SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds,
                verifyPasswordInterval = SettingsDefaults.VerifyPasswordIntervalDefault,
                bubblesHomeCardCtaState = SettingsDefaults.BubblesPreviewCardDefault,
                autoLockInactivityDelay = SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds,
                autoLockAppChangeDelay = SettingsDefaults.AutoLockAppChangeDelayMsDefault.milliseconds,
                clipboardDelay = SettingsDefaults.ClipboardClearDelayMsDefault.milliseconds,
                bubblesResendMessageDelay = SettingsDefaults.BubblesResendMessageDelayMsDefault.milliseconds,
                autoLockOSKInactivityDelay = SettingsDefaults.AutoLockInactivityDelayMsDefault.milliseconds,
                autoBackupEnabled = SettingsDefaults.AutoBackupEnabledDefault,
                autoBackupFrequency = SettingsDefaults.AutoBackupFrequencyMsDefault.milliseconds,
                autoBackupMaxNumber = SettingsDefaults.AutoBackupMaxNumberDefault,
                cloudBackupEnabled = SettingsDefaults.CloudBackupEnabledDefault,
                keepLocalBackupEnabled = SettingsDefaults.KeepLocalBackupEnabledDefault,
                itemOrdering = SettingsDefaults.ItemOrderingDefault,
                itemLayout = SettingsDefaults.ItemLayoutDefault,
                enableAutoBackupCtaState = SettingsDefaults.EnableAutoBackupCtaState,
                lastPasswordVerification = SettingsDefaults.lastPasswordVerificationDefault(clock),
                independentSafeInfoCtaState = SettingsDefaults.independentSafeInfoCtaState(clock),
            )
    }

    @Provides
    fun provideAppComponents(): Array<ComponentName> {
        return arrayOf(
            ComponentName("studio.lunabee.onesafe", "studio.lunabee.onesafe.MainActivity"),
        )
    }
}
