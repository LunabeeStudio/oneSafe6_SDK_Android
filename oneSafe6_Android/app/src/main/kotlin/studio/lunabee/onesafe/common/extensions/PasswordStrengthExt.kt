package studio.lunabee.onesafe.common.extensions

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.password.PasswordStrength

fun PasswordStrength.label(): LbcTextSpec? = when (this) {
    PasswordStrength.VeryWeak -> LbcTextSpec.StringResource(OSString.passwordStrength_veryWeak)
    PasswordStrength.Weak -> LbcTextSpec.StringResource(OSString.passwordStrength_weak)
    PasswordStrength.Good -> LbcTextSpec.StringResource(OSString.passwordStrength_good)
    PasswordStrength.Strong -> LbcTextSpec.StringResource(OSString.passwordStrength_strong)
    PasswordStrength.VeryStrong -> LbcTextSpec.StringResource(OSString.passwordStrength_veryStrong)
    PasswordStrength.BulletProof -> LbcTextSpec.StringResource(OSString.passwordStrength_bulletProof)
    else -> null
}
