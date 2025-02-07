package studio.lunabee.onesafe.feature.itemform

import androidx.compose.ui.graphics.Color
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.SimpleLoadingManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionFileFieldManager
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataManager
import studio.lunabee.onesafe.feature.itemform.model.ItemFieldType
import studio.lunabee.onesafe.feature.itemform.model.StandardItemFieldType
import studio.lunabee.onesafe.feature.itemform.model.uifield.FieldObserver
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.text.impl.UrlTextUiField
import studio.lunabee.onesafe.feature.itemform.viewmodel.ItemFormViewModel
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemFormInitialInfo
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.PopulateScreenDelegate
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.test.IncrementalIdProvider
import studio.lunabee.onesafe.test.OSUiThreadTest
import studio.lunabee.onesafe.test.testUUIDs
import java.util.UUID
import kotlin.test.assertContentEquals

class ItemFormViewModelTest : OSUiThreadTest() {
    private val initialColor = Color.Red

    val itemEditionDataManager: ItemEditionDataManagerDefault = mockk {
        every { nameField } returns mockk {
            every { getDisplayedValue() } returns "name"
            every { onValueChanged(any()) } returns Unit
            every { isInError() } returns false
        }
        every { colorCandidate } returns MutableStateFlow(null)
        every { this@mockk.itemIcon } returns MutableStateFlow(null)
        every { dialogState } returns MutableStateFlow(null)
        every { prepareDataForItemIconCapture() } returns flowOf(mockk())
    }

    val itemEditionFileManager: ItemEditionFileFieldManager = mockk {
        every { dialogState } returns MutableStateFlow(null)
        every { prepareDataForFieldImageCapture() } returns flowOf(mockk())
    }

    val populateScreenDelegate: PopulateScreenDelegate = object : PopulateScreenDelegate {
        override suspend fun getInitialInfo(): LBResult<ItemFormInitialInfo> {
            return LBResult.Success(
                ItemFormInitialInfo(
                    name = "name",
                    color = initialColor,
                    icon = null,
                    fields = emptyList(),
                    isFromCamera = false,
                ),
            )
        }

        override fun CoroutineScope.loadInitialInfo(info: ItemFormInitialInfo): Job = launch { }
    }

    val urlMetadataManager: UrlMetadataManager = mockk {
        every { isLoading } returns MutableStateFlow(null)
        every { urlMetadata } returns MutableSharedFlow()
    }

    private var actualObservedValues: MutableList<String> = mutableListOf()

    private val viewModel by lazy {
        object : ItemFormViewModel(
            saveItemAndFieldDelegate = mockk(),
            populateScreenDelegate = populateScreenDelegate,
            urlMetadataManager = urlMetadataManager,
            itemEditionDataManager = itemEditionDataManager,
            itemEditionFileManager = itemEditionFileManager,
            fieldIdProvider = FieldIdProvider(IncrementalIdProvider()),
            urlMetadataFieldObserver = object : FieldObserver {
                override fun onValueChanged(value: String) {
                    actualObservedValues += value
                }

                override fun onRemoved() {
                    /* no-op */
                }
            },
            loadingManager = SimpleLoadingManager(),
        ) {
            override fun checkTipsToDisplay(fields: List<UiField>) = Unit

            override fun isSaveEnabled(): OSActionState = OSActionState.Enabled

            override suspend fun save(
                name: String,
                icon: OSImageSpec?,
                color: Color?,
                itemFieldsData: List<ItemFieldData>,
            ): LBResult<UUID> {
                return LBResult.Success(testUUIDs[0])
            }
        }
    }

    @Test
    fun urlFields_only_first_observe_test(): TestResult = runTest {
        viewModel.addField(ItemFieldType.getMainItemFieldTypes().first { it.id == "url" } as StandardItemFieldType)
        viewModel.addField(ItemFieldType.getMainItemFieldTypes().first { it.id == "url" } as StandardItemFieldType)

        val value = viewModel.uiFields.first()
        val urlFieldA = value[0] as UrlTextUiField
        val urlFieldB = value[1] as UrlTextUiField

        urlFieldB.onValueChanged("b1")
        urlFieldA.onValueChanged("a1")
        urlFieldB.onValueChanged("b2")
        urlFieldA.onValueChanged("a2")

        assertContentEquals(listOf("a1", "a2"), actualObservedValues)
        actualObservedValues.clear()

        // re-order
        viewModel.updateFieldsOrder(listOf(urlFieldB, urlFieldA))
        viewModel.uiFields.first() // emulate ui refresh

        urlFieldB.onValueChanged("b1")
        urlFieldA.onValueChanged("a1")
        urlFieldB.onValueChanged("b2")
        urlFieldA.onValueChanged("a2")

        assertContentEquals(listOf("b1", "b2"), actualObservedValues)
        actualObservedValues.clear()

        // remove field b
        viewModel.removeField(urlFieldB)
        viewModel.uiFields.first() // emulate ui refresh

        urlFieldB.onValueChanged("b1")
        urlFieldA.onValueChanged("a1")
        urlFieldB.onValueChanged("b2")
        urlFieldA.onValueChanged("a2")

        assertContentEquals(listOf("a1", "a2"), actualObservedValues)
    }
}
