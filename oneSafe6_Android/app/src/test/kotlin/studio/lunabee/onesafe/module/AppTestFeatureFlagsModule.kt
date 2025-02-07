package studio.lunabee.onesafe.module

import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import studio.lunabee.onesafe.FeatureFlagsModule

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [FeatureFlagsModule::class],
)
internal object AppTestFeatureFlagsModule
