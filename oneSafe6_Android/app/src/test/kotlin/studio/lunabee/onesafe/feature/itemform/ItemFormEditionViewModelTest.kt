package studio.lunabee.onesafe.feature.itemform

import androidx.compose.ui.graphics.Color
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.SimpleLoadingManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Test
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.feature.itemform.destination.ItemEditionDestination
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionFileFieldManager
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataFieldObserver
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataManager
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemFormInitialInfo
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.edition.PopulateScreenFromItemDelegate
import studio.lunabee.onesafe.feature.itemform.viewmodel.impl.ItemEditionViewModel
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.test.IncrementalIdProvider
import studio.lunabee.onesafe.test.OSUiThreadTest
import java.util.UUID
import kotlin.test.assertEquals

class ItemFormEditionViewModelTest : OSUiThreadTest() {
    private val initialColor = Color.Red

    private val itemEditionDataManager: ItemEditionDataManagerDefault = mockk {
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

    private val itemEditionFileManager: ItemEditionFileFieldManager = mockk {
        every { dialogState } returns MutableStateFlow(null)
        every { prepareDataForFieldImageCapture() } returns flowOf(mockk())
    }

    private val populateScreenDelegate = PopulateScreenFromItemDelegate(
        savedStateHandle = mockk {
            every { this@mockk.get<String>(ItemEditionDestination.ItemIdArg) } returns UUID.randomUUID().toString()
        },
        getItemEditionInfoUseCase = mockk(),
        decryptUseCase = mockk(),
        getIconUseCase = mockk(),
        itemEditionDataManager = mockk(),
        loadFileUseCase = mockk(),
        getThumbnailFromFileUseCase = mockk(),
    )

    private val spyPopulateScreenDelegate: PopulateScreenFromItemDelegate = spyk(populateScreenDelegate) {
        val initialInfo = ItemFormInitialInfo(
            name = "name",
            color = initialColor,
            icon = null,
            fields = emptyList(),
            isFromCamera = false,
        )
        coEvery { getInitialInfo() } returns LBResult.Success(initialInfo)
    }

    private val urlMetadataManager: UrlMetadataManager = mockk {
        every { isLoading } returns MutableStateFlow(null)
        every { urlMetadata } returns MutableSharedFlow()
    }

    private val viewModel by lazy {
        ItemEditionViewModel(
            saveItemAndFieldDelegate = mockk(),
            populateScreenDelegate = spyPopulateScreenDelegate,
            urlMetadataManager = urlMetadataManager,
            itemEditionDataManager = itemEditionDataManager,
            itemEditionFileManager = itemEditionFileManager,
            fieldIdProvider = FieldIdProvider(IncrementalIdProvider()),
            urlMetadataFieldObserver = UrlMetadataFieldObserver(urlMetadataManager),
            loadingManager = SimpleLoadingManager(),
        )
    }

    @Test
    fun isSaveEnabled_color_picked_test() {
        every { itemEditionDataManager.colorCandidate } returns MutableStateFlow(null)
        assertEquals(OSActionState.DisabledWithAction, viewModel.isSaveEnabled())
        every { itemEditionDataManager.colorCandidate } returns MutableStateFlow(initialColor)
        assertEquals(OSActionState.DisabledWithAction, viewModel.isSaveEnabled())
        every { itemEditionDataManager.colorCandidate } returns MutableStateFlow(Color.Blue)
        assertEquals(OSActionState.Enabled, viewModel.isSaveEnabled())
    }
}
