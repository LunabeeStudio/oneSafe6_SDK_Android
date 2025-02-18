package studio.lunabee.onesafe.feature.itemform.uifield

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilAtLeastOneExists
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.DateAndTimeUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.DateUiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.time.impl.TimeUiField
import studio.lunabee.onesafe.ui.UiConstants
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class TimeRelatedUiFieldTest : LbcComposeTest() {
    private val fieldDescription: String = "test field"
    private val initialDate = LocalDateTime.of(2000, 1, 1, 0, 0)

    /**
     * Testing if dateUiField trigger the datepicker and the value is modified on confirm
     */
    @Test
    fun date_picker_test() {
        lateinit var context: Context
        val uiField = DateUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Date,
        )
        uiField.dateTime = initialDate
        invoke {
            setContent {
                context = LocalContext.current
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }
            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performClick()
            isDialog().waitUntilAtLeastOneExists()
            onNodeWithText(context.getString(OSString.common_confirm)).performClick()
            assertNotNull(uiField.dateTime)
            onNodeWithText(AppConstants.Ui.TimeRelatedFieldFormatter.DateFormatter.format(initialDate))
                .assertIsDisplayed()
        }
    }

    /**
     * Testing if TimeUiField trigger the TimePicker and the value is modified on confirm
     */
    @Test
    fun time_picker_test() {
        lateinit var context: Context
        val uiField = TimeUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.Date,
        )
        uiField.dateTime = initialDate
        invoke {
            setContent {
                context = LocalContext.current
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }
            onNodeWithTag(UiConstants.TestTag.Item.ItemFormField).performClick()
            isDialog().waitUntilAtLeastOneExists()
            onNodeWithText(context.getString(OSString.common_confirm)).performClick()
            assertNotNull(uiField.dateTime)
            onNodeWithText(
                AppConstants.Ui.TimeRelatedFieldFormatter.TimeFormatter.format(initialDate.truncatedTo(ChronoUnit.DAYS)),
            ).assertIsDisplayed()
        }
    }

    /**
     * Testing date and hour ui field
     * - Selecting an hour -> the hour is well displayed in the field
     * - Selecting a day -> day is well displayed in the field and the hour is kept
     */
    @Test
    fun date_and_time_picker_test() {
        val testedHour = 11
        val testedDayOfMonth = 15

        lateinit var context: Context
        val uiField = DateAndTimeUiField(
            id = UUID.randomUUID(),
            fieldDescription = mutableStateOf(LbcTextSpec.Raw(fieldDescription)),
            placeholder = LbcTextSpec.Raw(fieldDescription),
            safeItemFieldKind = SafeItemFieldKind.DateAndHour,
        )
        uiField.dateTime = initialDate
        invoke {
            setContent {
                context = LocalContext.current
                uiField.MainComposable(
                    modifier = Modifier,
                    hasNext = false,
                    toggleIdentifier = {},
                    renameField = {},
                    removeField = {},
                    useThumbnailAsIcon = {},
                )
            }
            onNodeWithTag(UiConstants.TestTag.Item.TimePickerAction).performClick()
            isDialog().waitUntilAtLeastOneExists()
            onNodeWithContentDescription(testedHour.toString(), substring = true).performClick()
            onNodeWithText(context.getString(OSString.common_confirm)).performClick()

            val expectedDate = initialDate
                .withHour(testedHour)

            onNodeWithText(AppConstants.Ui.TimeRelatedFieldFormatter.DateAndTimeFormatter.format(expectedDate)).assertIsDisplayed()

            onNodeWithTag(UiConstants.TestTag.Item.DatePickerAction).performClick()
            isDialog().waitUntilAtLeastOneExists()
            hasText(testedDayOfMonth.toString(), substring = true).waitUntilExactlyOneExists(useUnmergedTree = true).performClick()
            onNodeWithText(context.getString(OSString.common_confirm)).performClick()

            val expectedDateModified = initialDate
                .withHour(testedHour)
                .withDayOfMonth(testedDayOfMonth)
                .withMinute(0)
            onNodeWithText(AppConstants.Ui.TimeRelatedFieldFormatter.DateAndTimeFormatter.format(expectedDateModified))
                .assertIsDisplayed()
        }
    }
}
