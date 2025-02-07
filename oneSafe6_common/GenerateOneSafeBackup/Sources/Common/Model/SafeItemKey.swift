//
//  SafeItemKey.swift
//  Model
//
//  Created by Lunabee Studio (Nicolas) on 28/05/2021 - 10:34.
//  Copyright © 2021 Lunabee Studio. All rights reserved.
//

import Foundation

struct SafeItemKey {
    var id: String
    var value: Data

    init(id: String, value: Data) {
        self.id = id
        self.value = value
    }
}
