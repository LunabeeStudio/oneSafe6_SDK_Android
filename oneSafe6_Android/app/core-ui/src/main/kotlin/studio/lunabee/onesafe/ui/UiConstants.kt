package studio.lunabee.onesafe.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.UUID

object UiConstants {

    object Shimmer {
        const val AnimDurationMs: Int = 600
        const val AnimDelayMs: Int = AnimDurationMs / 2
    }

    object Animation {
        const val InitialVelocitySpringSearch: Float = 2.5f
        const val ScaleSearchAnimation: Float = 1.03f
    }

    object Alpha {
        const val Disable: Float = 0.5f
        const val EmojiBackground: Float = 0.2f
    }

    object TestTag {
        const val OSAppBarMenu: String = "OSAppBarMenu"
        const val OSTabs: String = "OSTabs"
        fun tab(idx: Int): String = "tab_$idx"
        const val OSSafeItemImage: String = "OSSafeItemImage"
        const val OSSafeItemText: String = "OSSafeItemText"
        const val OSTrailingIcon: String = "OSTrailingIcon"
        const val OSShimmerSafeItem: String = "OSShimmerSafeItem"

        object Screen {
            const val ExportGetArchiveScreen: String = "ExportGetArchiveScreen"
            const val Home: String = "HomeScreen"
            const val Login: String = "LoginScreen"
            fun itemDetailsScreen(itemId: UUID): String = "ItemDetailsScreen_$itemId"
            const val ItemFormScreen: String = "ItemFormScreen"
            const val ItemReOrderScreen: String = "ItemReOrderScreen"
            const val ItemDetailsFieldFullScreen: String = "ItemDetailsFieldFullScreen"
            const val Bin: String = "BinScreen"
            const val Favorite: String = "FavoriteScreen"
            const val CongratulationOnBoarding: String = "CongratulationOnBoarding"
            const val BiometricSetup: String = "BiometricSetup"
            const val PasswordCreation: String = "PasswordCreation"
            const val PasswordConfirmation: String = "PasswordConfirmation"
            const val SearchScreen: String = "SearchScreen"
            const val Settings: String = "Settings"
            const val Libraries: String = "Libraries"
            const val ExportAuthScreen: String = "ExportAuthScreen"
            const val ExportDataScreen: String = "ExportDataScreen"
            const val ImportAuthScreen: String = "ImportAuthScreen"
            const val ExportEmptyScreen: String = "ExportEmptyScreen"
            const val ImportFileScreen: String = "ImportFileScreen"
            const val ImportSaveDataScreen: String = "ImportSettingsScreen"
            const val ImportSharingScreen: String = "ImportSharingScreen"
            const val MoveHostScreen: String = "MoveHostScreen"
            const val SelectMoveDestinationScreen: String = "SelectMoveDestinationScreen"
            const val AppPresentationScreen: String = "AppPresentationScreen"
            const val AboutScreen: String = "AboutScreen"
            const val CreditsScreen: String = "CreditsScreen"
            const val AutofillItemsListScreen: String = "AutofillItemsListScreen"
            const val ExtensionSettingsScreen: String = "ExtensionSettingsScreen"
            const val ForceUpgradeScreen: String = "CreditsScreen"
            const val EncryptShareScreen: String = "EncryptShareScreen"
            const val ShareFileScreen: String = "ShareFileScreen"
            const val WrongPasswordScreen: String = "WrongPasswordScreen"
            const val RightPasswordScreen: String = "RightPasswordScreen"
            const val OneSafeKPresentationScreen: String = "OneSafeKPresentationScreen"
            const val OneSafeKAccessibilityScreen: String = "OneSafeKAccessibilityScreen"
            const val OneSafeKKeyboardSelectionScreen: String = "OneSafeKKeyboardSelectionScreen"
            const val EmptyContactScreen: String = "OneSafeKEmptyContactScreen"
            const val FilledContactScreen: String = "OneSafeKFilledContactScreen"
            const val BubblesHomeScreen: String = "BubblesHomeScreen"
            const val BubblesHomeScreenConversationTab: String = "BubblesHomeScreenConversationTab"
            const val BubblesHomeScreenContactTab: String = "BubblesHomeScreenContactTab"
            const val ContactDetailScreen: String = "ContactDetailScreen"
            const val InvitationScreen: String = "InvitationScreen"
            const val InvitationResponseScreen: String = "InvitationResponseScreen"
            const val ScanBarCodeScreen: String = "ScanBarCodeScreen"
            const val CreateContactScreen: String = "CreateContactScreen"
            const val DecryptMessageScreen: String = "DecryptMessageScreen"
            const val OnBoardingBubblesScreen: String = "OnBoardingBubblesScreen"
            const val WriteMessageScreen: String = "WriteMessageScreen"
            const val AutoBackupSettingsScreen: String = "AutoBackupSettingsScreen"
            const val FileViewerScreen: String = "FileViewerScreen"
            const val CameraActivityScreen: String = "CameraActivityScreen"
            const val CipherKeyPromptScreen: String = "CipherKeyPromptScreen"
            const val FinishSetupDatabaseErrorScreen: String = "FinishSetupDatabaseErrorScreen"
            const val LostKeyScreen: String = "LostKeyScreen"
            const val LostKeyExplainScreen: String = "LostKeyExplainScreen"
            const val OverEncryptionExplanationScreen: String = "OverEncryptionExplanationScreen"
            const val OverEncryptionBackupScreen: String = "OverEncryptionBackupScreen"
            const val OverEncryptionKeyScreen: String = "OverEncryptionKeyScreen"
            const val OverEncryptionEnabledScreen: String = "OverEncryptionEnabledScreen"
            const val SendItemBubblesScreen: String = "SendItemBubblesScreen"
            const val BreadcrumbScreen: String = "BreadcrumbScreen"
            const val AutoDestructionOnBoardingScreen: String = "AutoDestructionOnBoardingScreen"
            const val WidgetPanicModeSettingsScreen: String = "WidgetPanicModeSettingsScreen"
        }

