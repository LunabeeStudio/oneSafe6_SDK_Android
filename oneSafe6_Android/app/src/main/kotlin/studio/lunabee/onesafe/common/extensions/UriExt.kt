package studio.lunabee.onesafe.common.extensions

import android.net.Uri
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants

fun Uri.isAutoBackup(): Boolean = scheme == ImportExportAndroidConstants.AUTO_BACKUP_SCHEME
