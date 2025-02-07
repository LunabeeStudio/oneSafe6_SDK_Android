package studio.lunabee.onesafe

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.emoji2.text.EmojiCompat
import androidx.paging.LoadState
import androidx.paging.LoadStates
import com.lunabee.lbextensions.lazyFast
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.bubbles.domain.model.contact.PlainContact
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.PlainItemDataDefault
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow
import studio.lunabee.onesafe.common.utils.DrawableIdProperty
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.item.ItemsLayout
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemactions.ComposeItemActionNavScope
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.OSTestConfig
import java.lang.reflect.Field
import java.util.UUID

internal object AppAndroidTestUtils {
    fun createPlainItemData(
        id: UUID = UUID.randomUUID(),
        itemNameProvider: OSNameProvider = DefaultNameProvider(null),
        icon: ByteArray? = null,
        color: Color? = null,
        identifier: LbcTextSpec? = null,
        actions: (suspend () -> List<SafeItemAction>)? = null,
        itemsLayout: ItemsLayout = ItemsLayout.from(OSTestConfig.itemLayouts),
    ): PlainItemData {
        return when (itemsLayout) {
            ItemsLayout.Grid -> PlainItemDataDefault(id, itemNameProvider, icon, color, actions)
            ItemsLayout.List -> PlainItemDataRow(id, itemNameProvider, icon, color, identifier, actions)
        }
    }

    fun composeItemActionNavScopeTest(): ComposeItemActionNavScope {
        return object : ComposeItemActionNavScope {
            override val showSnackbar: (visuals: SnackbarVisuals) -> Unit = {}
            override val navigateToMove: (itemId: UUID) -> Unit = {}
            override val navigateToShare: (itemId: UUID, includeChildren: Boolean) -> Unit = { _, _ -> }
            override val navigateToSendViaBubbles: (itemId: UUID, includeChildren: Boolean) -> Unit = { _, _ -> }
            override val navigateBack: () -> Unit = {}
        }
    }

    fun createPlainItemDataRow(
        id: UUID = UUID.randomUUID(),
        itemNameProvider: OSNameProvider = DefaultNameProvider(null),
        icon: ByteArray? = null,
        color: Color? = null,
        identifier: String? = null,
    ): PlainItemDataRow {
        return PlainItemDataRow(id, itemNameProvider, icon, color, identifier?.let(LbcTextSpec::Raw)) { listOf() }
    }

    fun createPlainItemDataRow(
        size: Int,
        id: (Int) -> UUID = { UUID.randomUUID() },
        itemNameProvider: (Int) -> OSNameProvider = { DefaultNameProvider(null) },
        icon: (Int) -> ByteArray? = { null },
        color: (Int) -> Color? = { null },
        identifier: LbcTextSpec? = null,
        actions: (Int) -> (suspend () -> List<SafeItemAction>)? = { null },
    ): List<PlainItemDataRow> {
        return (0 until size).map {
            PlainItemDataRow(
                id = id(it),
                itemNameProvider = itemNameProvider(it),
                icon = icon(it),
                color = color(it),
                identifier = identifier,
                actions = actions(it),
            )
        }
    }

    fun createPlainItemData(
        size: Int,
        id: (Int) -> UUID = { UUID.randomUUID() },
        itemNameProvider: (Int) -> OSNameProvider = { DefaultNameProvider(null) },
        icon: (Int) -> ByteArray? = { null },
        color: (Int) -> Color? = { null },
        identifier: (Int) -> LbcTextSpec? = { null },
        actions: (Int) -> (suspend () -> List<SafeItemAction>)? = { null },
        itemsLayout: ItemsLayout = ItemsLayout.from(OSTestConfig.itemLayouts),
    ): List<PlainItemData> {
        return when (itemsLayout) {
            ItemsLayout.Grid -> (0 until size).map {
                PlainItemDataDefault(
                    id = id(it),
                    itemNameProvider = itemNameProvider(it),
                    icon = icon(it),
                    color = color(it),
                    actions = actions(it),
                )
            }
            ItemsLayout.List -> (0 until size).map {
                PlainItemDataRow(
                    id = id(it),
                    itemNameProvider = itemNameProvider(it),
                    icon = icon(it),
                    color = color(it),
                    actions = actions(it),
                    identifier = identifier(it),
                )
            }
        }
    }

