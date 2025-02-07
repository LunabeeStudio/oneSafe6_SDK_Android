package studio.lunabee.onesafe.debug.item

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.bubbles.ui.contact.detail.ContactDetailDestination
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.molecule.OSRow

private val corruptContact = LbcTextSpec.Raw("🤑 Corrupt contact ☢️")

@Composable
fun DebugBubblesEntries(
    modifier: Modifier,
    imeNotify: () -> Unit,
    createContact: () -> Unit,
    closeDrawer: () -> Unit,
    mainBackStackEntry: NavBackStackEntry?,
    onCorruptLastMessage: (String) -> Unit,
    onCorruptContact: (String) -> Unit,
) {
    OSRow(
        text = LbcTextSpec.Raw("📢 Enqueue notification"),
        modifier = modifier.clickable(onClick = imeNotify),
    )
    OSRow(
        text = LbcTextSpec.Raw("🙋‍♀️ Create dummy contact"),
        modifier = modifier.clickable {
            createContact()
            closeDrawer()
        },
    )
    val route = mainBackStackEntry?.destination?.route
    if (route == WriteMessageDestination.route) {
        mainBackStackEntry.arguments?.getString(WriteMessageDestination.ContactIdArg)?.let {
            OSRow(
                text = LbcTextSpec.Raw("🤑 Corrupt last message ☢️"),
                modifier = modifier.clickable {
                    onCorruptLastMessage(it)
                    closeDrawer()
                },
            )
            OSRow(
                text = corruptContact,
                modifier = modifier.clickable {
                    onCorruptContact(it)
                    closeDrawer()
                },
            )
        }
    }
    if (route == ContactDetailDestination.route) {
        mainBackStackEntry.arguments?.getString(ContactDetailDestination.ContactIdArg)?.let {
            OSRow(
                text = corruptContact,
                modifier = modifier.clickable {
                    onCorruptContact(it)
                    closeDrawer()
                },
            )
        }
    }
}
