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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import studio.lunabee.onesafe.ui.theme.OSTheme
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
    messageTextLongPress: MessageTextLongPress,
    safeItemMessageCombinedPress: SafeItemMessageCombinedPress,
) {
    val style: MessageRowStyle = when (messageData.direction) {
        MessageDirection.SENT -> MessageRowDefault.send()
        MessageDirection.RECEIVED -> MessageRowDefault.received()
    }
    val longClick: (() -> Unit)? = when (messageData) {
        is ConversationUiData.Message.SafeItem -> {
            { safeItemMessageCombinedPress.onLongClick(messageData.id.uuid) }
        }
        is ConversationUiData.Message.Text -> if (messageTextLongPress.enabled(messageData.type)) {
            { messageTextLongPress.onLongClick(messageData.id.uuid) }
        } else {
            null
        }
    }
    val onClick: (() -> Unit)? = when (messageData) {
        is ConversationUiData.Message.SafeItem -> {
            { messageData.itemId?.let(safeItemMessageCombinedPress::onClick) }
        }
        is ConversationUiData.Message.Text -> null
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
                    enabled = longClick != null || onClick != null,
                    onLongClick = { longClick?.invoke() },
                    onClick = { onClick?.invoke() },
                    onClickLabel = null,
                    onLongClickLabel = null,
                )
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Small),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.ExtraSmall),
        ) {
            when (messageData) {
                is ConversationUiData.Message.SafeItem -> {
                    OSTheme(
                        isSystemInDarkTheme = true,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OSTheme(isSystemInDarkTheme = style.isIconDarkMode) {
                                messageData.icon.ImageComposable(contentDescription = null, style = OSSafeItemStyle.Small)
                            }
                            Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Small))
                            Column {
                                OSText(
                                    text = messageData.name.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = style.textColor,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                messageData.identifier?.let {
                                    OSText(
                                        text = it,
                                        style = MaterialTheme.typography.labelXSmall,
                                        color = style.textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Light,
                                    )
                                }
                            }
                        }
                    }
                }
                is ConversationUiData.Message.Text -> {
                    MessageText(
                        text = messageData.text,
                        textAlign = style.textAlign,
                        color = style.textColor,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            when (messageData) {
                is ConversationUiData.Message.SafeItem -> safeItemMessageCombinedPress.Content(message = messageData)
                is ConversationUiData.Message.Text -> messageTextLongPress.Content(messageData)
            }
            val channelText = messageData.channelName?.let(LbcTextSpec::Raw)
                ?: LbcTextSpec.StringResource(OSString.oneSafeK_channel_unknown)
            messageData.date?.let { sendAt ->
                OSText(
                    text = LbcTextSpec.StringResource(
                        OSString.oneSafeK_messageRow_timeChannelLabel,
                        sendAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
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
    val isIconDarkMode: Boolean
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
            override val isIconDarkMode: Boolean = isSystemInDarkTheme()
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
            override val isIconDarkMode: Boolean = !isSystemInDarkTheme()
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
            val messageRowLongPress = object : MessageTextLongPress() {
                override fun onLongClick(id: UUID) {}
            }
            MessageRow(
                messageData = ConversationUiData.Message.Text(
                    id = createRandomUUID(),
                    text = LbcTextSpec.Raw(loremIpsum(10)),
                    direction = MessageDirection.SENT,
                    date = Instant.now(),
                    channelName = "Telegram",
                    type = ConversationUiData.MessageType.Message,
                    hasCorruptedData = false,
                ),
                contactName = DefaultNameProvider("Flo"),
                messageTextLongPress = messageRowLongPress,
                safeItemMessageCombinedPress = DropDownSafeItemMessageCombinedPress(
                    onDeleteMessageClick = {},
                    onNavigateToItemClick = {},
                ),
            )
            MessageRow(
                messageData = ConversationUiData.Message.Text(
                    id = createRandomUUID(),
                    text = LbcTextSpec.Raw(loremIpsum(10)),
                    direction = MessageDirection.RECEIVED,
                    date = Instant.now(),
                    channelName = "Telegram",
                    type = ConversationUiData.MessageType.Message,
                    hasCorruptedData = false,
                ),
                contactName = DefaultNameProvider("Flo"),
                messageTextLongPress = messageRowLongPress,
                safeItemMessageCombinedPress = DropDownSafeItemMessageCombinedPress(
                    onDeleteMessageClick = {},
                    onNavigateToItemClick = {},
                ),
            )
            MessageRow(
                messageData = ConversationUiData.Message.Text(
                    id = createRandomUUID(),
                    text = LbcTextSpec.StringResource(OSString.bubbles_writeMessageScreen_corruptedMessage),
                    direction = MessageDirection.SENT,
                    date = Instant.now(),
                    channelName = null,
                    type = ConversationUiData.MessageType.Message,
                    hasCorruptedData = true,
                ),
                contactName = DefaultNameProvider("Flo"),
                messageTextLongPress = messageRowLongPress,
                safeItemMessageCombinedPress = DropDownSafeItemMessageCombinedPress(
                    onDeleteMessageClick = {},
                    onNavigateToItemClick = {},
                ),
            )
        }
    }
}
