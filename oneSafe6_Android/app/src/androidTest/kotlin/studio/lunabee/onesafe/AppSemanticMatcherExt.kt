package studio.lunabee.onesafe

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import studio.lunabee.onesafe.ui.UiConstants

fun hasExcludeSearch(semanticMatcher: SemanticsMatcher): SemanticsMatcher = SemanticsMatcher(
    description = "NOT Ancestor SearchScreen with additional matcher ${semanticMatcher.description}",
) {
    !hasAnyAncestor(hasTestTag(UiConstants.TestTag.Screen.SearchScreen)).matches(it) && semanticMatcher.matches(it)
}

fun hasOnlySearch(semanticMatcher: SemanticsMatcher): SemanticsMatcher = SemanticsMatcher(
    description = "Ancestor SearchScreen with additional matcher ${semanticMatcher.description}",
) {
    hasAnyAncestor(hasTestTag(UiConstants.TestTag.Screen.SearchScreen)).matches(it) && semanticMatcher.matches(it)
}

/**
 * FIXME Breadcrumb may hold a reference to item name causing multiple match when using onNodeWithX methods.
 *  -> can't determine for now the origin of this problem.
 */
fun hasExcludeBreadcrumb(semanticMatcher: SemanticsMatcher): SemanticsMatcher {
    return SemanticsMatcher(
        description = "NOT Ancestor: BreadCrumbLayout with additional matcher ${semanticMatcher.description}",
    ) {
        !hasAnyAncestor(hasTestTag(UiConstants.TestTag.BreadCrumb.BreadCrumbLayout)).matches(it) && semanticMatcher.matches(it)
    }
}
