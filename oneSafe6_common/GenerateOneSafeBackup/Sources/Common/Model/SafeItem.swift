//
//  SafeItem.swift
//  oneSafe
//
//  Created by Lunabee Studio (Nicolas) on 28/05/2021 - 10:34.
//  Copyright © 2021 Lunabee Studio. All rights reserved.
//

import Foundation

struct SafeItem {
    var id: String
    var encName: Data?
    var encColor: Data?
    var iconId: String?
    var parentId: String?
    var deletedParentId: String?
    var isFavorite: Bool
    var createdAt: Date
    var updatedAt: Date
    var deletedAt: Date?
    var position: Double

    init(id: String = UUID().uuidStringV4,
                encName: Data? = nil,
                encColor: Data? = nil,
                iconId: String? = nil,
                parentId: String? = nil,
                deletedParentId: String? = nil,
                isFavorite: Bool = false,
                createdAt: Date = Date(),
                updatedAt: Date = Date(),
                deletedAt: Date? = nil,
                position: Double = 0) {
        self.id = id
        self.encName = encName
        self.encColor = encColor
        self.iconId = iconId
        self.parentId = parentId
        self.deletedParentId = deletedParentId
        self.isFavorite = isFavorite
        self.createdAt = createdAt
        self.updatedAt = updatedAt
        self.deletedAt = deletedAt
        self.position = position
    }
}

extension SafeItem: Hashable {
    static func == (lhs: SafeItem, rhs: SafeItem) -> Bool {
        lhs.id == rhs.id && lhs.updatedAt == rhs.updatedAt
    }
    
    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
        hasher.combine(updatedAt)
    }
}
