package studio.lunabee.onesafe.feature.home

import android.content.Context
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.common.CtaState
import studio.lunabee.onesafe.domain.repository.SupportOSRepository
import studio.lunabee.onesafe.domain.usecase.support.HasDismissSupportUseCase
import studio.lunabee.onesafe.domain.usecase.support.HasRatedOSUseCase
import studio.lunabee.onesafe.domain.usecase.support.IncrementVisitForAskingForSupportUseCase
import studio.lunabee.onesafe.feature.home.model.HomeInfoSectionData
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesHomeCtaInfoData
import studio.lunabee.onesafe.feature.supportus.SupportUsHomeInfoData
import studio.lunabee.onesafe.importexport.ui.AutoBackupEnableCtaHomeInfoData
import studio.lunabee.onesafe.importexport.ui.AutoBackupErrorHomeInfoData
import studio.lunabee.onesafe.importexport.usecase.ClearAutoBackupErrorUseCase
import studio.lunabee.onesafe.importexport.usecase.DismissEnableAutoBackupCtaUseCase
import studio.lunabee.onesafe.importexport.usecase.GetAutoBackupErrorUseCase
import studio.lunabee.onesafe.importexport.usecase.GetEnableAutoBackupCtaStateUseCase
import studio.lunabee.onesafe.usecase.DismissBubblesHomeCardUseCase
import studio.lunabee.onesafe.usecase.GetEnableBubblesCtaStateUseCase
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

interface HomeInfoSectionDelegate {
    val homeInfoSectionData: StateFlow<HomeInfoSectionData>
}

@ViewModelScoped
class HomeInfoSectionDelegateImpl @Inject constructor(
    supportOSRepository: SupportOSRepository,
    getAutoBackupErrorUseCase: GetAutoBackupErrorUseCase,
    private val incrementVisitForAskingForSupportUseCase: IncrementVisitForAskingForSupportUseCase,
    private val hasDismissSupportUseCase: HasDismissSupportUseCase,
    private val hasRatedOSUseCase: HasRatedOSUseCase,
    getEnableAutoBackupCtaStateUseCase: GetEnableAutoBackupCtaStateUseCase,
    private val dismissAutoBackupCtaUseCase: DismissEnableAutoBackupCtaUseCase,
    private val clock: Clock,
    private val clearAutoBackupErrorUseCase: ClearAutoBackupErrorUseCase,
    getEnableBubblesCtaStateUseCase: GetEnableBubblesCtaStateUseCase,
    private val dismissBubblesHomeCardUseCase: DismissBubblesHomeCardUseCase,
    reviewAppViewModelImpl: ReviewAppDelegateImpl,
) : HomeInfoSectionDelegate,
    CloseableCoroutineScope by CloseableMainCoroutineScope(),
    ReviewAppDelegate by reviewAppViewModelImpl {

    override val homeInfoSectionData: StateFlow<HomeInfoSectionData> = combine(
        supportOSRepository.visibleSince,
        getAutoBackupErrorUseCase(),
        getEnableAutoBackupCtaStateUseCase(),
        getEnableBubblesCtaStateUseCase(),
    ) { supportOSVisibleSince, autoBackupError, enableAutoBackupCtaState, enableBubblesCtaState ->
        val data = buildList {
            if (autoBackupError != null) {
                val errorHomeInfoData = AutoBackupErrorHomeInfoData(
                    errorLabel = LbcTextSpec.Raw(autoBackupError.code),
                    errorFull = LbcTextSpec.Raw(autoBackupError.toString()),
                    visibleSince = autoBackupError.date.toInstant(),
                ) {
                    coroutineScope.launch {
                        clearAutoBackupErrorUseCase.force()
                    }
                }
                add(errorHomeInfoData)
            }
            if (supportOSVisibleSince != null) {
                val supportUsHomeInfoData = SupportUsHomeInfoData(
                    visibleSince = supportOSVisibleSince,
                    onDismiss = {
                        coroutineScope.launch {
                            hasDismissSupportUseCase()
                        }
                    },
                    onClickOnSupportUs = { context: Context ->
                        coroutineScope.launch {
                            showFeedbackDialog(context)
                            hasRatedOSUseCase()
                        }
                    },
                )
                add(supportUsHomeInfoData)
            }
            if (enableAutoBackupCtaState is CtaState.VisibleSince && enableAutoBackupCtaState.timestamp <= Instant.now(clock)) {
                val autoBackupEnableCtaHomeInfoData = AutoBackupEnableCtaHomeInfoData(
                    visibleSince = enableAutoBackupCtaState.timestamp,
                    onDismiss = { coroutineScope.launch { dismissAutoBackupCtaUseCase.invoke() } },
                )
                add(autoBackupEnableCtaHomeInfoData)
            }
            if (enableBubblesCtaState is CtaState.VisibleSince) {
                BubblesHomeCtaInfoData(
                    visibleSince = enableBubblesCtaState.timestamp,
                    onDismiss = {
                        coroutineScope.launch {
                            dismissBubblesHomeCardUseCase()
                        }
                    },
                ).also(::add)
            }
        }.sorted()
        HomeInfoSectionData(data)
    }.stateIn(
        coroutineScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        HomeInfoSectionData(emptyList()),
    )

    init {
        incrementAppVisitForAskingForSupport()
    }

    private fun incrementAppVisitForAskingForSupport() {
        coroutineScope.launch {
            incrementVisitForAskingForSupportUseCase()
        }
    }
}