        object Item {
            const val FeedbackWarningPreventionClose: String = "FeedbackWarningPreventionClose"
            const val LoginButtonIcon: String = "LoginButtonIcon"
            const val LoginBiometricIcon: String = "LoginBiometricIcon"
            const val LoginPasswordTextField: String = "LoginPasswordTextField"
            const val HomeItemGrid: String = "HomeItemGrid"
            const val HomeItemSectionRow: String = "HomeItemSectionRow"
            const val ItemDetailsTopBar: String = "ItemDetailsTopBar"
            const val ItemDetailsChildrenRow: String = "ItemDetailsChildrenRow"
            const val AddPictureButton: String = "AddPictureButton"
            const val PictureButton: String = "PictureButton"
            const val ColorPickerButton: String = "ColorPickerButton"
            const val ItemDetailsRegularActionCard: String = "ItemDetailsRegularActionCard"
            const val ItemDetailsDangerousActionCard: String = "ItemDetailsDangerousActionCard"
            const val ColorPicker: String = "ColorPicker"
            const val SaveColorButton: String = "SaveColorButton"
            const val UrlMetadataCircularProgressIndicator: String = "UrlMetadataCircularProgressIndicator"
            const val BinItemGrid: String = "BinItemGrid"
            const val SearchTextField: String = "SearchTextField"
            const val SearchLoading: String = "SearchLoading"
            const val PasswordCreationTextField: String = "PasswordCreationTextField"
            const val PasswordConfirmationTextField: String = "PasswordConfirmationTextField"
            const val SelectMoveDestinationItemGrid: String = "SelectMoveDestinationItemGrid"
            const val AppPresentationVerticalPager: String = "AppPresentationVerticalPager"
            const val AppPresentationNextButton: String = "AppPresentationNextButton"
            const val AppPresentationSkipButton: String = "AppPresentationSkipButton"
            const val Slider: String = "Slider"
            const val ItemFormField: String = "ItemFormField"
            const val LinearProgressItem: String = "LinearProgressItem"
            const val GeneratedPasswordText: String = "GeneratedPasswordText"
            const val DatePickerAction: String = "DatePickerAction"
            const val TimePickerAction: String = "TimePickerAction"
            const val GeneratePasswordAction: String = "GeneratePasswordAction"
            const val VisibilityAction: String = "VisibilityAction"
            const val RecentSearchItem: String = "RecentSearchItem"
            const val PasswordStrengthText: String = "PasswordStrengthText"
            const val MoveHereButton: String = "MoveHereButton"
            const val IdentifierLabelText: String = "IdentifierLabelText"
            const val AboutScreenList: String = "AboutScreenList"
            const val OneSafeKStartOnBoardingCard: String = "OneSafeKStartOnBoardingCard"
            const val OneSafeKConfigurationCard: String = "OneSafeKConfigurationCard"
            const val BubblesNoContactCard: String = "BubblesNoContactCard"
            const val WriteMessageTopBar: String = "OneSafeKWriteMessageRecipientCard"
            const val SupportUsCard: String = "SupportUsCard"
            const val ConversationCard: String = "ConversationCard"
            const val DiscoveryItemCard: String = "DiscoveryItemCard"
            const val InvitationList: String = "InvitationList"
            const val NotificationIndicator: String = "NotificationIndicator"
            const val RenameFieldTextField: String = "RenameFieldTextField"
            const val QuickItemActionMenu: String = "QuickItemActionMenu"
            const val FileFieldsCard: String = "FileFieldsCard"
            const val MediaFieldsCard: String = "MediaFieldsCard"
            const val InformationFieldsCard: String = "InformationFieldsCard"
            const val SaveAction: String = "SaveAction"
            const val AutoBackupErrorCard: String = "AutoBackupErrorCard"
            fun fieldActionButton(itemName: String): String = "FieldActionButton-$itemName"
            const val ToggleSwitch: String = "ToggleSwitch"
            const val LoadingSwitch: String = "LoadingSwitch"
            const val AutoBackupEnableCtaCard: String = "AutoBackupEnableCtaCard"
            const val ShutterButton: String = "ShutterButton"
            const val CameraPreviewConfirmButton: String = "CameraPreviewConfirmButton"
            const val CipherKeyTextField: String = "CipherKeyTextField"
            const val BubblesCard: String = "BubblesCard"
            const val HomeEmptyCarousel: String = "HomeEmptyCarousel"
            const val FeedbackWarningPrevention: String = "FeedbackWarningPrevention"
        }

