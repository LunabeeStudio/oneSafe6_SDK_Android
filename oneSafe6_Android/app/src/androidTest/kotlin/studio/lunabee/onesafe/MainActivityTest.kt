package studio.lunabee.onesafe

import org.junit.Test

class MainActivityTest {

    // Duplicated code from MainActivity.kt to make sure it still work after lib update
    @Test
    fun tabRow_reflection_test() {
        Class
            .forName(AppConstants.Reflection.tabRowKt)
            .getDeclaredField(AppConstants.Reflection.scrollableTabRowMinimumTabWidth).apply {
                isAccessible = true
            }.set(null, 0f)
    }
}
