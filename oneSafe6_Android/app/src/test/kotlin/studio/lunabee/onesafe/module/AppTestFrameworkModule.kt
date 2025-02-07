package studio.lunabee.onesafe.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.FrameworkModule
import studio.lunabee.onesafe.common.extensions.getCacheImageFolder
import studio.lunabee.onesafe.domain.common.DuplicatedNameTransform
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.qualifier.ForceUpgradeUrl
import studio.lunabee.onesafe.qualifier.AppScope
import studio.lunabee.onesafe.qualifier.ImageCacheDirectory
import java.io.File
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FrameworkModule::class],
)
object AppTestFrameworkModule {

    const val COPY_STRING: String = "copy %s"

    @Provides
    fun provideDuplicatedNameTransform(): DuplicatedNameTransform {
        return DuplicatedNameTransform { originalName -> COPY_STRING.format(originalName) }
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
    @ForceUpgradeUrl
    fun provideForceUpgradeUrl(): String = AppConstants.UrlConstant.ForceUpgradeUrl

    @OptIn(DelicateCoroutinesApi::class)
    @Singleton
    @AppScope
    @Provides
    fun provideAppScope(
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
    ): CoroutineScope {
        return CoroutineScope(SupervisorJob() + defaultDispatcher)
    }
}
