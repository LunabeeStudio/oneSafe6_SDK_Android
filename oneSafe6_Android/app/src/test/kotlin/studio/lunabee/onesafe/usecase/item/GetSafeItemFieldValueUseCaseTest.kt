package studio.lunabee.onesafe.usecase.item

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetSafeItemFieldValueUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.assertThrows
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

@HiltAndroidTest
class GetSafeItemFieldValueUseCaseTest : OSHiltUnitTest() {

    override val testDispatcher: TestDispatcher = StandardTestDispatcher()

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var getSafeItemFieldValueUseCase: GetSafeItemFieldValueUseCase

    @Inject
    lateinit var createItemUseCase: CreateItemUseCase

    @Inject
    lateinit var addFieldUseCase: AddFieldUseCase

    @Inject
    lateinit var decryptUseCase: ItemDecryptUseCase

    @Inject
    lateinit var fieldIdProvider: FieldIdProvider

    lateinit var item: SafeItem
    lateinit var field: SafeItemField

    @Before
    fun setup() {
        runTest(testDispatcher) {
            item = createItemUseCase(
                name = null,
                parentId = null,
                isFavorite = false,
                icon = null,
                color = "#000000",
            ).data!!

            field = addFieldUseCase(
                itemId = item.id,
                itemFieldData = ItemFieldData(
                    id = fieldIdProvider(),
                    name = "Test",
                    position = 0.0,
                    placeholder = null,
                    value = "Value",
                    kind = SafeItemFieldKind.Text,
                    showPrediction = false,
                    isItemIdentifier = false,
                    formattingMask = null,
                    secureDisplayMask = null,
                    isSecured = false,
                ),
            ).data!!
        }
    }

    @Test
    fun success_case_test() {
        runTest {
            val result = getSafeItemFieldValueUseCase(itemId = item.id, fieldId = field.id).first().data!!
            assertEquals(expected = decryptUseCase(item.encColor!!, item.id, String::class).data!!, actual = result.color)
            assertEquals(expected = decryptUseCase(field.encValue!!, item.id, String::class).data!!, actual = result.fieldValue)
            assertEquals(expected = decryptUseCase(field.encName!!, item.id, String::class).data!!, actual = result.fieldName)
        }
    }

    @OptIn(FlowPreview::class)
    @Test
    fun no_crypto_test() {
        runTest {
            cryptoRepository.unloadMasterKeys()
            val resultFlow = getSafeItemFieldValueUseCase(itemId = item.id, fieldId = field.id).timeout(100.milliseconds)
            assertThrows<TimeoutCancellationException> {
                resultFlow.first()
            }
        }
    }
}
