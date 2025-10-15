/*
 * Copyright (c) 2024 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 10/2/2024 - for the oneSafe6 SDK.
 * Last modified 02/10/2024 11:07
 */

package studio.lunabee.onesafe.messaging.writemessage.composable

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText

@Composable
fun MessageText(
    text: LbcTextSpec,
    textAlign: TextAlign,
    color: Color,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val originalString = text.string
    val linkColor = MaterialTheme.colorScheme.inversePrimary
    val annotatedString: AnnotatedString by remember(originalString) {
        mutableStateOf(buildAnnotatedStringWithClickableLink(originalString, linkColor))
    }
    OSText(
        text = LbcTextSpec.Annotated(annotatedString),
        textAlign = textAlign,
        color = color,
        style = style,
        modifier = modifier,
    )
}

private fun buildAnnotatedStringWithClickableLink(
    text: String,
    linkColor: Color,
): AnnotatedString {
    val regex = android.util.Patterns.WEB_URL
        .toRegex()
    val matches: Sequence<MatchResult> = regex.findAll(text)
    return buildAnnotatedString {
        append(text)
        matches.forEach { link ->
            val startIndex = link.range.first
            val endIndex = link.range.last + 1 // MatchResult last index is inclusive while addLink end index in exclusive
            addLink(
                LinkAnnotation.Url(
                    url = link.value,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                ),
                start = startIndex,
                end = endIndex,
            )
        }
    }
}
