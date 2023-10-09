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
 * Created by Lunabee Studio / Date - 10/3/2023 - for the oneSafe6 SDK.
 * Last modified 10/3/23, 11:54 AM
 */

package studio.lunabee.onesafe.importexport.ui.settings

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import studio.lunabee.onesafe.commonui.navigation.OSDestination

fun NavGraphBuilder.autoBackupSettingsNavGraph(
    navigateBack: () -> Unit,
    navigateToRestoreBackup: (Uri) -> Unit,
) {
    navigation(startDestination = AutoBackupSettingsScreenDestination.route, route = AutoBackupSettingsNavGraphDestination.route) {
        autoBackupSettingsScreen(
            navigateBack = navigateBack,
            navigateToRestoreBackup = navigateToRestoreBackup,
        )
    }
}

object AutoBackupSettingsNavGraphDestination : OSDestination {
    override val route: String = "auto_backup_settings_graph"
}
