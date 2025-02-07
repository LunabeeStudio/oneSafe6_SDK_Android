//
//  CoreCrypto.swift
//  CoreCrypto
//
//  Created by Lunabee Studio (Nicolas) on 10/02/2023 - 14:29.
//  Copyright © 2023 Lunabee Studio. All rights reserved.
//

import Foundation
import CryptoKit
import CommonCrypto

enum CoreCrypto {
    static func derive(password: Data, salt: Data, iterationsCount: Int) throws -> Data {
        guard let password = String(data: password, encoding: .utf8) else { throw AppError.cryptoWrongPasswordFormat }
        return try derive(password: password, salt: salt, iterationsCount: iterationsCount)
    }

    static func derive(password: String, salt: Data, iterationsCount: Int) throws -> Data {
        try pbkdf2(password: password, salt: salt, keyByteCount: 32, rounds: iterationsCount)
    }

    static func generateKey() -> Data {
        SymmetricKey(size: .bits256).withUnsafeBytes { Data($0) }
    }
}

// MARK: - Encryption/decryption functions -
extension CoreCrypto {
    static func encrypt(value: String, key: Data, authenticating: Data? = nil) throws -> Data {
        try encrypt(value: Data(value.utf8), key: key, authenticating: authenticating)
    }

    static func encrypt(value: Data, key: Data, authenticating: Data? = nil) throws -> Data {
        try encrypt(data: value, key: SymmetricKey(data: key), authenticating: authenticating)
    }

    static func decrypt(value: String, key: Data, authenticating: Data? = nil) throws -> Data {
        try decrypt(value: Data(value.utf8), key: key, authenticating: authenticating)
    }

    static func decrypt(value: Data, key: Data, authenticating: Data? = nil) throws -> Data {
        try decrypt(data: value, key: SymmetricKey(data: key), authenticating: authenticating)
    }
}

// MARK: - Utils -
private extension CoreCrypto {
    static func pbkdf2(password: String, salt: Data, keyByteCount: Int, rounds: Int) throws -> Data {
        let passwordData: Data = Data(password.utf8)

        var derivedKeyData: Data = .init(repeating: 0, count: keyByteCount)
        let derivedCount: Int = derivedKeyData.count

        let derivationStatus: OSStatus = derivedKeyData.withUnsafeMutableBytes { derivedKeyBytes in
            let derivedKeyRawBytes: UnsafeMutablePointer<UInt8>? = derivedKeyBytes.bindMemory(to: UInt8.self).baseAddress
            return salt.withUnsafeBytes { saltBytes in
                let rawBytes: UnsafePointer<UInt8>? = saltBytes.bindMemory(to: UInt8.self).baseAddress
                return CCKeyDerivationPBKDF(CCPBKDFAlgorithm(kCCPBKDF2),
                                            password,
                                            passwordData.count,
                                            rawBytes,
                                            salt.count,
                                            CCPBKDFAlgorithm(kCCPRFHmacAlgSHA512),
                                            UInt32(rounds),
                                            derivedKeyRawBytes,
                                            derivedCount)
            }
        }
        if derivationStatus == kCCSuccess {
            return derivedKeyData
        } else {
            throw AppError.cryptoKeyDerivationError
        }
    }
}

// MARK: - Low level cryptographic functions -
private extension CoreCrypto {
    static func encrypt(string: String, key: SymmetricKey, authenticating: Data? = nil) throws -> String {
        guard let data = string.data(using: .utf8) else { throw AppError.cryptoBadUTF8String }
        return try encrypt(data: data, key: key, authenticating: authenticating).base64EncodedString()
    }

    static func encrypt(data: Data, key: SymmetricKey, authenticating: Data? = nil) throws -> Data {
        if let authenticating {
            return try ChaChaPoly.seal(data, using: key, authenticating: authenticating).combined
        } else {
            return try ChaChaPoly.seal(data, using: key).combined
        }
    }

    static func decrypt(string: String, key: SymmetricKey, authenticating: Data? = nil) throws -> String {
        guard let data = Data(base64Encoded: string) else { throw AppError.cryptoBadUTF8String }
        let decryptedData: Data = try decrypt(data: data, key: key, authenticating: authenticating)
        return decryptedData.string(using: .utf8).orEmpty
    }

    static func decrypt(data: Data, key: SymmetricKey, authenticating: Data? = nil) throws -> Data {
        let sealedBox: ChaChaPoly.SealedBox = try .init(combined: data)
        if let authenticating {
            return try ChaChaPoly.open(sealedBox, using: key, authenticating: authenticating)
        } else {
            return try ChaChaPoly.open(sealedBox, using: key)
        }
    }
}
