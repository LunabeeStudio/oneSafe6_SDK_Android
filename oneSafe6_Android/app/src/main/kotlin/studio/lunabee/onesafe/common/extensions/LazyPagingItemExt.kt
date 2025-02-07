package studio.lunabee.onesafe.common.extensions

import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

fun LazyPagingItems<*>.isInitializing(): Boolean {
    return isLoading() && itemCount == 0
}

fun LazyPagingItems<*>.isEmptyAfterLoading(): Boolean {
    return !isLoading() && itemCount == 0
}

fun LazyPagingItems<*>.isLoading(): Boolean {
    return loadState.source.append == LoadState.Loading
        || loadState.source.prepend == LoadState.Loading
        || loadState.source.refresh == LoadState.Loading
}
