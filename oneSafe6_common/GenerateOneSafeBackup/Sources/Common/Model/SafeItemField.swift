//
//  SafeItemField.swift
//  oneSafe
//
//  Created by Lunabee Studio (Nicolas) on 28/05/2021 - 10:34.
//  Copyright © 2021 Lunabee Studio. All rights reserved.
//

import Foundation

struct SafeItemField {
    public enum Kind: String, CaseIterable {
        case text
        case url
        case password
        case note
        case email
        case phone
        case date
        case time
        case dateAndTime
        case number
        case creditCardNumber
        case iban
        case socialSecurityNumber
    }

    var id: String
    var encName: Data?
    var position: Double
    var itemId: String
    var encPlaceholder: Data?
    var encValue: Data?
    var showPrediction: Bool
    var encKind: Data?
    var createdAt: Date
    var updatedAt: Date
    var isItemIdentifier: Bool
    var encFormattingMask: Data?
    var encSecureDisplayMask: Data?
    var isSecured: Bool

    init(id: String,
                encName: Data? = nil,
                position: Double,
                itemId: String,
                encPlaceholder: Data? = nil,
                encValue: Data? = nil,
                showPrediction: Bool = true,
                encKind: Data? = nil,
                createdAt: Date = Date(),
                updatedAt: Date = Date(),
                isItemIdentifier: Bool = false,
                encFormattingMask: Data? = nil,
                encSecureDisplayMask: Data? = nil,
                isSecured: Bool) {
        self.id = id
        self.encName = encName
        self.position = position
        self.itemId = itemId
        self.encPlaceholder = encPlaceholder
        self.encValue = encValue
        self.showPrediction = showPrediction
        self.encKind = encKind
        self.createdAt = createdAt
        self.updatedAt = updatedAt
        self.isItemIdentifier = isItemIdentifier
        self.encFormattingMask = encFormattingMask
        self.encSecureDisplayMask = encSecureDisplayMask
        self.isSecured = isSecured
    }
}

extension SafeItemField: Hashable {
    static func == (lhs: SafeItemField, rhs: SafeItemField) -> Bool {
        lhs.id == rhs.id && lhs.updatedAt == rhs.updatedAt
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(updatedAt)
    }
}
