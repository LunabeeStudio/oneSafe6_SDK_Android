package studio.lunabee.onesafe.help.debug.extension

import androidx.navigation.NavBackStackEntry

internal fun NavBackStackEntry.resolveArgsToString(): String? {
    var routeString = destination.route
    val args = destination.arguments
    args.forEach { arg ->
        @Suppress("DEPRECATION")
        val argValue = arguments?.get(arg.key)
        routeString = routeString?.replace("{${arg.key}}", argValue.toString())
    }

    return routeString
}
