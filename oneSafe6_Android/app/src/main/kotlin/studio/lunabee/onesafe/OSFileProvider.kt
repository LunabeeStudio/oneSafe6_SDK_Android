package studio.lunabee.onesafe

import androidx.annotation.XmlRes
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat

/**
 * Avoid manifest collision
 */
class OSFileProvider @JvmOverloads constructor(@XmlRes resourceId: Int = ResourcesCompat.ID_NULL) : FileProvider(resourceId)
