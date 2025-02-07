package studio.lunabee.onesafe.navigation.note

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.extension.printToCacheDir
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.OSMainActivityTest
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.hasExcludeSearch
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
@OptIn(ExperimentalTestApi::class)
class ItemDetailsFieldFullScreenNavigationTest : OSMainActivityTest() {
    @get:Rule(order = 0)
    override var hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var fieldIdProvider: FieldIdProvider

    override val initialTestState: InitialTestState = InitialTestState.Home {
        safeItemId = createItemUseCase.test(itemName).id
        noteFieldId = addFieldUseCase(
            safeItemId,
            ItemFieldData(
                id = fieldIdProvider(),
                name = fieldName,
                position = 0.0,
                placeholder = null,
                value = noteValue,
                kind = SafeItemFieldKind.Note,
                showPrediction = false,
                isItemIdentifier = false,
                formattingMask = null,
                secureDisplayMask = null,
                isSecured = false,
            ),
        ).data!!.id
    }

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @Inject
    lateinit var addFieldUseCase: AddFieldUseCase

    private val itemName: String = "test item"
    lateinit var safeItemId: UUID
    private val noteValue: String = loremIpsum(30)
    private val fieldName: String = "note"
    lateinit var noteFieldId: UUID

    @Test
    fun item_detail_to_field_full_screen_test() {
        invoke {
            hasExcludeSearch(hasText(itemName))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.Screen.itemDetailsScreen(testUUIDs[0]))
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            hasText(fieldName)
                .waitUntilExactlyOneExists(useUnmergedTree = true)
                .performClick()
            onNodeWithTag(UiConstants.TestTag.Screen.ItemDetailsFieldFullScreen)
                .assertIsDisplayed()
            hasText(noteValue)
                .waitUntilExactlyOneExists()
                .assertIsDisplayed()
            onRoot()
                .printToCacheDir(printRule)
        }
    }
}
