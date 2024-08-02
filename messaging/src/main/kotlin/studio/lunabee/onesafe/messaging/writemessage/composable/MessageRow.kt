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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.createRandomUUID
import studio.lunabee.messaging.domain.model.MessageDirection
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSItemIllustrationHelper
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.messaging.writemessage.model.ConversationUiData
import studio.lunabee.onesafe.model.OSSafeItemStyle
import studio.lunabee.onesafe.model.combinedClickableWithHaptic
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.UUID

@Composable
fun MessageRow(
    messageData: ConversationUiData.Message,
    contactName: OSNameProvider,
    messageLongPress: MessageLongPress,
) {
    val style: MessageRowStyle = when (messageData.direction) {
        MessageDirection.SENT -> MessageRowDefault.send()
        MessageDirection.RECEIVED -> MessageRowDefault.received()
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
                .combinedClickableWithHaptic(
                    enabled = messageLongPress.enabled(messageData.type),
                    onLongClick = { messageLongPress.onLongClick(messageData.id.uuid) },
                    onClick = {},
                    onClickLabel = null,
                    onLongClickLabel = null,
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
            messageLongPress.Content(messageData)
            val channelText = if (messageData.channelName != null) {
                LbcTextSpec.Raw(messageData.channelName)
            } else {
                LbcTextSpec.StringResource(OSString.oneSafeK_channel_unknown)
            }
            messageData.sendAt?.let {
                OSText(
                    text = LbcTextSpec.StringResource(
                        OSString.oneSafeK_messageRow_timeChannelLabel,
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
        }
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
            val messageRowLongPress = object : MessageLongPress() {
                override fun onLongClick(id: UUID) {}
            }
            MessageRow(
                messageData = ConversationUiData.Message(
                    id = createRandomUUID(),
                    text = LbcTextSpec.Raw(loremIpsum(10)),
                    direction = MessageDirection.SENT,
                    sendAt = Instant.now(),
                    channelName = "Telegram",
                    type = ConversationUiData.MessageType.Message,
                    hasCorruptedData = false,
                ),
                contactName = DefaultNameProvider("Flo"),
                messageLongPress = messageRowLongPress,
            )
            MessageRow(
                messageData = ConversationUiData.Message(
                    id = createRandomUUID(),
                    text = LbcTextSpec.Raw(loremIpsum(10)),
                    direction = MessageDirection.RECEIVED,
                    sendAt = Instant.now(),
                    channelName = "Telegram",
                    type = ConversationUiData.MessageType.Message,
                    hasCorruptedData = false,
                ),
                contactName = DefaultNameProvider("Flo"),
                messageLongPress = messageRowLongPress,
            )
            MessageRow(
                messageData = ConversationUiData.Message(
                    id = createRandomUUID(),
                    text = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_corruptedMessage),
                    direction = MessageDirection.SENT,
                    sendAt = null,
                    channelName = null,
                    type = ConversationUiData.MessageType.Message,
                    hasCorruptedData = true,
                ),
                contactName = DefaultNameProvider("Flo"),
                messageLongPress = messageRowLongPress,
            )
        }
    }
}
