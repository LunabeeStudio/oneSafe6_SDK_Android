package studio.lunabee.onesafe.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.beta.AppBetaVersionChip
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionDelegate
import studio.lunabee.onesafe.feature.settings.personalization.ItemDisplayOptionsBottomSheet
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun HomeTopActions(
    navigateToSettings: () -> Unit,
    navigateToBubbles: () -> Unit,
    isBubblesShown: Boolean,
    itemDisplayOptionDelegate: ItemDisplayOptionDelegate,
    showBeta: Boolean,
    hasPreventionWarning: Boolean,
) {
    val itemDisplayOptionsBottomSheet by itemDisplayOptionDelegate.itemDisplayOptionsBottomSheet.collectAsStateWithLifecycle()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBeta) {
            AppBetaVersionChip()
        }
        Spacer(modifier = Modifier.weight(1f))
        if (isBubblesShown) {
            OSIconButton(
                image = OSImageSpec.Drawable(OSDrawable.ic_people),
                onClick = navigateToBubbles,
                buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                contentDescription = LbcTextSpec.StringResource(id = OSString.accessibility_home_contacts_button_clickLabel),
                colors = OSIconButtonDefaults.secondaryIconButtonColors(),
            )
        }

        var isItemDisplayOptionsBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
        itemDisplayOptionsBottomSheet.Composable(
            isVisible = isItemDisplayOptionsBottomSheetVisible,
            onBottomSheetClosed = { isItemDisplayOptionsBottomSheetVisible = false },
        )
        OSIconButton(
            image = OSImageSpec.Drawable(OSDrawable.ic_menu),
            onClick = { isItemDisplayOptionsBottomSheetVisible = true },
            buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
            contentDescription = LbcTextSpec.StringResource(id = OSString.home_displayOptions_sorting_contentDescription),
            colors = OSIconButtonDefaults.secondaryIconButtonColors(),
        )

        val (contentColor, containerColor) = LocalDesignSystem.current.feedbackWarningBackgroundGradient()
        OSIconButton(
            image = OSImageSpec.Drawable(OSDrawable.ic_settings),
            onClick = navigateToSettings,
            buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
            contentDescription = LbcTextSpec.StringResource(id = OSString.accessibility_home_settings_button_clickLabel),
            colors = if (hasPreventionWarning) {
                ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = contentColor)
            } else {
                OSIconButtonDefaults.secondaryIconButtonColors()
            },
            modifier = Modifier
                .then(
                    if (hasPreventionWarning) {
                        Modifier
                            .clip(CircleShape)
                            .background(brush = containerColor)
                    } else {
                        Modifier
                    },
                ),
        )
    }
}

@OsDefaultPreview
@Composable
private fun HomeTopActionsPreview() {
    OSPreviewOnSurfaceTheme {
        HomeTopActions(
            navigateToSettings = {},
            navigateToBubbles = {},
            isBubblesShown = true,
            itemDisplayOptionDelegate = object : ItemDisplayOptionDelegate {
                override val itemDisplayOptionsBottomSheet: StateFlow<ItemDisplayOptionsBottomSheet> = MutableStateFlow(
                    ItemDisplayOptionsBottomSheet(
                        onSelectItemOrder = {},
                        selectedItemOrder = ItemOrder.Alphabetic,
                        onSelectItemLayout = {},
                        selectedItemLayout = ItemLayout.Grid,
                    ),
                )
            },
            showBeta = true,
            hasPreventionWarning = false,
        )
    }
}

@OsDefaultPreview
@Composable
private fun HomeTopActionsWarningPreview() {
    OSPreviewOnSurfaceTheme {
        HomeTopActions(
            navigateToSettings = {},
            navigateToBubbles = {},
            isBubblesShown = true,
            itemDisplayOptionDelegate = object : ItemDisplayOptionDelegate {
                override val itemDisplayOptionsBottomSheet: StateFlow<ItemDisplayOptionsBottomSheet> = MutableStateFlow(
                    ItemDisplayOptionsBottomSheet(
                        onSelectItemOrder = {},
                        selectedItemOrder = ItemOrder.Alphabetic,
                        onSelectItemLayout = {},
                        selectedItemLayout = ItemLayout.Grid,
                    ),
                )
            },
            showBeta = true,
            hasPreventionWarning = true,
        )
    }
}
