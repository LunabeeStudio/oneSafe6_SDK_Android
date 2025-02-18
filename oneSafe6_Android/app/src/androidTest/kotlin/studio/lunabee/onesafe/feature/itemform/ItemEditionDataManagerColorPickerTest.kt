package studio.lunabee.onesafe.feature.itemform

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.core.net.toUri
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.SimpleLoadingManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.feature.camera.model.CameraData
import studio.lunabee.onesafe.feature.camera.model.OSMediaType
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryWithTemplate
import studio.lunabee.onesafe.feature.itemform.destination.ItemCreationDestination
import studio.lunabee.onesafe.feature.itemform.manager.ExternalPhotoCapture
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture
import studio.lunabee.onesafe.feature.itemform.manager.ItemEditionDataManagerDefault
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataFieldObserver
import studio.lunabee.onesafe.feature.itemform.manager.UrlMetadataManager
import studio.lunabee.onesafe.feature.itemform.screen.ItemFormRoute
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemFormInitialInfo
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.creation.PopulateScreenFromTemplateDelegate
import studio.lunabee.onesafe.feature.itemform.viewmodel.impl.ItemCreationViewModel
import studio.lunabee.onesafe.feature.itemform.viewmodel.state.ItemFormState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.usecase.GetItemFormTipsToSeeUseCase
import studio.lunabee.onesafe.usecase.SaveItemFormTipsSeenUseCase
import java.io.File
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class, FlowPreview::class)
@HiltAndroidTest
class ItemEditionDataManagerColorPickerTest : LbcComposeTest() {

    @get:Rule(order = 0)
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val itemEditionDataManager = ItemEditionDataManagerDefault(
        context = context,
        getAppSettingUseCase = mockk {
            every { cameraSystemFlow() } returns flowOf(OSTestConfig.cameraSystem)
        },
        imageHelper = ImageHelper(Dispatchers.Default, Dispatchers.IO),
    )
    private val initialColor = Color.Red
    private val urlMetadataManager = mockk<UrlMetadataManager> {
        every { isLoading } returns MutableStateFlow(null)
        every { urlMetadata } returns MutableSharedFlow()
    }

    private val getItemFormTipsToSeeUseCase: GetItemFormTipsToSeeUseCase = mockk()
    private val saveItemFormTipsSeenUseCase: SaveItemFormTipsSeenUseCase = mockk()

    private val populateScreenDelegate = PopulateScreenFromTemplateDelegate(
        savedStateHandle = mockk {
            every { this@mockk.get<String>(ItemCreationDestination.ItemTypeArg) } returns ItemCreationEntryWithTemplate.Template.Custom.name
            every { this@mockk.get<Int>(ItemCreationDestination.ItemParentColorArg) } returns -1
        },
        itemEditionFileManager = mockk(),
        itemEditionDataManager = itemEditionDataManager,
        fieldIdProvider = mockk(),
        context = context,
        resizeImageManager = mockk(),
        urlMetadataManager = urlMetadataManager,
    )
    private val spyPopulateScreenDelegate: PopulateScreenFromTemplateDelegate = spyk(populateScreenDelegate) {
        val initialInfo = ItemFormInitialInfo("", null, null, emptyList(), false)
        coEvery { getInitialInfo() } returns LBResult.Success(initialInfo)
    }

    private val cameraData = when (OSTestConfig.cameraSystem) {
        CameraSystem.InApp -> CameraData.InApp(InAppMediaCapture(null, null, OSMediaType.PHOTO))
        CameraSystem.External -> CameraData.External(lazy { ExternalPhotoCapture(File(""), "".toUri()) })
    }

    private val viewModel: ItemCreationViewModel = spyk(
        ItemCreationViewModel(
            saveItemAndFieldDelegate = mockk(),
            populateScreenFromTemplateDelegate = spyPopulateScreenDelegate,
            urlMetadataManager = urlMetadataManager,
            itemEditionDataManager = itemEditionDataManager,
            itemEditionFileManager = mockk {
                every { dialogState } returns MutableStateFlow(null)
                every { prepareDataForFieldImageCapture() } returns flowOf(cameraData)
            },
            fieldIdProvider = mockk(),
            urlMetadataFieldObserver = UrlMetadataFieldObserver(urlMetadataManager),
            loadingManager = SimpleLoadingManager(),
            getItemFormTipsToSeeUseCase = getItemFormTipsToSeeUseCase,
            saveItemFormTipsSeenUseCase = saveItemFormTipsSeenUseCase,
        ),
    )

