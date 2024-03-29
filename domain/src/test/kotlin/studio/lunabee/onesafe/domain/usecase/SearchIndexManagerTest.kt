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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.domain.usecase

import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbextensions.lazyFast
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import studio.lunabee.onesafe.domain.manager.SearchIndexManager
import studio.lunabee.onesafe.domain.model.search.PlainIndexWordEntry
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.usecase.search.DecryptIndexWordUseCase
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.testUUIDs
import kotlin.test.assertContentEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SearchIndexManagerTest {
    private val indexManager: SearchIndexManager by lazyFast {
        SearchIndexManager(indexWordEntryRepository, decryptIndexWordUseCase)
    }

    @MockK lateinit var indexWordEntryRepository: IndexWordEntryRepository

    @MockK lateinit var decryptIndexWordUseCase: DecryptIndexWordUseCase

    private val indexList: List<IndexWordEntry> = listOf(IndexWordEntry(byteArrayOf(), testUUIDs[0], null))
    private val clearIndexWordEntries = listOf(PlainIndexWordEntry("word", testUUIDs[0], null))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        every { indexWordEntryRepository.getAll() } returns flowOf(indexList)
        coEvery { decryptIndexWordUseCase.invoke(indexList) } returns LBResult.Success(clearIndexWordEntries)
    }

    @Test
    fun initStoreIndex_test(): TestResult = runTest {
        assertIs<LBFlowResult.Loading<List<PlainIndexWordEntry>>>(indexManager.decryptedIndex.value)
        assertNull(indexManager.decryptedIndex.value.data)

        indexManager.initStoreIndex(this)
        runCurrent()

        assertSuccess(indexManager.decryptedIndex.value)
        assertContentEquals(clearIndexWordEntries, indexManager.decryptedIndex.value.data)
    }

    @Test
    fun clear_index_after_delay_test(): TestResult = runTest {
        indexManager.initStoreIndex(this)
        advanceTimeBy(SearchIndexManager.DELAY_CLEAR_DECRYPTED_INDEX)

        assertSuccess(indexManager.decryptedIndex.value)
        assertNotNull(indexManager.decryptedIndex.value.data)

        runCurrent()

        assertIs<LBFlowResult.Loading<List<PlainIndexWordEntry>>>(indexManager.decryptedIndex.value)
        assertNull(indexManager.decryptedIndex.value.data)
    }

    @Test
    fun collect_new_item_when_index_is_decrypted_test(): TestResult = runTest {
        val expectedIndex = listOf(
            PlainIndexWordEntry("word", testUUIDs[0], null),
            PlainIndexWordEntry("word2", testUUIDs[1], null),
        )

        val indexList2 = listOf(
            IndexWordEntry(byteArrayOf(), testUUIDs[0], null),
            IndexWordEntry(byteArrayOf(), testUUIDs[1], null),
        )
        val indexFlow = MutableStateFlow(indexList)

        every { indexWordEntryRepository.getAll() } returns indexFlow
        coEvery { decryptIndexWordUseCase.invoke(indexList2) } returns LBResult.Success(expectedIndex)

        indexManager.initStoreIndex(this)
        runCurrent()

        assertContentEquals(clearIndexWordEntries, indexManager.decryptedIndex.value.data!!.toList())

        indexFlow.value = indexList2

        runCurrent()

        assertContentEquals(expectedIndex, indexManager.decryptedIndex.value.data!!.toList())
    }
}
