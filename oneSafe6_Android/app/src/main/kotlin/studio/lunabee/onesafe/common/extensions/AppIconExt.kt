package studio.lunabee.onesafe.common.extensions

import studio.lunabee.onesafe.BuildConfig
import studio.lunabee.onesafe.model.AppIcon

val AppIcon.alias: String
    get() = when (this) {
        AppIcon.Default -> BuildConfig.ALIAS_appIconDefault
        AppIcon.DefaultDark -> BuildConfig.ALIAS_appIconDefaultDark
        AppIcon.ChessText -> BuildConfig.ALIAS_appIconChessText
        AppIcon.Chess -> BuildConfig.ALIAS_appIconChess
        AppIcon.Headphones -> BuildConfig.ALIAS_appIconHeadphones
    }
