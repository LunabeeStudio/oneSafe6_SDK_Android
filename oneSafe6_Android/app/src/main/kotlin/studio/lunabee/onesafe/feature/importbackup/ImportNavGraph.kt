package studio.lunabee.onesafe.feature.importbackup

import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import studio.lunabee.onesafe.feature.importbackup.auth.ImportAuthDestination
import studio.lunabee.onesafe.feature.importbackup.auth.ImportAuthRoute
import studio.lunabee.onesafe.feature.importbackup.bubbleswarning.ImportBubblesWarningDestination
import studio.lunabee.onesafe.feature.importbackup.bubbleswarning.ImportBubblesWarningNavScope
import studio.lunabee.onesafe.feature.importbackup.bubbleswarning.ImportBubblesWarningRoute
import studio.lunabee.onesafe.feature.importbackup.nofullysupported.NotFullySupportedArchiveDestination
import studio.lunabee.onesafe.feature.importbackup.nofullysupported.NotFullySupportedArchiveRoute
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataDestination
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataRoute
import studio.lunabee.onesafe.feature.importbackup.selectdata.ImportSelectDataDestination
import studio.lunabee.onesafe.feature.importbackup.selectdata.ImportSelectDataNavScope
import studio.lunabee.onesafe.feature.importbackup.selectdata.ImportSelectDataRoute
import studio.lunabee.onesafe.feature.importbackup.selectfile.importFileScreen

@SuppressWarnings("LongParameterList")
fun NavGraphBuilder.importGraph(
    navigateBack: () -> Unit,
    navigateToImportAuthDestination: () -> Unit,
    navigateToImportSettingsDestination: () -> Unit,
    navigateToSelectImportDataDestination: () -> Unit,
    navigateBackToSettings: () -> Unit,
    navigateBackToFileSelection: () -> Unit,
    navigateToWarningNotFullySupportedArchive: () -> Unit,
    navigateToWarningBubbles: () -> Unit,
    showSnackBar: (visuals: SnackbarVisuals) -> Unit,
) {
    importFileScreen(
        navigateBack = navigateBack,
        navigateToImportAuthDestination = navigateToImportAuthDestination,
        navigateToWarningNotFullySupportedArchive = navigateToWarningNotFullySupportedArchive,
        showSnackBar = showSnackBar,
    )

    composable(
        route = ImportAuthDestination.route,
    ) {
        ImportAuthRoute(
            navigateBack = navigateBackToSettings,
            onSuccess = { hasBubblesData ->
                if (hasBubblesData) {
                    navigateToSelectImportDataDestination()
                } else {
                    navigateToImportSettingsDestination()
                }
            },
        )
    }

    composable(
        route = ImportSaveDataDestination.route,
    ) {
        ImportSaveDataRoute(
            navigateBackToSettings = navigateBackToSettings,
            showSnackBar = showSnackBar,
            navigateBackToFileSelection = navigateBackToFileSelection,
        )
    }

    composable(
        route = NotFullySupportedArchiveDestination.route,
    ) {
        NotFullySupportedArchiveRoute(
            navigateBack = navigateBackToSettings,
            navigateToImportAuthDestination = navigateToImportAuthDestination,
        )
    }

    composable(
        route = ImportSelectDataDestination.route,
    ) {
        val navScope = object : ImportSelectDataNavScope {
            override val navigateBack: () -> Unit = navigateBack
            override val continueImport: (Boolean) -> Unit = { hasBubblesImport ->
                if (hasBubblesImport) {
                    navigateToWarningBubbles()
                } else {
                    navigateToImportSettingsDestination()
                }
            }
        }
        ImportSelectDataRoute(navScope)
    }

    composable(
        route = ImportBubblesWarningDestination.route,
    ) {
        val navScope = object : ImportBubblesWarningNavScope {
            override val navigateBack: () -> Unit = navigateBack
            override val navigateToSaveData: () -> Unit = navigateToImportSettingsDestination
            override val showSnackBar: (SnackbarVisuals) -> Unit = showSnackBar
            override val navigateBackToSettings: () -> Unit = navigateBackToSettings
            override val navigateBackToFileSelection: () -> Unit = navigateBackToFileSelection
        }
        ImportBubblesWarningRoute(navScope)
    }
}
