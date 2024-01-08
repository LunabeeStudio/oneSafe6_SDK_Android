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
 * Created by Lunabee Studio / Date - 5/15/2023 - for the oneSafe6 SDK.
 * Last modified 5/15/23, 4:11 PM
 */

package studio.lunabee.onesafe.messagecompanion

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Rect
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.onesafe.messagecompanion.databinding.AccessibilityLayoutBinding
import studio.lunabee.onesafe.messagecompanion.databinding.DecryptedMessageLayoutBinding

private val logger = LBLogger.get<OSAccessibilityService>()

/**
 * Accessibility Service of the oneSafe 6 app.
 * The purpose of this service is to analyse the view tree displayed on the screen in the search of encrypted messages. If such messages are
 * found then the service is able to display the decrypted version above the associated view.
 */
class OSAccessibilityService : AccessibilityService() {

    /**
     * System service that will be used to add decrypted message's views to the displayed window.
     */
    private var windowManager: WindowManager? = null

    /**
     * Current state of the messages displayed on the screen.
     * If set to  [MessagesDisplayedState.Encrypted], then no modification is set to the current window.
     * If set to  [MessagesDisplayedState.Plain],  then the encountered encrypted message's views are added to the current window.
     */
    private var messagesDisplayState: MessagesDisplayedState = MessagesDisplayedState.Encrypted

    /**
     * List of views used to display plain messages.
     * We saved them in a list so that if when analysing the current view tree we found oneSafe encrypted message  and we can add them to
     * the window as soon as the messageDisplay is set to MessagesDisplayed.Plain.
     */
    private val plainMessagesView: MutableList<PlainMessageView> = mutableListOf()

    /**
     * If we catch a [AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED] event, we will go through the current view tree in search of oneSafe
     * encrypted messages.
     * In case the previous plain messages are no visible anymore, we remove all of them from the window. (If they are still visible, even
     * at another position they will be added when going through the view tree).
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && messagesDisplayState == MessagesDisplayedState.Plain) {
            removeAllPlainMessagesFromTheWindow()
            goThroughAccessibilityNodesAndAddDecryptedMessages(rootInActiveWindow)
        }
    }

    /**
     * Remove all the current plain messages view from the window to avoid duplication.
     */
    private fun removeAllPlainMessagesFromTheWindow() {
        plainMessagesView.forEach {
            try {
                windowManager?.removeView(it.view)
            } catch (e: Exception) {
                logger.e(e)
            }
        }
        plainMessagesView.clear()
    }

    /**
     * Method that will go through an accessibility node in the search of one (or more) encrypted message(s).
     * an AccessibilityNodeInfo can be composed of several children that are also AccessibilityNodeInfo.
     * Thus, we need to go through all the children of all the children of the source and so on... Hence the recursive function.
     */
    private fun goThroughAccessibilityNodesAndAddDecryptedMessages(source: AccessibilityNodeInfo?) {
        repeat(source?.childCount ?: 0) {
            val child = source?.getChild(it)
            if (child != null) {
                if (child.text.isOneSafeKMessage()) {
                    addDecryptedMessageView(child)
                }
                goThroughAccessibilityNodesAndAddDecryptedMessages(child)
            }
        }
    }

    /**
     * Method that will add a view displaying the plain message to the window manager
     * (or save it for later use in the plainMessagesView list if the messagesDisplayState is set to Encrypted)
     */
    private fun addDecryptedMessageView(node: AccessibilityNodeInfo?) {
        // Compute the position on the screen of the node.
        val rect = Rect()
        node?.getBoundsInScreen(rect)

        // Init and set the layout params of the view
        val layoutParams = LayoutParams().apply {
            type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = flags or LayoutParams.FLAG_NOT_FOCUSABLE
            x = rect.left
            y = rect.top - (rect.bottom - rect.top)
            width = rect.right - rect.left
            height = LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP
        }

        // inflate the view and set the text
        val mLayout = FrameLayout(this)
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.decrypted_message_layout, mLayout)
        val decryptedMessageLayout = DecryptedMessageLayoutBinding.inflate(inflater)
        decryptedMessageLayout.decryptedMessage.text = node?.text.trimOneSafeKMessage()

        if (messagesDisplayState == MessagesDisplayedState.Plain) {
            try {
                windowManager?.addView(decryptedMessageLayout.root, layoutParams)
            } catch (e: Exception) {
                logger.e(e)
            }
        }
        plainMessagesView.add(PlainMessageView(layoutParams, decryptedMessageLayout.root))
    }

    /**
     * Extension method that returns true if the CharSequence is an encrypted oneSafe message.
     */
    private fun CharSequence?.isOneSafeKMessage(): Boolean {
        return this?.startsWith(OneSafeKMessagePrefix) ?: false
    }

    /**
     * Extension method that remove everything after the OneSafeKMessageSuffix.
     */
    private fun CharSequence?.trimOneSafeKMessage(): String {
        if (this?.contains(OneSafeKMessageSuffix) == true) {
            return this.toString().substringBefore(OneSafeKMessageSuffix).substringAfter(OneSafeKMessagePrefix).reversed()
        }
        return toString().substringAfter(OneSafeKMessagePrefix).reversed()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        addOneSafeKTopView()
    }

    /**
     * Method that add the oneSafe K top view the user can interact with to show or hide the plain messages.
     */
    private fun addOneSafeKTopView() {
        val mLayout = FrameLayout(this)

        val layoutParams = LayoutParams().apply {
            type = LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
            format = PixelFormat.TRANSLUCENT
            flags = flags or LayoutParams.FLAG_NOT_FOCUSABLE
            width = LayoutParams.WRAP_CONTENT
            height = LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP
        }
        val inflater = LayoutInflater.from(this)
        inflater.inflate(R.layout.accessibility_layout, mLayout)
        val accessibilityLayout = AccessibilityLayoutBinding.inflate(inflater)

        // Set up the display/hide plain message button
        accessibilityLayout.seeModeButton.setOnClickListener {
            if (messagesDisplayState == MessagesDisplayedState.Plain) {
                messagesDisplayState = MessagesDisplayedState.Encrypted
                accessibilityLayout.seeModeButton.setImageResource(R.drawable.ic_visibility_on)
                accessibilityLayout.modeTextView.text = getString(R.string.oneSafeK_accessibilityService_encryptedLabel)
                // remove all views displayed on the screen :
                removeAllPlainMessagesFromTheWindow()
                plainMessagesView.forEach {
                    try {
                        windowManager?.removeView(it.view)
                    } catch (e: Exception) {
                        logger.e(e)
                    }
                }
            } else {
                accessibilityLayout.modeTextView.text = getString(R.string.oneSafeK_accessibilityService_plainLabel)
                accessibilityLayout.seeModeButton.setImageResource(R.drawable.ic_visibility_off)
                messagesDisplayState = MessagesDisplayedState.Plain
                removeAllPlainMessagesFromTheWindow()
                goThroughAccessibilityNodesAndAddDecryptedMessages(rootInActiveWindow)
            }
        }

        // Set up the close button
        accessibilityLayout.closeBtn.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(intent)
        }

        windowManager?.addView(accessibilityLayout.root, layoutParams)
    }

    override fun onInterrupt() {}
}

private const val OneSafeKMessagePrefix: String = "OS6:"
private const val OneSafeKMessageSuffix: String = ":OS6"
