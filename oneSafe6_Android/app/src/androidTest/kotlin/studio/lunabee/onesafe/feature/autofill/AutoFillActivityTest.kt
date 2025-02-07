package studio.lunabee.onesafe.feature.autofill

import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.runAndroidComposeUiTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSActivityTest
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.ui.UiConstants
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@HiltAndroidTest
class AutoFillActivityTest : OSActivityTest<AutoFillActivity>() {

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.SignedUp {
        createItemUseCase.test()
    }

    @OptIn(ExperimentalTestApi::class)
    operator fun invoke(
        effectContext: CoroutineContext = EmptyCoroutineContext,
        block: AndroidComposeUiTest<AutoFillActivity>.() -> Unit,
    ) {
        runAndroidComposeUiTest(effectContext = effectContext) {
            this@AutoFillActivityTest.activity = activity!!
            initKeyboardHelper()
            try {
                block()
            } catch (e: Throwable) {
                runCatching { onFailure(e) }
                throw e
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun launch_test(): Unit = invoke {
        hasTestTag(UiConstants.TestTag.Screen.Login).waitUntilExactlyOneExists()
        login()
        hasTestTag(UiConstants.TestTag.Screen.AutofillItemsListScreen).waitUntilExactlyOneExists()
    }
}
