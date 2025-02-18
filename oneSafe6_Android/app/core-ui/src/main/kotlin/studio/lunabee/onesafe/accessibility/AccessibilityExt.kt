package studio.lunabee.onesafe.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import studio.lunabee.compose.accessibility.state.AccessibilityState
import studio.lunabee.compose.accessibility.state.rememberAccessibilityState

/**
 * Wrapper to avoid boilerplate code by returning `true` on every click.
 * @param label The description of this action. By letting null, Talkback will say "Double tap to activate".
 * @param action The function to invoke when this action is performed.
 */
fun SemanticsPropertyReceiver.accessibilityClick(
    label: String? = null,
    action: () -> Unit,
) {
    onClick(
        label = label,
        action = {
            action()
            // From Google's documentation:
            // The function should return a boolean result indicating whether the action is successfully handled. For example,
            // a scroll forward action should return false if the widget is not enabled or has reached the end of the list.
            // All our common use case should return true. Use your own `onClick` directly if you need to change the behavior.
            true
        },
    )
}

/**
 * Set a node as invisible for Talkback. As [clearAndSetSemantics] erase all semantics, put back default text and test tag if needed.
 * You can also use [androidx.compose.ui.semantics.invisibleToUser].
 * Check documentation for specific usage, our common use case should use our custom method).
 *
 * If you do not set text or testTag after clear, tests can fail (i.e `onNodeWithText` or `onNodeWithTag` will fail).
 * If [text] is set, element will not be focusable by Talkback but will be read if used with `mergeDescendants` set to `true`.
 */
fun Modifier.accessibilityClearForInvisibilityToUser(
    text: String? = null,
    testTag: String? = null,
): Modifier {
    return clearAndSetSemantics {
        text?.let { this.text = AnnotatedString(text = text) }
        testTag?.let { this.testTag = testTag }
    }
}

/**
 * [Modifier] to use [androidx.compose.ui.semantics.invisibleToUser] directly, without annotating [ExperimentalComposeUiApi] everywhere.
 * If looking for a way to hide semantics of small items from screen readers because they're
 * redundant with semantics of their parent, consider [Modifier.accessibilityClearForInvisibilityToUser] instead.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.accessibilityInvisibleToUser(): Modifier {
    return semantics {
        invisibleToUser()
    }
}

/**
 * Similar to [accessibilityClick] but create a [CustomAccessibilityAction] instead.
 * [CustomAccessibilityAction] are listed in Talkback actions menu (slide up then right and select Actions).
 */
@Suppress("unused")
fun SemanticsPropertyReceiver.accessibilityCustomAction(
    label: String,
    action: () -> Unit,
): CustomAccessibilityAction {
    return CustomAccessibilityAction(label = label) {
        action()
        // From Google's documentation:
        // The function should return a boolean result indicating whether the action is successfully handled. For example,
        // a scroll forward action should return false if the widget is not enabled or has reached the end of the list.
        // All our common use case should return true. Use your own `onClick` directly if you need to change the behavior.
        true
    }
}

/**
 * Shortcut if you only need to merge descendants without adding any properties.
 */
fun Modifier.accessibilityMergeDescendants(): Modifier {
    return semantics(mergeDescendants = true) { }
}

/**
 * Shortcut if you only need to set an element as heading.
 */
fun Modifier.accessibilityHeading(mergeDescendants: Boolean = false): Modifier {
    return semantics(mergeDescendants = mergeDescendants) { heading() }
}

/**
 * Shortcut if you only need to set a [LiveRegionMode].
 */
fun Modifier.accessibilityLiveRegion(mergeDescendants: Boolean = false, liveRegionMode: LiveRegionMode = LiveRegionMode.Polite): Modifier {
    return semantics(mergeDescendants = mergeDescendants) {
        liveRegion = liveRegionMode
    }
}

@Composable
fun rememberOSAccessibilityState(): AccessibilityState {
    return rememberAccessibilityState()
}
