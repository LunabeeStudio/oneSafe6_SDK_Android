package studio.lunabee.onesafe.feature.autofill

import android.app.assist.AssistStructure.ViewNode
import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import studio.lunabee.onesafe.commonui.OSString
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
object AutoFillHelper {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getHint(node: ViewNode, context: Context): String? {
        val autofillHints = node.autofillHints
        var finalHint: String? = null
        if (autofillHints != null && autofillHints.isNotEmpty()) {
            finalHint = containsPossibleAutofillHint(autofillHints[0], context)
        } else {
            if (node.idEntry != null) {
                finalHint = containsPossibleAutofillHint(node.idEntry, context)
            }
        }
        if (finalHint == null && node.hint != null) {
            finalHint = containsPossibleAutofillHint(node.hint, context)
        }
        return finalHint
    }

    private fun containsPossibleAutofillHint(hint: String?, context: Context): String? {
        if (hint == null) {
            return null
        }
        for (possibleHint in autoFillHints(context)) {
            if (hint.lowercase(Locale.getDefault()).contains(possibleHint.lowercase(Locale.getDefault()))) {
                return possibleHint
            }
        }
        return null
    }

    private fun autoFillHints(context: Context): List<String> = listOf(
        View.AUTOFILL_HINT_EMAIL_ADDRESS,
        View.AUTOFILL_HINT_PASSWORD,
        View.AUTOFILL_HINT_USERNAME,
        View.AUTOFILL_HINT_PHONE,
        context.getString(OSString.autofill_hints_email),
        context.getString(OSString.autofill_hints_emailCustom),
        context.getString(OSString.autofill_hints_password),
    )

    fun getAutoFillFieldFromHint(hint: String, context: Context): AutoFillFields {
        return when (hint) {
            View.AUTOFILL_HINT_EMAIL_ADDRESS,
            View.AUTOFILL_HINT_PHONE,
            View.AUTOFILL_HINT_USERNAME,
            context.getString(OSString.autofill_hints_email),
            context.getString(OSString.autofill_hints_emailCustom),
            -> AutoFillFields.Identifier
            View.AUTOFILL_HINT_PASSWORD,
            context.getString(OSString.autofill_hints_password),
            -> AutoFillFields.Password
            else -> AutoFillFields.Unknown
        }
    }
}
