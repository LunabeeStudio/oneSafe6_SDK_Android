package studio.lunabee.onesafe.feature.home

import android.content.Context
import androidx.paging.PagingData
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbextensions.lazyFast
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.utils.LocaleCompat
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsCurrentSafeBiometricEnabledUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.autolock.LockAppUseCase
import studio.lunabee.onesafe.domain.usecase.item.CountAllFavoriteUseCase
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.FindLastFavoriteUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetAppVisitUseCase
import studio.lunabee.onesafe.domain.usecase.settings.GetPreventionWarningCtaStateUseCase
import studio.lunabee.onesafe.domain.usecase.support.IncrementVisitForAskingForSupportUseCase
import studio.lunabee.onesafe.domain.usecase.support.IsLanguageGeneratedUseCase
import studio.lunabee.onesafe.domain.usecase.support.ResetLanguageConfigSupportUseCase
import studio.lunabee.onesafe.domain.usecase.support.ShouldAskForSupportUseCase
import studio.lunabee.onesafe.domain.usecase.support.ShouldAskForTranslationSupportUseCase
import studio.lunabee.onesafe.domain.usecase.verifypassword.ShouldVerifyPasswordUseCase
import studio.lunabee.onesafe.feature.home.model.HomeInfoSectionData
import studio.lunabee.onesafe.feature.home.model.ItemRowData
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionDelegateImpl
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionsBottomSheet
import studio.lunabee.onesafe.test.OSTestUtils
import studio.lunabee.onesafe.test.OSUiThreadTest
import studio.lunabee.onesafe.usecase.ImportDiscoveryItemUseCase
import studio.lunabee.onesafe.usecase.ImportPrefillItemUseCase
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertIs

class HomeScreenViewModelFavoritesTest : OSUiThreadTest() {

    private val decryptUseCase: ItemDecryptUseCase = mockk()
    private val getIconUseCase: GetIconUseCase = mockk()
    private val getPreventionWarningUseCase: GetPreventionWarningCtaStateUseCase = mockk {
        every { this@mockk.invoke() } returns MutableStateFlow(null)
    }
    private val isCurrentSafeBiometricEnabledUseCase: IsCurrentSafeBiometricEnabledUseCase = mockk()
    private val shouldVerifyPasswordUseCase: ShouldVerifyPasswordUseCase = mockk {
        coEvery { this@mockk.invoke() } returns LBResult.Success(false)
    }
    private val lockAppUseCase: LockAppUseCase = mockk()
    private val isSafeReadyUseCase: IsSafeReadyUseCase = mockk {
        every { this@mockk.flow() } returns flowOf(true)
    }
    private val shouldAskForSupportUseCase: ShouldAskForSupportUseCase = mockk()
    private val incrementVisitForAskingForSupportUseCase: IncrementVisitForAskingForSupportUseCase = mockk()
    private val importDiscoveryItemUseCase: ImportDiscoveryItemUseCase = mockk()
    private val shouldAskForTranslationSupportUseCase: ShouldAskForTranslationSupportUseCase = mockk()
    private val resetLanguageConfigUseCase: ResetLanguageConfigSupportUseCase = mockk()
    private val osFeatureFlags: FeatureFlags = mockk()
    private val importPrefillItemUseCase: ImportPrefillItemUseCase = mockk()
    private val getAppVisitUseCase: GetAppVisitUseCase = mockk()
    private val isLanguageGeneratedUseCase: IsLanguageGeneratedUseCase = mockk()
    private val context: Context = mockk()
    private val getSafeItemActionHelper: GetSafeItemActionHelper = mockk {
        coEvery { getQuickActions(any(), any()) } returns { listOf() }
    }
    private val homeInfoSectionDelegate: HomeInfoSectionDelegateImpl = mockk {
        every { homeInfoSectionData } returns MutableStateFlow(HomeInfoSectionData(emptyList()))
    }
    private val findLastFavoriteUseCase: FindLastFavoriteUseCase = mockk()
    private val itemDisplayOptionDelegateImpl: ItemDisplayOptionDelegateImpl = mockk {
        every { this@mockk.itemDisplayOptionsBottomSheet } returns MutableStateFlow(
            ItemDisplayOptionsBottomSheet(
                onSelectItemOrder = {},
                selectedItemOrder = ItemOrder.Alphabetic,
                onSelectItemLayout = {},
                selectedItemLayout = ItemLayout.Grid,
            ),
        )
    }
    private val homePagedPlainItemDataUseCase: HomePagedPlainItemDataUseCase = mockk {
        every { this@mockk.invoke() } returns flowOf(PagingData.empty())
    }
    private val homeConversationSectionDelegateImpl: HomeConversationSectionDelegateImpl = mockk()

    private val countSafeItemInParentUseCase: CountSafeItemUseCase = mockk()
    private val countAllFavoriteUseCase: CountAllFavoriteUseCase = mockk()

