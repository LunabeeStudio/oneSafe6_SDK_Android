package studio.lunabee.onesafe.usecase.knownclient

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.onesafe.domain.model.client.AfClientData
import studio.lunabee.onesafe.domain.usecase.FindKnownClientDataUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class FindKnownClientUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject lateinit var findKnownClientUseCase: FindKnownClientDataUseCase

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun test_find_known_client() {
        assertEquals(
            expected = oneSafeAppData,
            actual = findKnownClientUseCase("studio.lunabee.onesafe", "", knownClients),
        )

        assertEquals(
            expected = oneSafeAppData,
            actual = findKnownClientUseCase("", "oneSafe6.com", knownClients),
        )

        assertEquals(
            expected = oneSafeAppData,
            actual = findKnownClientUseCase("studio.lunabee.onesafe", "oneSafe6.com", knownClients),
        )
    }

    @Test
    fun test_do_not_find_known_client() {
        assertEquals(
            expected = null,
            actual = findKnownClientUseCase("unknown.package", "", knownClients),
        )

        assertEquals(
            expected = null,
            actual = findKnownClientUseCase("", "example.com", knownClients),
        )

        assertEquals(
            expected = null,
            actual = findKnownClientUseCase("unknown.package", "example.com", knownClients),
        )
    }

    private val oneSafeAppData = AfClientData(
        name = "oneSafe6",
        domains = listOf("oneSafe6.com"),
        appPackage = "studio.lunabee.onesafe",
    )

    private val knownClients = listOf(
        oneSafeAppData,
    )
}