        object BottomSheet {
            const val CreateItemBottomSheet: String = "CreateItemBottomSheet"
            const val ItemImagePickerBottomSheet: String = "ItemImagePickerBottomSheet"
            const val ColorPickerBottomSheet: String = "ColorPickerBottomSheet"
            const val PasswordGeneratorBottomSheet: String = "PasswordGeneratorBottomSheet"
            const val EnterPasswordBottomSheet: String = "EnterPasswordBottomSheet"
            const val IdentifierInfoBottomSheet: String = "IdentifierInfoBottomSheet"
            const val VerifyPasswordBottomSheet: String = "VerifyPasswordBottomSheet"
            const val HelpUsTranslateBottomSheet: String = "HelpUsTranslateBottomSheet"
            const val VerifyPasswordBottomSheetInterval: String = "VerifyPasswordBottomSheetInterval"
            const val AskForSupportBottomSheet: String = "AskForSupportBottomSheet"
            const val ItemDisplayOptionsBottomSheet: String = "ItemDisplayOptionsBottomSheet"
            const val ItemFormNewFieldBottomSheet: String = "ItemFormNewFieldBottomSheet"
            const val AppBetaVersionBottomSheet: String = "AppBetaVersionBottomSheet"
        }

        object BreadCrumb {
            const val OSCreateItemButton: String = "OSCreateItemButton"
            const val BreadCrumbLayout: String = "BreadCrumbLayout"
        }

        object ScrollableContent {
            const val ItemDetailLazyColumn: String = "ItemDetailLazyColumn"
            const val AutoBackupSettingsLazyColumn: String = "AutoBackupSettingsLazyColumn"
            const val SettingsSafeLazyColumn: String = "SettingsSafeLazyColumn"
            const val SettingsGlobalLazyColumn: String = "SettingsGlobalLazyColumn"
            const val SettingsHorizontalPager: String = "SettingsHorizontalPager"
        }

        object Menu {
            const val FieldActionMenu: String = "FieldActionMenu"
        }
    }

    object Text {
        const val MaxLineSize: Int = 3
        const val MaxLineSizeNoteField: Int = 10
        const val MaxLetterNavigationItem: Int = 14
        const val MaxLetterTruncatedNavigationItem: Int = 12
        const val RatioFontSizeReductionResponsiveText: Float = 0.9f
        const val RatioLineHeightReductionResponsiveText: Float = 0.95f
    }

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    object GoogleInternalApi {
        const val DisabledContentAlpha: Float = androidx.compose.material3.tokens.TextButtonTokens.DisabledLabelTextOpacity
        const val DisabledContainerAlpha: Float = androidx.compose.material3.tokens.FilledButtonTokens.DisabledContainerOpacity
        val SpacingBetweenTooltipAndAnchor: Dp = androidx.compose.material3.SpacingBetweenTooltipAndAnchor
        val TonalElevation: Dp = androidx.compose.material3.tokens.RichTooltipTokens.ContainerElevation

        /**
         * Should be [androidx.compose.material3.tokens.RichTooltipTokens.ContainerElevation] but:
         * FIXME https://issuetracker.google.com/issues/329470609
         */
        val ShadowElevation: Dp = 0.dp
    }

    object SnackBar {
        const val ZIndex: Float = 1.0f
    }

    object Tooltip {
        const val DisplayDelay: Long = 200L
    }
}
