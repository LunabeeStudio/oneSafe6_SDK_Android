/*
 * Copyright (c) 2023-2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 7/6/2023 - for the oneSafe6 SDK.
 * Last modified 7/6/23, 4:53 PM
 */

package studio.lunabee.onesafe.commonui.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import javax.inject.Inject

class OSNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    val manager: NotificationManagerCompat = NotificationManagerCompat.from(context)

    val messagingNotificationBuilder: NotificationCompat.Builder
        get() {
            manager.createNotificationChannel(messagingChannel)
            return getDefaultNotificationBuilder(OSNotificationChannelId.MESSAGING_CHANNEL_ID)
        }

    private val messagingChannel: NotificationChannelCompat
        get() = NotificationChannelCompat.Builder(
            OSNotificationChannelId.MESSAGING_CHANNEL_ID.id,
            NotificationManagerCompat.IMPORTANCE_HIGH,
        )
            .setName(context.getString(OSString.notification_channel_messaging_name))
            .setDescription(context.getString(OSString.notification_channel_messaging_description))
            .setSound(null, null)
            .build()

    val backupNotificationBuilder: NotificationCompat.Builder
        get() {
            manager.createNotificationChannel(backupChannel)
            return getDefaultNotificationBuilder(OSNotificationChannelId.BACKUP_CHANNEL_ID)
        }

    private val backupChannel: NotificationChannelCompat
        get() = NotificationChannelCompat.Builder(
            OSNotificationChannelId.BACKUP_CHANNEL_ID.id,
            NotificationManagerCompat.IMPORTANCE_HIGH, // allow heads-up notifications
        )
            .setName(context.getString(OSString.notification_channel_backup_name))
            .setDescription(context.getString(OSString.notification_channel_backup_description))
            .build()

    private fun getDefaultNotificationBuilder(channelId: OSNotificationChannelId): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId.id)
            .setSmallIcon(OSDrawable.ic_onesafe_notification)

    fun areNotificationsEnabled(channelId: OSNotificationChannelId?): Boolean {
        if (!manager.areNotificationsEnabled()) return false

        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        if (!hasPermission) return false

        val isChannelEnabled = if (channelId != null) {
            createNotificationChannel(channelId)
            val channel = manager.getNotificationChannelCompat(channelId.id)
            channel?.importance != NotificationManagerCompat.IMPORTANCE_NONE
        } else {
            true
        }

        return isChannelEnabled
    }

    private fun createNotificationChannel(channelId: OSNotificationChannelId) {
        when (channelId) {
            OSNotificationChannelId.MESSAGING_CHANNEL_ID -> manager.createNotificationChannel(messagingChannel)
            OSNotificationChannelId.BACKUP_CHANNEL_ID -> manager.createNotificationChannel(backupChannel)
        }
    }

    companion object {
        const val EXPORT_WORKER_NOTIFICATION_ID: Int = 0
        const val NEW_MESSAGE_NOTIFICATION_ID: Int = 1
        const val AUTO_BACKUP_WORKER_NOTIFICATION_ID: Int = 2
        const val AUTO_BACKUP_ERROR_WORKER_NOTIFICATION_ID: Int = 3
    }
}