    private val vm: HomeScreenViewModel by lazyFast {
        HomeScreenViewModel(
            decryptUseCase = decryptUseCase,
            getIconUseCase = getIconUseCase,
            isCurrentSafeBiometricEnabledUseCase = isCurrentSafeBiometricEnabledUseCase,
            shouldVerifyPasswordUseCase = shouldVerifyPasswordUseCase,
            importDiscoveryItemUseCase = importDiscoveryItemUseCase,
            osFeatureFlags = osFeatureFlags,
            getAppVisitUseCase = getAppVisitUseCase,
            shouldAskForTranslationSupportUseCase = shouldAskForTranslationSupportUseCase,
            resetLanguageConfigUseCase = resetLanguageConfigUseCase,
            isLanguageGeneratedUseCase = isLanguageGeneratedUseCase,
            context = context,
            getSafeItemActionHelper = getSafeItemActionHelper,
            importPrefillItemUseCase = importPrefillItemUseCase,
            homeInfoSectionDelegate = homeInfoSectionDelegate,
            findLastFavoriteUseCase = findLastFavoriteUseCase,
            itemDisplayOptionDelegate = itemDisplayOptionDelegateImpl,
            pagedPlainItemDataUseCase = homePagedPlainItemDataUseCase,
            isAppBetaVersion = false,
            homeConversationSectionDelegateImpl = homeConversationSectionDelegateImpl,
            countSafeItemInParentUseCase = countSafeItemInParentUseCase,
            countAllFavoriteUseCase = countAllFavoriteUseCase,
            lockAppUseCase = lockAppUseCase,
            isSafeReadyUseCase = isSafeReadyUseCase,
            getPreventionWarningCtaStateUseCase = getPreventionWarningUseCase,
        )
    }

    init {
        mockkObject(LocaleCompat)
        every { LocaleCompat.getMainLocale(any()) } returns Locale.FRENCH.language
        coEvery { shouldAskForSupportUseCase.invoke() } returns false
        coEvery { incrementVisitForAskingForSupportUseCase.invoke() } returns Unit
        every { isLanguageGeneratedUseCase.invoke(any()) } returns false
    }

    @Test
    fun map_favorites_below_max_test(): TestResult = runTest {
        val favoriteItems = OSTestUtils.createSafeItems(
            AppConstants.Ui.HomeDeleted.MaxShowAmount,
            encName = { null },
            encColor = { null },
            iconId = { null },
        )
        every { isCurrentSafeBiometricEnabledUseCase.flow() } returns flowOf(false)
        coEvery { isCurrentSafeBiometricEnabledUseCase.invoke() } returns false
        coEvery { countSafeItemInParentUseCase.notDeleted(null) } returns LBResult.Success(0)
        every { findLastFavoriteUseCase(AppConstants.Ui.HomeFavorite.MaxShowAmount) } returns flowOf(
            favoriteItems.take(AppConstants.Ui.HomeFavorite.MaxShowAmount),
        )
        every { countAllFavoriteUseCase.invoke() } returns flowOf(favoriteItems.size)
        every { countSafeItemInParentUseCase.allDeleted() } returns flowOf(0)
        every { getAppVisitUseCase.hasDoneOnBoardingBubbles() } returns flowOf(true)

        val uiState = vm.uiState.first()
        val itemRowData = uiState.favoriteItems.first()
        assertEquals(favoriteItems.size, itemRowData.size)
        itemRowData.forEach {
            assertIs<ItemRowData.Item>(it)
        }
    }

    @Test
    fun map_favorites_above_max_test(): TestResult = runTest {
        val moreCount = 10
        val itemsSize = AppConstants.Ui.HomeFavorite.MaxShowAmount + moreCount
        val favoriteItems = OSTestUtils.createSafeItems(
            itemsSize,
            encName = { null },
            encColor = { null },
            iconId = { null },
        )
        every { isCurrentSafeBiometricEnabledUseCase.flow() } returns flowOf(false)
        coEvery { isCurrentSafeBiometricEnabledUseCase.invoke() } returns false
        every { findLastFavoriteUseCase(AppConstants.Ui.HomeFavorite.MaxShowAmount) } returns flowOf(
            favoriteItems.take(AppConstants.Ui.HomeFavorite.MaxShowAmount),
        )
        every { countAllFavoriteUseCase.invoke() } returns flowOf(itemsSize)
        coEvery { countSafeItemInParentUseCase.notDeleted(null) } returns LBResult.Success(0)
        every { countSafeItemInParentUseCase.allDeleted() } returns flowOf(0)
        every { getAppVisitUseCase.hasDoneOnBoardingBubbles() } returns flowOf(true)

        val uiState = vm.uiState.first()
        val itemRowData = uiState.favoriteItems.first()
        assertEquals(AppConstants.Ui.HomeFavorite.MaxShowAmount, itemRowData.size)
        itemRowData.forEachIndexed { idx, favorite ->
            if (idx == itemRowData.lastIndex) {
                assertIs<ItemRowData.More>(favorite)
            } else {
                assertIs<ItemRowData.Item>(favorite)
            }
        }
    }
}
