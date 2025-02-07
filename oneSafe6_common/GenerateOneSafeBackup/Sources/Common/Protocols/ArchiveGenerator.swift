//
//  SafeItemKey.swift
//  Model
//
//  Created by Lunabee Studio (Nicolas) on 28/05/2021 - 10:34.
//  Copyright © 2021 Lunabee Studio. All rights reserved.
//

import Foundation

protocol ArchiveGenerator {
    static func backup(templateItems: [Item], templateFields: [Field], password: String, iterationsCount: Int, itemCount: Int, outputDirectory: String) async throws -> URL
}
