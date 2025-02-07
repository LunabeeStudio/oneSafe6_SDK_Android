enum AppError: Error {
    // MARK: - Crypto
    case cryptoUnknown
    case cryptoWrongCryptoKeyLength
    case cryptoNoUsername
    case cryptoWrongPassword
    case cryptoWrongPasswordFormat
    case cryptoKeyDerivationError
    case cryptoNoMasterKeyLoaded
    case cryptoNoBiometryMasterKey
    case cryptoBiometryMasterKeySaveError
    case cryptoNoMasterSalt
    case cryptoNoSearchIndexSalt
    case cryptoNoReencryptionOriginMasterKey
    case cryptoNoReencryptionDestinationMasterKey
    case cryptoBadBase64EncodedString
    case cryptoEncryptionError
    case cryptoItemKeyIdCreationError
    case cryptoBadUTF8String
    case cryptoNoKeyForEncryption
    case cryptoNoKeyForDecryption

    // MARK: - Archive
    case archiveDataNoUsernameProvided
    case archiveDataEncryptionFailed
    case archiveDataDecryptionFailed
    case archiveDataNoDecryptionPasswordProvided
    case archiveDataNoMasterKeyForImport
    case archiveDataNoUsernameDataForImport
    case archiveNoKeyInArchive

    // MARK: - Icon
    case iconNoFileData
}
