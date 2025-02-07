package studio.lunabee.onesafe

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.app.assist.AssistStructure.ViewNode
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillContext
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.InlinePresentation
import android.service.autofill.Presentations
import android.service.autofill.SaveCallback
import android.service.autofill.SaveInfo
import android.service.autofill.SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE
import android.service.autofill.SaveRequest
import android.util.Size
import android.view.View
import android.view.autofill.AutofillId
import android.view.inputmethod.InlineSuggestionsRequest
import android.widget.RemoteViews
import android.widget.inline.InlinePresentationSpec
import androidx.annotation.RequiresApi
import androidx.autofill.inline.UiVersions
import androidx.autofill.inline.v1.InlineSuggestionUi
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import studio.lunabee.onesafe.commonui.OSMipmap
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.autofill.AutoFillActivity
import studio.lunabee.onesafe.feature.autofill.AutoFillActivity.Companion.IdentifierFieldKey
import studio.lunabee.onesafe.feature.autofill.AutoFillActivity.Companion.PasswordFieldIdKey
import studio.lunabee.onesafe.feature.autofill.AutoFillFields
import studio.lunabee.onesafe.feature.autofill.AutoFillHelper
import java.util.stream.Collectors.toList
import studio.lunabee.onesafe.R as AppR

// TODO implement test  with Maestro: https://www.notion.so/lunabeestudio/Test-Autofill-Avec-Maestro-20cb3f4b76ea4ea18a314cb4b00bd236
@RequiresApi(Build.VERSION_CODES.O)
class OSAutoFillService : AutofillService() {

