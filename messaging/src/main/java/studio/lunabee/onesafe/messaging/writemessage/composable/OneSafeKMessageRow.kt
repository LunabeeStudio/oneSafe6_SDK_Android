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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 5:34 PM
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSItemIllustrationHelper
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.extension.copyToClipBoard
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.messaging.writemessage.model.MessageAction
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OneSafeKMessageRow(
    messageData: ConversationUiData.PlainMessageData,
    contactName: OSNameProvider,
    onResendClick: (UUID) -> Unit,
) {
    var isActionMenuExpanded: Boolean by rememberSaveable { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val messageText = messageData.text.string
    val context = LocalContext.current
    val style: MessageRowStyle = when (messageData.direction) {
        MessageDirection.SENT -> MessageRowDefault.send()
        MessageDirection.RECEIVED -> MessageRowDefault.received()
    }
    val actions: List<MessageAction> = when (messageData.direction) {
        MessageDirection.SENT -> listOf(
            MessageAction.Resend { messageData.id.let(onResendClick) },
            MessageAction.Copy {
                context.copyToClipBoard(
                    messageText,
                    LbcTextSpec.StringResource(R.string.bubbles_writeMessageScreen_copyLabel),
                )
            },
        )
        MessageDirection.RECEIVED -> listOf(
            MessageAction.Copy {
                context.copyToClipBoard(
                    messageText,
                    LbcTextSpec.StringResource(R.string.bubbles_writeMessageScreen_copyLabel),
                )
            },
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = style.containerArrangement,
        verticalAlignment = Alignment.Bottom,
    ) {
        if (messageData.direction == MessageDirection.RECEIVED) {
            OSItemIllustrationHelper.get(contactName).ImageComposable(contentDescription = null, style = OSSafeItemStyle.Small)
        }
        Column(
            modifier = Modifier
                .clip(style.shape)
                .background(style.backgroundColor)
                .fillMaxWidth(style.widthRow)
                .combinedClickable(
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        isActionMenuExpanded = true
                    },
                    onClick = {},
                )
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.ExtraSmall),
        ) {
            OSText(
                text = messageData.text,
                textAlign = style.textAlign,
                color = style.textColor,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
            val channelText = if (messageData.channelName != null) {
                LbcTextSpec.Raw(messageData.channelName)
            } else {
                LbcTextSpec.StringResource(R.string.oneSafeK_channel_unknown)
            }
            OSText(
                text = LbcTextSpec.StringResource(
                    R.string.oneSafeK_messageRow_timeChannelLabel,
                    messageData.sendAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                    channelText,
                ),
                textAlign = style.textAlign,
                color = style.textColor,
                style = MaterialTheme.typography.labelXSmall,
                fontWeight = FontWeight.Light,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        MessageActionMenu(
            isMenuExpended = isActionMenuExpanded,
            onDismiss = { isActionMenuExpanded = false },
            actions = actions,
        )
    }
}

private const val SendMessageRowWidthRatio: Float = 0.75f
private const val ReceiveMessageRowWidthRatio: Float = 0.9f

interface MessageRowStyle {
    val backgroundColor: Color
    val shape: RoundedCornerShape
    val textColor: Color
    val textAlign: TextAlign
    val containerArrangement: Arrangement.Horizontal
    val widthRow: Float
}

object MessageRowDefault {
    @Composable
    fun received(): MessageRowStyle {
        return object : MessageRowStyle {
            override val backgroundColor: Color = MaterialTheme.colorScheme.surface
            override val shape: RoundedCornerShape = RoundedCornerShape(
                topStart = OSDimens.SystemCornerRadius.ExtraLarge,
                topEnd = OSDimens.SystemCornerRadius.ExtraLarge,
                bottomStart = OSDimens.SystemCornerRadius.Small,
                bottomEnd = OSDimens.SystemCornerRadius.ExtraLarge,
            )
            override val textColor: Color = MaterialTheme.colorScheme.onSurface
            override val textAlign: TextAlign = TextAlign.Start
            override val containerArrangement: Arrangement.Horizontal = Arrangement.spacedBy(OSDimens.SystemSpacing.Small)
            override val widthRow: Float = ReceiveMessageRowWidthRatio
        }
    }

    @Composable
    fun send(): MessageRowStyle {
        return object : MessageRowStyle {
            override val backgroundColor: Color = MaterialTheme.colorScheme.primary
            override val shape: RoundedCornerShape = RoundedCornerShape(
                topStart = OSDimens.SystemCornerRadius.ExtraLarge,
                topEnd = OSDimens.SystemCornerRadius.ExtraLarge,
                bottomStart = OSDimens.SystemCornerRadius.ExtraLarge,
                bottomEnd = OSDimens.SystemCornerRadius.Small,
            )
            override val textColor: Color = MaterialTheme.colorScheme.onPrimary
            override val textAlign: TextAlign = TextAlign.End
            override val containerArrangement: Arrangement.Horizontal = Arrangement.End
            override val widthRow: Float = SendMessageRowWidthRatio
        }
    }
}

@Preview
@Composable
fun OneSafeKMessageRowPreview() {
    OSPreviewBackgroundTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(OSDimens.SystemSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            OneSafeKMessageRow(
                messageData = ConversationUiData.PlainMessageData(
                    id = UUID.randomUUID(),
                    text = LbcTextSpec.Raw(loremIpsum(10)),
                    direction = MessageDirection.SENT,
                    sendAt = Instant.now(),
                    channelName = "Telegram",
                ),
                contactName = DefaultNameProvider("Flo"),
                onResendClick = {},
            )
            OneSafeKMessageRow(
                messageData = ConversationUiData.PlainMessageData(
                    id = UUID.randomUUID(),
                    text = LbcTextSpec.Raw(loremIpsum(10)),
                    direction = MessageDirection.RECEIVED,
                    sendAt = Instant.now(),
                    channelName = "Telegram",
                ),
                contactName = DefaultNameProvider("Flo"),
                onResendClick = {},
            )
        }
    }
}
