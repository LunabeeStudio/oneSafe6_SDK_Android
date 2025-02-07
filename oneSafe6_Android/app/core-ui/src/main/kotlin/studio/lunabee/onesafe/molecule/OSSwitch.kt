package studio.lunabee.onesafe.molecule

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSMediumSpacer
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSSwitchState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.ui.theme.OSTypography
import studio.lunabee.onesafe.ui.theme.OSTypography.labelSmallRegular

/**
 * Switch - format Row
 *
 * @see <a href="https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?type=design&node-id=3696-52823">
 *     Figma</a>
 */
@Composable
fun OSSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    description: LbcTextSpec? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    accessibilityLabel: LbcTextSpec? = null,
) {
    OSSwitchRow(
        OSSwitchState.fromChecked(checked),
        { onCheckedChange(it.checked) },
        label,
        modifier,
        description,
        leadingIcon,
        enabled,
        accessibilityLabel,
    )
}

/**
 * Switch - format Row
 *
 * @see <a href="https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?type=design&node-id=3696-52823">
 *     Figma</a>
 */
@Composable
fun OSSwitchRow(
    state: OSSwitchState,
    onStateChange: (OSSwitchState) -> Unit,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    description: LbcTextSpec? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    accessibilityLabel: LbcTextSpec? = null,
) {
    OSSwitch(
        state = state,
        enabled = enabled,
        content = {
            leadingIcon?.let {
                leadingIcon()
                OSMediumSpacer()
            }
            Column(
                modifier = Modifier
                    .weight(1.0f, true)
                    .composed {
                        val text = accessibilityLabel?.string.orEmpty()
                        semantics { this.text = AnnotatedString(text) }
                    },
            ) {
                OSText(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                description?.let {
                    OSText(
                        text = description,
                        style = OSTypography.Typography.labelSmallRegular,
                        color = LocalColorPalette.current.Neutral60,
                    )
                }
            }
        },
        modifier = Modifier
            .testTag(label.string)
            .toggleableSwitch(
                checked = state.checked,
                onCheckedChange = { onStateChange(OSSwitchState.fromChecked(it)) },
                enabled = enabled,
            )
            .then(modifier),
    )
}

/**
 * Switch - format Option
 *
 * @see <a href="https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?type=design&node-id=3540-47669">
 *     Figma</a>
 */
@Composable
fun OSSwitchOption(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: LbcTextSpec? = null,
) {
    OSSwitchOption(
        OSSwitchState.fromChecked(checked),
        { onCheckedChange(it.checked) },
        label,
        modifier,
        enabled,
        accessibilityLabel,
    )
}

/**
 * Switch - format Option
 *
 * @see <a href="https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?type=design&node-id=3540-47669">
 *     Figma</a>
 */
@Composable
fun OSSwitchOption(
    state: OSSwitchState,
    onStateChange: (OSSwitchState) -> Unit,
    label: LbcTextSpec,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: LbcTextSpec? = null,
) {
    OSSwitch(
        state = state,
        enabled = enabled,
        content = {
            OSText(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1.0f, false)
                    .composed {
                        val text = accessibilityLabel?.string.orEmpty()
                        semantics { this.text = AnnotatedString(text) }
                    },
            )
        },
        modifier = Modifier
            .testTag(label.string)
            .clip(shape = MaterialTheme.shapes.medium)
            .toggleableSwitch(
                checked = state.checked,
                onCheckedChange = { onStateChange(OSSwitchState.fromChecked(it)) },
                enabled = state != OSSwitchState.Loading,
            )
            .then(modifier),
    )
}

@Composable
fun OSSwitch(
    checked: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit),
) {
    OSSwitch(
        state = OSSwitchState.fromChecked(checked),
        enabled = enabled,
        modifier = modifier,
        content = content,
    )
}

@Composable
fun OSSwitch(
    state: OSSwitchState,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (RowScope.() -> Unit),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .minimumInteractiveComponentSize(),
    ) {
        content()
        OSRegularSpacer()

        var boxWidth: Int by remember { mutableIntStateOf(0) }

        AnimatedContent(
            targetState = state,
            label = AnimatedOSSwitch,
            contentKey = {
                when (it) {
                    OSSwitchState.True,
                    OSSwitchState.False,
                    -> ContentKeySwitch
                    OSSwitchState.Loading -> ContentKeyLoading
                }
            },
        ) { targetState ->
            when (targetState) {
                OSSwitchState.True,
                OSSwitchState.False,
                -> {
                    Switch(
                        checked = targetState == OSSwitchState.True,
                        onCheckedChange = null,
                        enabled = enabled,
                        modifier = Modifier
                            .testTag(UiConstants.TestTag.Item.ToggleSwitch)
                            .onSizeChanged {
                                boxWidth = it.width
                            },
                    )
                }
                OSSwitchState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .widthIn(min = with(LocalDensity.current) { boxWidth.toDp() }),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .testTag(UiConstants.TestTag.Item.LoadingSwitch),
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.toggleableSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean): Modifier {
    return this.toggleable(
        value = checked,
        onValueChange = onCheckedChange,
        role = Role.Switch,
        enabled = enabled,
    )
}

private const val AnimatedOSSwitch = "AnimatedOSSwitch"
private const val ContentKeySwitch = "ContentKeySwitch"
private const val ContentKeyLoading = "ContentKeyLoading"

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OSSwitchPreview() {
    OSPreviewOnSurfaceTheme {
        Column {
            OSSwitchOption(
                checked = true,
                onCheckedChange = {},
                label = LbcTextSpec.Raw("OSSwitchOption"),
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Small),
            )
            OSSwitchRow(
                checked = false,
                onCheckedChange = {},
                label = loremIpsumSpec(10),
                description = LbcTextSpec.Raw("OSSwitchRow - description"),
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.os_ic_sample), contentDescription = null)
                },
                modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
            )
        }
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OSSwitchLoadingPreview() {
    OSPreviewOnSurfaceTheme {
        var osCheckFalseTrue by remember {
            mutableStateOf(OSSwitchState.False)
        }
        var osCheckFalseLoadingFalse by remember {
            mutableStateOf(OSSwitchState.False)
        }
        var osCheckFalseLoadingTrue by remember {
            mutableStateOf(OSSwitchState.Loading)
        }
        Column {
            OSSwitchRow(state = osCheckFalseTrue, label = LbcTextSpec.Raw("False / True"), onStateChange = {
                osCheckFalseTrue = OSSwitchState.fromChecked(!osCheckFalseTrue.checked)
            })
            OSSwitchRow(state = osCheckFalseLoadingFalse, label = LbcTextSpec.Raw("False / Loading / False"), onStateChange = {
                osCheckFalseLoadingFalse = if (osCheckFalseLoadingFalse == OSSwitchState.False) {
                    OSSwitchState.Loading
                } else {
                    OSSwitchState.False
                }
            })
            OSSwitchRow(state = osCheckFalseLoadingTrue, label = LbcTextSpec.Raw("Loading / True / False"), onStateChange = {
                osCheckFalseLoadingTrue = when (osCheckFalseLoadingTrue) {
                    OSSwitchState.Loading -> OSSwitchState.True
                    OSSwitchState.True -> OSSwitchState.False
                    OSSwitchState.False -> OSSwitchState.Loading
                }
            })
        }
    }
}
