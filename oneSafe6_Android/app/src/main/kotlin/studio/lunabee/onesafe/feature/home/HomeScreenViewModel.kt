package studio.lunabee.onesafe.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault.Companion.mapForUi
import studio.lunabee.onesafe.common.utils.LocaleCompat
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.snackbar.MessageSnackBarState
import studio.lunabee.onesafe.commonui.snackbar.SnackbarState
import studio.lunabee.onesafe.domain.common.FeatureFlags
import studio.lunabee.onesafe.domain.model.safeitem.DiscoverPrefillData
import studio.lunabee.onesafe.domain.model.safeitem.DiscoveryData
import studio.lunabee.onesafe.domain.qualifier.StoreBetaTrack
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
import studio.lunabee.onesafe.domain.usecase.support.IsLanguageGeneratedUseCase
import studio.lunabee.onesafe.domain.usecase.support.ResetLanguageConfigSupportUseCase
import studio.lunabee.onesafe.domain.usecase.support.ShouldAskForTranslationSupportUseCase
import studio.lunabee.onesafe.domain.usecase.verifypassword.ShouldVerifyPasswordUseCase
import studio.lunabee.onesafe.feature.home.model.ItemRowData
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionUiStateDelegate
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionDelegate
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionDelegateImpl
import studio.lunabee.onesafe.usecase.ImportDiscoveryItemUseCase
import studio.lunabee.onesafe.usecase.ImportPrefillItemUseCase
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    countSafeItemInParentUseCase: CountSafeItemUseCase,
    decryptUseCase: ItemDecryptUseCase,
    getIconUseCase: GetIconUseCase,
    isCurrentSafeBiometricEnabledUseCase: IsCurrentSafeBiometricEnabledUseCase,
    private val shouldVerifyPasswordUseCase: ShouldVerifyPasswordUseCase,
    private val importDiscoveryItemUseCase: ImportDiscoveryItemUseCase,
    private val importPrefillItemUseCase: ImportPrefillItemUseCase,
    val osFeatureFlags: FeatureFlags,
    getAppVisitUseCase: GetAppVisitUseCase,
    private val shouldAskForTranslationSupportUseCase: ShouldAskForTranslationSupportUseCase,
    private val resetLanguageConfigUseCase: ResetLanguageConfigSupportUseCase,
    private val isLanguageGeneratedUseCase: IsLanguageGeneratedUseCase,
    private val getSafeItemActionHelper: GetSafeItemActionHelper,
    private val homeInfoSectionDelegate: HomeInfoSectionDelegateImpl,
    findLastFavoriteUseCase: FindLastFavoriteUseCase,
    private val itemDisplayOptionDelegate: ItemDisplayOptionDelegateImpl,
    private val lockAppUseCase: LockAppUseCase,
    pagedPlainItemDataUseCase: HomePagedPlainItemDataUseCase,
    @StoreBetaTrack val isAppBetaVersion: Boolean,
    homeConversationSectionDelegateImpl: HomeConversationSectionDelegateImpl,
    countAllFavoriteUseCase: CountAllFavoriteUseCase,
    isSafeReadyUseCase: IsSafeReadyUseCase,
    getPreventionWarningCtaStateUseCase: GetPreventionWarningCtaStateUseCase,
) : ViewModel(),
    GetSafeItemActionUiStateDelegate by getSafeItemActionHelper,
    HomeInfoSectionDelegate by homeInfoSectionDelegate,
    ItemDisplayOptionDelegate by itemDisplayOptionDelegate,
    HomeConversationSectionDelegate by homeConversationSectionDelegateImpl {

    private val json = Json { ignoreUnknownKeys = true }

    val uiState: StateFlow<HomeScreenUiState>

    private val _areItemsBeingGenerated: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val areItemsBeingGenerated: StateFlow<Boolean> = _areItemsBeingGenerated.asStateFlow()

    private val _shouldAskForTranslationSupport: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val shouldAskForTranslationSupport: StateFlow<Boolean> = _shouldAskForTranslationSupport.asStateFlow()

    val hasPreventionWarnings: StateFlow<Boolean> = getPreventionWarningCtaStateUseCase()
        .map { it != null }
        .stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            false,
        )

    private val _snackbarState = MutableStateFlow<SnackbarState?>(null)
    val snackbarState: StateFlow<SnackbarState?> = _snackbarState.asStateFlow()

    private var favoriteCount = 0

    init {
        val rootItemsFlow: Flow<PagingData<PlainItemData>> = pagedPlainItemDataUseCase()
            .cachedIn(viewModelScope)

        val favoriteItemRowsFlow: Flow<ImmutableList<ItemRowData>> = findLastFavoriteUseCase(
            limit = AppConstants.Ui.HomeFavorite.MaxShowAmount,
        )
            .mapForUi(
                decryptUseCase = decryptUseCase,
                getIconUseCase = getIconUseCase,
                getQuickAction = getSafeItemActionHelper::getQuickActions,
            )
            .map { plainItemDataList ->
                getImmutableRowData(plainItemDataList.toImmutableList()) // TODO verify immutable use
            }

        uiState = combine(
            countAllFavoriteUseCase(),
            countSafeItemInParentUseCase.allDeleted(),
            flow { emit(countSafeItemInParentUseCase.notDeleted(null).data ?: 0) },
            isCurrentSafeBiometricEnabledUseCase.flow(), // Handle biometric errors during login
            flow { emit(shouldVerifyPasswordUseCase().data ?: false) },
            getAppVisitUseCase.hasDoneOnBoardingBubbles(),
            isSafeReadyUseCase.flow(),
        ) {
            val favoriteCount = it[0] as Int
            val deletedCount = it[1] as Int
            val initialItemCount = it[2] as Int
            val isBiometricEnabled = it[3] as Boolean
            val shouldVerifyPassword = it[4] as Boolean
            val hasDoneOnBoardingBubbles = it[5] as Boolean
            val isSafeReady = it[6] as Boolean

            this.favoriteCount = favoriteCount

            HomeScreenUiState(
                items = rootItemsFlow,
                initialItemCount = initialItemCount,
                favoriteItems = favoriteItemRowsFlow,
                deletedItemCount = deletedCount,
                isBiometricEnabled = isBiometricEnabled,
                isLanguageGenerated = isLanguageGeneratedUseCase(LocaleCompat.getMainLocale(context)),
                showFavoritesSeeAll = favoriteCount > 0,
                isAppBetaVersion = isAppBetaVersion,
                shouldVerifyPassword = shouldVerifyPassword,
                hasDoneOnBoardingBubbles = hasDoneOnBoardingBubbles,
                isSafeReady = isSafeReady,
            )
        }.stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            HomeScreenUiState.initializing(),
        )
    }

    private fun getImmutableRowData(
        items: List<PlainItemData>,
    ): ImmutableList<ItemRowData> {
        val itemRowDataList = mutableListOf<ItemRowData>()
        if (favoriteCount > AppConstants.Ui.HomeFavorite.MaxShowAmount) {
            items.take(AppConstants.Ui.HomeFavorite.MaxShowAmount - 1).mapTo(itemRowDataList) { ItemRowData.Item(it) }
            itemRowDataList.add(ItemRowData.More(favoriteCount - itemRowDataList.size))
        } else {
            items.mapTo(itemRowDataList) { ItemRowData.Item(it) }
        }
        return itemRowDataList.toImmutableList()
    }

    fun importDiscoveryData(
        importExampleItems: Boolean,
        importPrefillItem: Boolean,
        locale: String,
    ) {
        viewModelScope.launch {
            _areItemsBeingGenerated.value = true
            if (importExampleItems) importDiscoveryItems(locale)
            if (importPrefillItem) importPrefillItems(locale)
            _areItemsBeingGenerated.value = false
        }
    }

    private suspend fun importDiscoveryItems(locale: String) {
        val discoverData: DiscoveryData = json.decodeFromString(
            javaClass.classLoader!!
                .getResourceAsStream("discoverItems.json")!!
                .use { inputStream -> inputStream.bufferedReader().readText() },
        )
        val result = importDiscoveryItemUseCase(
            data = discoverData,
            locale = locale,
        )

        if (result is LBResult.Failure) {
            _snackbarState.value = MessageSnackBarState(result.throwable.description())
        }
    }

    private suspend fun importPrefillItems(locale: String) {
        val discoverData: DiscoverPrefillData = json.decodeFromString(
            javaClass.classLoader!!
                .getResourceAsStream("discoverPrefill.json")!!
                .use { inputStream -> inputStream.bufferedReader().readText() },
        )
        val result = importPrefillItemUseCase(
            data = discoverData,
            locale = locale,
        )

        if (result is LBResult.Failure) {
            _snackbarState.value = MessageSnackBarState(result.throwable.description())
        }
    }

    fun shouldAskForTranslationSupport(currentLocale: String) {
        viewModelScope.launch {
            shouldAskForTranslationSupportUseCase(currentLocale).collect {
                _shouldAskForTranslationSupport.value = it
            }
        }
    }

    fun blockTranslationCount() {
        viewModelScope.launch {
            resetLanguageConfigUseCase()
        }
    }

    fun lockSafe() {
        viewModelScope.launch {
            lockAppUseCase(true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        getSafeItemActionHelper.close()
        homeInfoSectionDelegate.close()
        itemDisplayOptionDelegate.close()
    }
}