    fun createItemFieldPhotoData(
        fileId: String,
        name: String? = UUID.randomUUID().toString(),
    ) = createItemFieldData(
        value = "$fileId|jpeg",
        kind = SafeItemFieldKind.Photo,
        name = name,
    )

    fun createItemFieldFileData(
        fileId: String,
        name: String? = UUID.randomUUID().toString(),
    ) = createItemFieldData(
        value = "$fileId|test",
        kind = SafeItemFieldKind.File,
        name = name,
    )

    fun loadedPagingStates() = LoadStates(
        LoadState.NotLoading(endOfPaginationReached = true),
        LoadState.NotLoading(endOfPaginationReached = true),
        LoadState.NotLoading(endOfPaginationReached = true),
    )

    fun waitEmojiInit() {
        runBlocking {
            var loadState = EmojiCompat.get().loadState
            while (loadState == EmojiCompat.LOAD_STATE_LOADING) {
                delay(50)
                loadState = EmojiCompat.get().loadState
            }
            if (loadState != EmojiCompat.LOAD_STATE_SUCCEEDED) {
                println("Fail to initialize EmojiCompat. State = $loadState")
            }
        }
    }
}

internal suspend fun CreateContactUseCase.test(
    id: DoubleRatchetUUID = DoubleRatchetUUID(UUID.randomUUID()),
    name: String = UUID.randomUUID().toString(),
    sharedKey: ByteArray? = null,
    sharedConversationId: DoubleRatchetUUID = DoubleRatchetUUID(UUID.randomUUID()),
    messageSharingMode: MessageSharingMode = MessageSharingMode.CypherText,
): DoubleRatchetUUID {
    invoke(PlainContact(id, name, sharedKey, sharedConversationId, messageSharingMode))
    return id
}

private val actionResField: Field by lazyFast {
    val clazz = Class.forName("${LbcTextSpec::class.qualifiedName}\$StringResource")
    clazz.getDeclaredField("id").also {
        it.isAccessible = true
    }
}

internal fun Context.getLbcTextSpecResString(textSpec: LbcTextSpec): String = getString(actionResField.getInt(textSpec))

/**
 * Log after a semantic action. Sometimes there is exception during test that does not point to our code.
 * This method can be used as debugging tool for flaky tests 🤡.
 * Example:
 * ```
 * hasText(getString(OSString.safeItemDetail_actionCard_move))
 *      .waitAndPrintRootToCacheDir( printRule, "_move_action")
 *      .consoleLog("printToCacheDir")
 *      .performScrollTo()
 *      .consoleLog("scrollTo")
 *      .performClick()
 * ```
 * Will return a default output like this:
 * ConsoleTestLog - On node with TestTag SelectMoveDestinationScreen - isRoot? false - printToCacheDir
 * ConsoleTestLog - On node with TestTag SelectMoveDestinationScreen - isRoot? false - scrollTo
 */
internal fun SemanticsNodeInteraction.consoleLog(message: String, tag: String = "ConsoleTestLog"): SemanticsNodeInteraction {
    val semanticsNode = fetchSemanticsNode()
    val config = semanticsNode.config
    val (type, value) = when {
        config.getOrNull(SemanticsProperties.Text) != null -> SemanticsProperties.Text.name to config[SemanticsProperties.Text]
        config.getOrNull(SemanticsProperties.ContentDescription) != null ->
            SemanticsProperties.ContentDescription.name to config[SemanticsProperties.ContentDescription]

        config.getOrNull(SemanticsProperties.TestTag) != null ->
            SemanticsProperties.TestTag.name to config[SemanticsProperties.TestTag]

        else -> throw IllegalArgumentException("Not handled")
    }
    println("$tag - On node with $type $value - isRoot? ${semanticsNode.isRoot} - $message")
    return this
}

internal fun hasDrawable(@DrawableRes id: Int): SemanticsMatcher =
    SemanticsMatcher.expectValue(DrawableIdProperty, id)
