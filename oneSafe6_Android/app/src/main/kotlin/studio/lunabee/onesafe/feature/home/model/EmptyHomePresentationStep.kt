package studio.lunabee.onesafe.feature.home.model

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSLinearProgress
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

sealed interface EmptyHomePresentationStep {
    @Composable
    fun Card(modifier: Modifier)

    class Import(
        private val onClick: () -> Unit,
    ) : EmptyHomePresentationStep {
        @Composable
        override fun Card(modifier: Modifier) {
            val context = LocalContext.current
            OSMessageCard(
                modifier = modifier
                    .clickable(onClickLabel = context.getString(OSString.home_section_welcome_emptyTab_import_button), onClick = onClick),
                title = LbcTextSpec.StringResource(OSString.home_section_welcome_emptyTab_import_title),
                description = LbcTextSpec.StringResource(OSString.home_section_welcome_emptyTab_import_message),
                action = {
                    OSFilledButton(
                        text = LbcTextSpec.StringResource(OSString.home_section_welcome_emptyTab_import_button),
                        onClick = onClick,
                        modifier = Modifier
                            .clearAndSetSemantics { }
                            .padding(vertical = OSDimens.SystemSpacing.Regular),
                    )
                },
            )
        }
    }

    class Migrate(
        private val onClick: () -> Unit,
    ) : EmptyHomePresentationStep {
        @Composable
        override fun Card(modifier: Modifier) {
            val context = LocalContext.current
            OSMessageCard(
                modifier = modifier
                    .clickable(onClickLabel = context.getString(OSString.home_section_welcome_emptyTab_migrate_button), onClick = onClick),
                title = LbcTextSpec.StringResource(OSString.home_section_welcome_emptyTab_migrate_title),
                description = LbcTextSpec.StringResource(OSString.home_section_welcome_emptyTab_migrate_message),
                action = {
                    OSFilledButton(
                        text = LbcTextSpec.StringResource(OSString.home_section_welcome_emptyTab_migrate_button),
                        onClick = onClick,
                        modifier = Modifier
                            .clearAndSetSemantics { }
                            .padding(vertical = OSDimens.SystemSpacing.Regular),
                    )
                },
            )
        }
    }

    class Discover(
        private val onClick: () -> Unit,
        private val isLoading: Boolean,
    ) : EmptyHomePresentationStep {
        @Composable
        override fun Card(modifier: Modifier) {
            val context = LocalContext.current
            OSMessageCard(
                modifier = modifier
                    .testTag(UiConstants.TestTag.Item.DiscoveryItemCard)
                    .clickable(
                        enabled = !isLoading,
                        onClickLabel = context.getString(OSString.home_tutorialDialog_discover_button),
                        onClick = onClick,
                    ),
                title = LbcTextSpec.StringResource(OSString.home_section_welcome_emptyTab_discover_title),
                description = LbcTextSpec.StringResource(OSString.home_section_welcome_emptyTab_welcomeAndDiscover_message),
                action = { paddingValues ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(vertical = OSDimens.SystemSpacing.Regular)
                            .clearAndSetSemantics {},
                    ) {
                        OSFilledButton(
                            text = LbcTextSpec.StringResource(OSString.home_tutorialDialog_discover_button),
                            onClick = onClick,
                            state = if (isLoading) {
                                OSActionState.Disabled
                            } else {
                                OSActionState.Enabled
                            },
                        )
                        if (isLoading) {
                            OSLinearProgress(
                                progress = null,
                            )
                        }
                    }
                },
            )
        }
    }
}