    @Before
    fun setUp() {
        hiltRule.inject()

        every { viewModel.itemFormState } returns MutableStateFlow(ItemFormState.Idle(cameraData, cameraData, null))
        every { viewModel.uiFields } returns MutableStateFlow(listOf())
        every { viewModel.itemIconLoading } returns MutableStateFlow(null)
        every { viewModel.dialogState } returns MutableStateFlow(null)
        every { viewModel.isSaveEnabled() } returns OSActionState.Enabled
        every { viewModel.snackbarData } returns MutableStateFlow(null)
        every { viewModel.canFetchFromUrl() } returns true

        // Set an initial color
        itemEditionDataManager.setColorSelected(initialColor)
        itemEditionDataManager.saveSelectedColor()
    }

    @Test
    fun pick_color_without_validation() {
        setContent {
            openColorPicker()
            pickColor()

            runBlocking {
                itemEditionDataManager.colorPreview.filter { it != initialColor }.timeout(2.seconds)
                itemEditionDataManager.colorCandidate.filter { it == initialColor }.timeout(2.seconds)
            }
        }
    }

    @Test
    fun pick_color_and_cancel() {
        setContent {
            openColorPicker()
            pickColor()
            Espresso.pressBack()

            runBlocking {
                itemEditionDataManager.colorCandidate.filter { it == initialColor }.timeout(2.seconds)
                itemEditionDataManager.colorPreview.filter { it == itemEditionDataManager.colorCandidate.value }.timeout(2.seconds)
            }
        }
    }

    @Test
    fun validate_without_picking_new_color() {
        setContent {
            openColorPicker()
            validatePicker()

            runBlocking {
                itemEditionDataManager.colorCandidate.filter { it == initialColor }.timeout(2.seconds)
                itemEditionDataManager.colorPreview.filter { it == itemEditionDataManager.colorCandidate.value }.timeout(2.seconds)
            }
        }
    }

    @Test
    fun pick_new_color_and_validate() {
        setContent {
            openColorPicker()
            pickColor()
            validatePicker()

            runBlocking {
                itemEditionDataManager.colorCandidate.filter { it != initialColor }.timeout(2.seconds)
                itemEditionDataManager.colorPreview.filter { it == itemEditionDataManager.colorCandidate.value }.timeout(2.seconds)
            }
        }
    }

    @Test
    fun extract_image_color_then_pick_color() {
        setContent {
            // Remove initial color
            itemEditionDataManager.setColorSelected(null)
            itemEditionDataManager.saveSelectedColor()

            // Pick an image and extract color
            itemEditionDataManager.onItemIconPickedByUser(OSImageSpec.Data(iconSample))

            val extractedColorCandidate = runBlocking {
                itemEditionDataManager.colorCandidate.filter { it != initialColor }.timeout(2.seconds).first()
            }

            // Pick a color and validate.
            openColorPicker()
            pickColor()
            validatePicker()

            runBlocking {
                itemEditionDataManager.colorPreview.combine(itemEditionDataManager.colorCandidate) { colorPreview, colorCandidate ->
                    colorPreview == colorCandidate
                }.filter { it }.timeout(2.seconds)
                // Check final color is nether the initial nor the image extracted one
                itemEditionDataManager.colorCandidate.filter { it != initialColor }.timeout(2.seconds)
                itemEditionDataManager.colorCandidate.filter { it != extractedColorCandidate }.timeout(2.seconds)
            }
        }
    }

    private fun setContent(
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                ItemFormRoute(
                    navigateBack = {},
                    navigateToItemDetails = {},
                    viewModel = viewModel,
                    screenTitle = LbcTextSpec.StringResource(OSString.safeItemDetail_newItem_title),
                )
            }
            block()
        }
    }

    companion object ColorPickerHelper {
        context(ComposeUiTest)
        fun openColorPicker() {
            hasTestTag(testTag = UiConstants.TestTag.Item.ColorPickerButton)
                .waitUntilExactlyOneExists()
                .performClick()
        }

        context(ComposeUiTest)
        fun validatePicker() {
            hasTestTag(testTag = UiConstants.TestTag.Item.SaveColorButton)
                .waitUntilExactlyOneExists()
                .performClick()
            waitForIdle()
        }

        context(ComposeUiTest)
        fun pickColor() {
            hasTestTag(testTag = UiConstants.TestTag.Item.ColorPicker)
                .waitUntilExactlyOneExists()
                .performTouchInput {
                    this.click(
                        this.percentOffset(
                            x = OSTestConfig.random.nextDouble(0.25, 0.75).toFloat(),
                            y = OSTestConfig.random.nextDouble(0.25, 0.75).toFloat(),
                        ),
                    )
                }
            waitForIdle()
        }
    }
}