    var clientDomain: String? = null

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback,
    ) {
        val fillContexts: List<FillContext> = request.fillContexts
        val clientPackageName: String = fillContexts[fillContexts.size - 1].structure.activityComponent.packageName

        val structures: List<AssistStructure> = fillContexts.stream().map(FillContext::getStructure).collect(toList())
        val fields: Map<AutoFillFields, AutofillId?> = getAutofillableFields(structures)

        val inlinePresentationSizes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getInlinePresentationSizes(request.inlineSuggestionsRequest)
        } else {
            InlinePresentationNullSizePair
        }

        // For now we propose to fill out form if identifier and password fields are displayed
        if (fields.containsKey(AutoFillFields.Password) && fields.containsKey(AutoFillFields.Identifier)) {
            val fillResponse = buildRequireOSAuthenticationResponse(
                fields,
                clientPackageName,
                inlinePresentationSizes,
            )
            callback.onSuccess(fillResponse)
        } else {
            callback.onSuccess(null)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun getInlinePresentationSizes(request: InlineSuggestionsRequest?): Pair<Size, Size> {
        return if (request?.inlinePresentationSpecs?.firstOrNull() != null) {
            val sizes = request.inlinePresentationSpecs.first()
            sizes.minSize to sizes.maxSize
        } else {
            InlinePresentationNullSizePair
        }
    }

    private fun buildRequireOSAuthenticationResponse(
        fields: Map<AutoFillFields, AutofillId?>,
        clientPackageName: String,
        inlinePresentationSizes: Pair<Size, Size>,
    ): FillResponse {
        val authIntentSender = getAuthIntent().intentSender
        val authPresentation = getRequireAuthPresentation()

        val responseBuilder = FillResponse.Builder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            responseBuilder.setAuthentication(
                fields.values.toTypedArray(),
                authIntentSender,
                Presentations.Builder().setMenuPresentation(authPresentation).build(),
            )
        } else {
            @Suppress("DEPRECATION")
            responseBuilder.setAuthentication(fields.values.toTypedArray(), authIntentSender, authPresentation)
        }

        return FillResponse.Builder()
            .setOSAuthentication(
                fields = fields,
                inlinePresentationSizes = inlinePresentationSizes,
            )
            .setOSClientState(clientPackageName, fields)
            .build()
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getAuthIntent(): PendingIntent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this,
                AuthActivityRequestCode,
                AutoFillActivity.getIntent(this),
                PendingIntent.FLAG_MUTABLE,
            )
        } else {
            PendingIntent.getActivity(
                this,
                AuthActivityRequestCode,
                AutoFillActivity.getIntent(this),
                PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

    private fun getRequireAuthPresentation(): RemoteViews =
        RemoteViews(packageName, AppR.layout.autofill_suggestion).apply {
            setTextViewText(AppR.id.label, getString(OSString.autofill_startServiceLabel))
        }

    /**
     * Add the need to authentication to the FillResponse.Builder.
     */
    private fun FillResponse.Builder.setOSAuthentication(
        fields: Map<AutoFillFields, AutofillId?>,
        inlinePresentationSizes: Pair<Size, Size>,
    ): FillResponse.Builder {
        val authPendingIntent = getAuthIntent()
        val authPresentation = getRequireAuthPresentation()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val inlineSuggestionUi = InlineSuggestionUi.newContentBuilder(getAuthIntent())
                .setStartIcon(Icon.createWithResource(this@OSAutoFillService, OSMipmap.ic_launcher_foreground))
                .setTitle(getString(OSString.autofill_startServiceLabel))
                .build()

            @SuppressLint("RestrictedApi")
            val inlinePresentation = InlinePresentation(
                inlineSuggestionUi.slice,
                InlinePresentationSpec.Builder(inlinePresentationSizes.first, inlinePresentationSizes.second)
                    .setStyle(
                        UiVersions.newStylesBuilder().addStyle(InlineSuggestionUi.newStyleBuilder().build()).build(),
                    )
                    .build(),
                false,
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                this.setAuthentication(
                    fields.values.toTypedArray(),
                    authPendingIntent.intentSender,
                    Presentations.Builder()
                        .setMenuPresentation(authPresentation)
                        .setInlinePresentation(inlinePresentation)
                        .build(),
                )
            } else {
                @Suppress("DEPRECATION")
                this.setAuthentication(
                    fields.values.toTypedArray(),
                    authPendingIntent.intentSender,
                    authPresentation,
                    inlinePresentation,
                )
                    .setSaveInfo(
                        SaveInfo.Builder(
                            SaveInfo.SAVE_DATA_TYPE_USERNAME or SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                            (fields.values.toTypedArray() as Array<out AutofillId>),
                        ).setFlags(FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
                            .build(),
                    )
            }
        } else {
            @Suppress("DEPRECATION")
            this.setAuthentication(fields.values.toTypedArray(), authPendingIntent.intentSender, authPresentation)
        }
    }

    /**
     * Add the necessary information to pass to the AutoFillAuthActivity to the FillResponse.Builder
     */
    private fun FillResponse.Builder.setOSClientState(
        clientPackageName: String,
        fields: Map<AutoFillFields, AutofillId?>,
    ): FillResponse.Builder {
        return setClientState(
            bundleOf(
                AutoFillActivity.CallerDomainKey to clientDomain,
                AutoFillActivity.CallerPackageKey to clientPackageName,
                AutoFillActivity.IdentifierFieldKey to fields[AutoFillFields.Identifier],
                AutoFillActivity.PasswordFieldIdKey to fields[AutoFillFields.Password],
                AutoFillActivity.SaveCredentialsKey to true,
            ),
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val structures: List<AssistStructure> = request.fillContexts.stream().map(FillContext::getStructure).collect(toList())
        getAutofillableFields(structures)

        val fillContexts: List<FillContext> = request.fillContexts

        val clientPackageName: String = fillContexts[fillContexts.size - 1].structure.activityComponent.packageName

        val clientState: Bundle? = request.clientState
        val usernameId: AutofillId? = clientState?.let { BundleCompat.getParcelable(it, IdentifierFieldKey, AutofillId::class.java) }
        val passwordId: AutofillId? = clientState?.let { BundleCompat.getParcelable(it, PasswordFieldIdKey, AutofillId::class.java) }

        val usernameNode: ViewNode? = findNodeByAutofillId(structures, usernameId)
        val username: String = usernameNode?.autofillValue?.textValue?.toString().orEmpty()

        val passwordNode: ViewNode? = findNodeByAutofillId(structures, passwordId)
        val password: String = passwordNode?.autofillValue?.textValue?.toString().orEmpty()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            callback.onSuccess(
                PendingIntent.getActivity(
                    this,
                    AuthActivityRequestCode,
                    AutoFillActivity.getIntent(
                        this,
                        username,
                        password,
                        clientPackageName,
                        clientDomain.orEmpty(),
                        true,
                    ),
                    PendingIntent.FLAG_IMMUTABLE,
                ).intentSender,
            )
        }
    }

    @Suppress("ReturnCount")
    private fun findNodeByAutofillId(structures: List<AssistStructure>, id: AutofillId?): ViewNode? {
        structures.forEach { structure ->
            for (windowNodeIndex in 0 until structure.windowNodeCount) {
                val node = structure.getWindowNodeAt(windowNodeIndex).rootViewNode
                if (node.autofillId == id) {
                    return node
                }
                val childNode = getNodeAutofillId(node, id)
                if (childNode != null) {
                    return childNode
                }
            }
        }

        return null
    }

    @Suppress("ReturnCount")
    private fun getNodeAutofillId(node: ViewNode, autofillId: AutofillId?): ViewNode? {
        val childrenSize = node.childCount
        repeat(childrenSize) { index ->
            val childNode = node.getChildAt(index)
            if (childNode?.autofillId == autofillId) {
                return childNode
            }

            val findNode = getNodeAutofillId(node.getChildAt(index), autofillId)
            if (findNode != null) {
                return findNode
            }
        }
        return null
    }

    /**
     * Parses the [AssistStructure] representing the activity being autofilled, and returns a
     * map of autofillable fields (represented by their autofill ids) mapped by the hint associate
     * with them.
     *
     *
     * An autofillable field is a [AssistStructure.ViewNode]
     */
    private fun getAutofillableFields(structures: List<AssistStructure>): Map<AutoFillFields, AutofillId?> {
        val fields: MutableMap<AutoFillFields, AutofillId?> = mutableMapOf()
        structures.forEach { assistStructure ->
            val windowNodeCount = assistStructure.windowNodeCount
            for (windowNodeIndex in 0 until windowNodeCount) {
                val node = assistStructure.getWindowNodeAt(windowNodeIndex).rootViewNode
                if (!node.webDomain.isNullOrEmpty()) {
                    clientDomain = node.webDomain.orEmpty()
                }
                addAutofillableFields(fields, node)
            }
        }
        return fields
    }

    /**
     * Adds any autofillable view from the [AssistStructure.ViewNode] and its descendants to the map.
     */
    private fun addAutofillableFields(fields: MutableMap<AutoFillFields, AutofillId?>, node: ViewNode) {
        if (!node.webDomain.isNullOrEmpty()) {
            clientDomain = node.webDomain.orEmpty()
        }
        val type = node.autofillType
        if (type == View.AUTOFILL_TYPE_TEXT) {
            val hint: String? = AutoFillHelper.getHint(node, this)
            if (hint != null) {
                val id = node.autofillId
                if (!fields.containsKey(AutoFillHelper.getAutoFillFieldFromHint(hint, this))) {
                    fields[AutoFillHelper.getAutoFillFieldFromHint(hint, this)] = id
                }
            }
        }
        val childrenSize = node.childCount
        repeat(childrenSize) { index ->
            addAutofillableFields(fields, node.getChildAt(index))
        }
    }

    companion object {
        private const val AuthActivityRequestCode: Int = 0
        private val InlinePresentationNullSizePair: Pair<Size, Size> = Size(0, 0) to Size(0, 0)
    }
}
