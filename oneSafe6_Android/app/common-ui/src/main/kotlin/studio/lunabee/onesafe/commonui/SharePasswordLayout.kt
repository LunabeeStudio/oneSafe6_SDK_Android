package studio.lunabee.onesafe.commonui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.extension.getTextSharingIntent
import studio.lunabee.onesafe.model.OSGeneratedPasswordOption
import studio.lunabee.onesafe.molecule.OSGeneratedPassword

@Composable
fun SharePasswordLayout(
    password: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    var isPasswordVisible: Boolean by rememberSaveable { mutableStateOf(false) }
    val eyeIconRes = if (isPasswordVisible) {
        R.drawable.ic_visibility_off
    } else {
        R.drawable.ic_visibility_on
    }

    val context = LocalContext.current

    OSGeneratedPassword(
        password = password,
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        options = listOf(
            OSGeneratedPasswordOption.Primary(
                icon = OSImageSpec.Drawable(eyeIconRes),
                onClick = {
                    isPasswordVisible = !isPasswordVisible
                },
                contentDescription = LbcTextSpec
                    .StringResource(R.string.safeItemDetail_contentCard_informations_accessibility_securedValue_actionShow),
            ),
            OSGeneratedPasswordOption.Primary(
                icon = OSImageSpec.Drawable(R.drawable.ic_share),
                onClick = {
                    context.startActivity(context.getTextSharingIntent(password))
                },
                contentDescription = LbcTextSpec.StringResource(R.string.share_accessibility_sharePasswordContentDescription),
            ),
        ),
        modifier = modifier,
        onClick = onClick,
    )
}
