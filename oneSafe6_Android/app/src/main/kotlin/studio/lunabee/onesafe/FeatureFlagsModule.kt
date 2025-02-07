package studio.lunabee.onesafe

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import studio.lunabee.onesafe.domain.common.FeatureFlags

@Module
@InstallIn(SingletonComponent::class)
internal object FeatureFlagsModule {
    @Provides
    fun provideFeatureFlags(
        @ApplicationContext context: Context,
    ): FeatureFlags = object : FeatureFlags {
        // Prod
        override fun florisBoard(): Boolean = true
        override fun sqlcipher(): Boolean = true
        override fun oneSafeK(): Boolean = florisBoard()
        override fun bubbles(): Boolean = true

        // Play services
        override fun cloudBackup(): Boolean =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        // Dev
        override fun accessibilityService(): Boolean = BuildConfig.IS_DEV
        override fun quickSignIn(): Boolean = BuildConfig.IS_DEV
        override fun backupWorkerExpedited(): Boolean = BuildConfig.IS_DEV
    }
}
