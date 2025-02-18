package studio.lunabee.compose.accessibility

/**
 * Override cleanForAccessibility in tests
 */
@Suppress("unused")
@Deprecated(message = "should not be used", level = DeprecationLevel.ERROR)
object LbcAccessibilityUtils {
    fun String.cleanForAccessibility(): String = this
}
