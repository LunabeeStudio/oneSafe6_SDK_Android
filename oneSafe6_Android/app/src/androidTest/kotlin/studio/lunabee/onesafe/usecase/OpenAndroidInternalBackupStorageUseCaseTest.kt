/*
 * Copyright (c) 2024-2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 9/6/2024 - for the oneSafe6 SDK.
 * Last modified 9/6/24, 3:26 PM
 */

package studio.lunabee.onesafe.usecase

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.importexport.usecase.OpenAndroidInternalBackupStorageUseCase
import studio.lunabee.onesafe.test.InitialTestState
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.test.assertFalse

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class OpenAndroidInternalBackupStorageUseCaseTest : OSMainActivityTest() {

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject
    lateinit var useCaseFilter: OpenAndroidInternalBackupStorageUseCase

    /**
     * Make sure we don't show oneSafe itself in the app chooser to open the internal backup storage
     * https://console.firebase.google.com/project/onesafe-revival/crashlytics/app/android:studio.lunabee.onesafe/issues/5588cbc36e5e627826f8fdd084ecb63f
     */
    @Test
    fun don_t_show_oneSafe_test() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        val useCaseNoFilter = OpenAndroidInternalBackupStorageUseCase(arrayOf())

        // potential package of the app chooser
        val androidIntentResolverSelector = By.pkg(Pattern.compile("^com\\.android\\.intentresolver$|^android$"))
        val androidDocumentExplorerSelector = By.pkg(Pattern.compile("^com\\.google\\.android\\.documentsui$"))
        val appNameTextSelector = By.text(getString(OSString.application_name)).hasAncestor(androidIntentResolverSelector)

        invoke {
            val activity = activity!!

            waitForIdle()

            useCaseNoFilter(activity)
            waitUntil("app chooser is shown", 5_000) { device.hasObject(androidIntentResolverSelector) }
            waitUntil("oneSafe 6 is shown in the app chooser", 5_000) { device.hasObject(appNameTextSelector) }

            device.pressBack()

            waitUntil("app chooser is hidden", 5_000) { !device.hasObject(androidIntentResolverSelector) }

            useCaseFilter(activity)

            waitUntil("app chooser or system document app is shown", 5_000) {
                device.hasObject(androidIntentResolverSelector) || device.hasObject(androidDocumentExplorerSelector)
            }
            assertFalse(device.hasObject(appNameTextSelector))

            device.pressBack()
        }
    }
}
