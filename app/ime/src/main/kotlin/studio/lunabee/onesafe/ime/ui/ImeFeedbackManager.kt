/*
 * Copyright (c) 2023 Lunabee Studio
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
 * Last modified 7/6/23, 5:20 PM
 */

package studio.lunabee.onesafe.ime.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.bubbles.ui.home.BubblesHomeDestination
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.error.title
import studio.lunabee.onesafe.commonui.notification.OSNotificationChannelId
import studio.lunabee.onesafe.commonui.notification.OSNotificationManager
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.messaging.domain.usecase.IncomingMessageState
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class ImeFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val osNotificationManager: OSNotificationManager,
) {
    fun sendIncomingMessageFeedback(result: LBResult<IncomingMessageState>) {
        var title: String? = null
        var description: String? = null
        var pendingIntent: PendingIntent? = null
        when (result) {
            is LBResult.Failure -> {
                title = (result.throwable as? OSError)?.title()?.string(context)
                description = result.throwable?.localizedMessage ?: context.getString(R.string.common_error_unknown)
                if ((result.throwable as? OSStorageError)?.code == OSStorageError.Code.ENQUEUED_MESSAGE_ALREADY_EXIST_ERROR) {
                    pendingIntent = getContactPendingIntent(null)
                }
            }
            is LBResult.Success -> {
                val messageState = result.successData
                when (messageState) {
                    IncomingMessageState.NotBase64 -> return // no feedback
                    IncomingMessageState.Enqueued -> {
                        title = context.getString(R.string.notification_messaging_enqueued_description)
                        pendingIntent = getContactPendingIntent(null)
                    }
                    is IncomingMessageState.Processed -> {
                        pendingIntent = getContactPendingIntent(messageState.contactId)
                        title = context.getString(R.string.notification_messaging_processed_description)
                    }
                }
            }
        }

        @SuppressLint("MissingPermission")
        if (osNotificationManager.areNotificationsEnabled(OSNotificationChannelId.MESSAGING_CHANNEL_ID)) {
            val notification = osNotificationManager.messagingNotificationBuilder
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(description)
                .setTicker(title)
                .setAutoCancel(true)
                .setTimeoutAfter(1.minutes.inWholeMilliseconds)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()

            osNotificationManager.manager.notify(OSNotificationManager.NEW_MESSAGE_NOTIFICATION_ID, notification)
        } else {
            Toast.makeText(
                context,
                description ?: title ?: context.getString(R.string.common_error_unknown),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    // TODO oSK improve to send the enqueued message id so we can redirect when the queue is processed
    // TODO oSK update or dismiss notification if the enqueued message has been processed
    private fun getContactPendingIntent(contactId: UUID?): PendingIntent? {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            val route = if (contactId != null) {
                WriteMessageDestination.getRouteFromContactId(contactId)
            } else {
                BubblesHomeDestination.route
            }
            data = Uri.Builder()
                .scheme(CommonUiConstants.Deeplink.MAIN_NAV_SCHEME)
                .authority(route)
                .build()
        }
        return PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)
    }
}
