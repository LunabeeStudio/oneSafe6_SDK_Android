package studio.lunabee.onesafe

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Workaround Compose + Hilt + AndroidTest
 * https://github.com/google/dagger/issues/3394#issue-1233863896
 */

@AndroidEntryPoint
class HiltComponentActivity : ComponentActivity()
