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
 * Created by Lunabee Studio / Date - 8/27/2024 - for the oneSafe6 SDK.
 * Last modified 27/08/2024 11:10
 */

package studio.lunabee.onesafe.feature.importbackup.selectdata

import studio.lunabee.onesafe.commonui.OSDestination

object ImportSelectDataDestination : OSDestination {
    private const val path: String = "importSelectData"

    override val route: String = path
}

interface ImportSelectDataNavScope {
    val navigateBack: () -> Unit
    val continueImport: (Boolean) -> Unit
}
