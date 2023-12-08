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
 * Created by Lunabee Studio / Date - 9/21/2023 - for the oneSafe6 SDK.
 * Last modified 9/21/23, 10:50 AM
 */

package studio.lunabee.onesafe.ime

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.patrickgold.florisboard.editorInstance
import dev.patrickgold.florisboard.ime.editor.EditorInstance
import dev.patrickgold.florisboard.ime.editor.FlorisEditorInfo
import studio.lunabee.onesafe.ime.model.OSKImeState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Save and restore [FlorisEditorInfo] on oSK state changes
 */
@Singleton
class OSKEditorInfoManager @Inject constructor(
    @ApplicationContext context: Context,
) : OSKImeStateObserver {
    private val editorInstance: EditorInstance = context.editorInstance().value
    private var previousFlorisEditorInfo: FlorisEditorInfo? = null

    override fun onStateChange(state: OSKImeState) {
        if (state.isOneSafeUiVisible) {
            previousFlorisEditorInfo = editorInstance.activeInfo
        } else {
            previousFlorisEditorInfo?.let {
                editorInstance.handleStartInput(it)
                previousFlorisEditorInfo = null
            }
        }
    }
}
