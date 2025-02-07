package studio.lunabee.onesafe.usecase

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.common.UpdateState
import studio.lunabee.onesafe.domain.usecase.CheckValueChangeUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import javax.inject.Inject
import kotlin.test.assertIs

@HiltAndroidTest
class CheckValueUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.SignedOut

    @Inject
    lateinit var checkValueChangeUseCase: CheckValueChangeUseCase

    @Test
    fun value_unchanged() {
        val result = checkValueChangeUseCase(value = "test", previousValue = "test")
        assertIs<UpdateState.Unchanged<String>>(result)
    }

    @Test
    fun value_removed() {
        val result = checkValueChangeUseCase(value = null, previousValue = "test")
        assertIs<UpdateState.Removed<String>>(result)
    }

    @Test
    fun value_modified() {
        val result = checkValueChangeUseCase(value = "testModified", previousValue = "test")
        assertIs<UpdateState.ModifiedTo<String>>(result)
    }
}
