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
 * Created by Lunabee Studio / Date - 12/11/2023 - for the oneSafe6 SDK.
 * Last modified 12/11/23, 10:22 AM
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.model.safeitem.ItemNameWithIndex
import studio.lunabee.onesafe.domain.usecase.item.CleanForAlphaIndexingUseCase
import studio.lunabee.onesafe.domain.usecase.item.ComputeItemAlphaIndexUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSError.Companion.get
import studio.lunabee.onesafe.error.osCode
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.testUUIDs
import java.util.UUID
import kotlin.reflect.KClass
import kotlin.test.assertEquals

class ComputeItemAlphaIndexUseCaseTest {

    private val items: MutableList<ItemNameWithIndex> = mutableListOf()

    private val itemDecryptUseCase: ItemDecryptUseCase = mockk {
        coEvery { this@mockk.invoke(any<ByteArray>(), any<UUID>(), any<KClass<String>>()) } answers {
            LBResult.Success(items.first { it.id == secondArg() }.encName!!.toString(Charsets.UTF_8))
        }
    }

    val useCase: ComputeItemAlphaIndexUseCase by lazy {
        ComputeItemAlphaIndexUseCase(
            safeItemRepository = mockk {
                coEvery { getItemNameWithIndexAt(any()) } answers {
                    val idx = firstArg() as Int
                    items[idx]
                }
                coEvery { getAlphaIndexRange() } answers {
                    items.sortBy { it.indexAlpha }
                    (items.firstOrNull()?.indexAlpha ?: 0.0) to (items.lastOrNull()?.indexAlpha ?: 0.0)
                }
                coEvery { getSafeItemsCount() } answers {
                    items.size
                }
            },
            itemDecryptUseCase = itemDecryptUseCase,
            cleanForAlphaIndexingUseCase = CleanForAlphaIndexingUseCase(),
        )
    }

    @Test
    fun null_empty_item_test(): TestResult = runTest {
        items += ItemNameWithIndex(testUUIDs[0], "A".toByteArray(Charsets.UTF_8), 0.0)
        items += ItemNameWithIndex(testUUIDs[1], "B".toByteArray(Charsets.UTF_8), 4.0)

        val expected = 5.0
        val actual = useCase(null).data!!
        assertEquals(expected, actual)
        items += ItemNameWithIndex(testUUIDs[2], "".toByteArray(Charsets.UTF_8), 5.0)

        val expected2 = 5.0
        val actual2 = useCase("").data!!
        assertEquals(expected2, actual2)
    }

    @Test
    fun equals_item_test(): TestResult = runTest {
        items += ItemNameWithIndex(testUUIDs[0], "A".toByteArray(Charsets.UTF_8), 0.0)

        val expectedA = 0.0
        val actualA = useCase("A").data!!
        assertEquals(expectedA, actualA)
    }

    @Test
    fun first_item_test(): TestResult = runTest {
        val expected = 0.0
        val actual = useCase("A").data!!

        assertEquals(expected, actual)
    }

    @Test
    fun after_and_before_test(): TestResult = runTest {
        items += ItemNameWithIndex(testUUIDs[0], "A".toByteArray(Charsets.UTF_8), 0.0)

        val expectedB = 1.0
        val actualB = useCase("B").data!!
        assertEquals(expectedB, actualB)

        val expected1 = -1.0
        val actual1 = useCase("1").data!!
        assertEquals(expected1, actual1)
    }

    @Test
    fun middle_test(): TestResult = runTest {
        items += ItemNameWithIndex(testUUIDs[0], "A".toByteArray(Charsets.UTF_8), 0.0)
        items += ItemNameWithIndex(testUUIDs[1], "Z".toByteArray(Charsets.UTF_8), 10.0)

        val expectedF = 5.0
        val actualF = useCase("F").data!!
        assertEquals(expectedF, actualF)
        items += ItemNameWithIndex(testUUIDs[2], "F".toByteArray(Charsets.UTF_8), actualF)

        val expectedS = 7.5
        val actualS = useCase("S").data!!
        assertEquals(expectedS, actualS)
        items += ItemNameWithIndex(testUUIDs[3], "S".toByteArray(Charsets.UTF_8), actualS)

        val expectedX = 8.75
        val actualX = useCase("X").data!!
        assertEquals(expectedX, actualX)
        items += ItemNameWithIndex(testUUIDs[4], "X".toByteArray(Charsets.UTF_8), actualX)

        val expectedZero = -1.0
        val actualZero = useCase("0").data!!
        assertEquals(expectedZero, actualZero)
        items += ItemNameWithIndex(testUUIDs[5], "0".toByteArray(Charsets.UTF_8), actualZero)

        val expectedZeroA = -0.5
        val actualZeroA = useCase("0A").data!!
        assertEquals(expectedZeroA, actualZeroA)
        items += ItemNameWithIndex(testUUIDs[6], "0A".toByteArray(Charsets.UTF_8), actualZeroA)

        printSortedItems()
    }

    @Test
    fun error_test(): TestResult = runTest {
        coEvery { itemDecryptUseCase.invoke(any<ByteArray>(), any<UUID>(), any<KClass<String>>()) } throws
            OSCryptoError.Code.MASTER_KEY_NOT_LOADED.get()

        items += ItemNameWithIndex(testUUIDs[0], "A".toByteArray(Charsets.UTF_8), 0.0)

        val actual = useCase("B")
        assertFailure(actual)
        assertEquals(OSDomainError.Code.ALPHA_INDEX_COMPUTE_FAILED, actual.throwable.osCode()!!)
    }

    @Test
    fun no_change_required_test(): TestResult = runTest {
        items += ItemNameWithIndex(testUUIDs[0], "A".toByteArray(Charsets.UTF_8), 0.0)
        items += ItemNameWithIndex(testUUIDs[1], "B".toByteArray(Charsets.UTF_8), 1.0)
        items += ItemNameWithIndex(testUUIDs[2], "C".toByteArray(Charsets.UTF_8), 2.0)

        val actualLower = useCase("AB", 1.0).data!!
        val actualHigher = useCase("BC", 1.0).data!!
        assertEquals(1.0, actualLower)
        assertEquals(1.0, actualHigher)
    }

    private fun printSortedItems() {
        println(items.sortedBy { it.indexAlpha }.joinToString("\n") { "${it.encName?.toString(Charsets.UTF_8)}\t->\t${it.indexAlpha}" })
    }
}
