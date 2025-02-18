package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.error.OSMigrationError

fun OSMigrationError.localizedTitle(): LbcTextSpec? = when (this.code) {
    OSMigrationError.Code.USERNAME_REMOVAL_FAIL,
    OSMigrationError.Code.SET_PASSWORD_VERIFICATION_FAIL,
    OSMigrationError.Code.DECRYPT_FAIL,
    OSMigrationError.Code.ENCRYPT_FAIL,
    OSMigrationError.Code.GET_DECRYPT_STREAM_FAIL,
    OSMigrationError.Code.GET_ENCRYPT_STREAM_FAIL,
    OSMigrationError.Code.MISSING_LEGACY_USERNAME,
    OSMigrationError.Code.MISSING_MIGRATION,
    -> null
}

fun OSMigrationError.localizedDescription(): LbcTextSpec? = when (this.code) {
    OSMigrationError.Code.USERNAME_REMOVAL_FAIL,
    OSMigrationError.Code.SET_PASSWORD_VERIFICATION_FAIL,
    OSMigrationError.Code.DECRYPT_FAIL,
    OSMigrationError.Code.ENCRYPT_FAIL,
    OSMigrationError.Code.GET_DECRYPT_STREAM_FAIL,
    OSMigrationError.Code.GET_ENCRYPT_STREAM_FAIL,
    OSMigrationError.Code.MISSING_LEGACY_USERNAME,
    OSMigrationError.Code.MISSING_MIGRATION,
    -> null
}
