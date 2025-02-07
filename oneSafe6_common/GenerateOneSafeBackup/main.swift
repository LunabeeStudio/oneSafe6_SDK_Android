#!/usr/bin/swift sh

import Foundation
import SwiftProtobuf // https://github.com/apple/swift-protobuf.git == 1.20.3
import ZIPFoundation  // git@github.com:weichsel/ZIPFoundation.git == 0.9.15
import ArgumentParser // https://github.com/apple/swift-argument-parser == 1.2.2

struct BackupExportOptions: ParsableArguments {
    @Option(name: .shortAndLong,
            help: ArgumentHelp("The Archive Specification version <v> to use. Available versions: 1.",
                               valueName: "v"))
    var archiveVersion: Int

    @Option(name: .shortAndLong,
            help: ArgumentHelp("The password to use to create the backup.",
                               valueName: "p"))
    var password: String = "a"

    @Option(name: [.long, .customShort("c")],
            help: ArgumentHelp("Use <n> iterations for the PBKDF2 derivation.",
                               discussion: "Used when deriving the backup password to get the masterkey.",
                               valueName: "n"))
    var iterationsCount: Int = 120_000

    @Option(name: .shortAndLong, help: ArgumentHelp("Creates <n> root items (visible on the Home).", valueName: "n"))
    var itemsCount: Int = 1

    @Option(name: .shortAndLong, help: ArgumentHelp("Writes the generated backup to the dictory at path <p>.", valueName: "p"))
    var outputDirectory: String = "./output"

    @Option(name: .shortAndLong, help: ArgumentHelp("If this value is greater than 0, an item with <n> children will be added to the generated backup .", valueName: "n"))
    var bigItemChildrenCount: Int = 0

    @Option(name: .shortAndLong, help: ArgumentHelp("If this value is greater than 0, an item with <n> descendance levels, each containing only one child, will be added to the generated backup .", valueName: "n"))
    var deepItemDescendanceLevelsCount: Int = 0

    @Flag(name: .shortAndLong, help: ArgumentHelp("Add a recursive item (referencing itself as parent)", valueName: "r"))
    var recursiveItem: Bool = false
}

let options: BackupExportOptions = .parseOrExit()

func recursivelyGenerateItemDescendance(parent: Item, remainingChildren: [Item], processedChildren: [Item] = []) -> [Item] {
    if remainingChildren.isEmpty {
        return processedChildren
    } else {
        var childrenToProcess: [Item] = remainingChildren
        var nextChildrenToProcess: Item = childrenToProcess.removeFirst()
        nextChildrenToProcess.id = UUID().uuidStringV4
        nextChildrenToProcess.parentId = parent.id
        nextChildrenToProcess.addFields = false
        var doneChildren: [Item] = processedChildren
        doneChildren.append(nextChildrenToProcess)
        return recursivelyGenerateItemDescendance(parent: nextChildrenToProcess, remainingChildren: childrenToProcess, processedChildren: doneChildren)
    }
}

Task {
    do {
        let itemsSet: [Item] = ItemsFactory.items()
        var templateItems: [Item] = itemsSet
        while templateItems.count < options.itemsCount {
            templateItems.append(contentsOf: itemsSet)
        }
        templateItems = [Item](templateItems.prefix(options.itemsCount))
        let templateFields: [Field] = FieldsFactory.fields()

        if options.bigItemChildrenCount > 0 {
            var bigItemChildrenItems: [Item] = itemsSet
            while bigItemChildrenItems.count < options.bigItemChildrenCount {
                bigItemChildrenItems.append(contentsOf: itemsSet)
            }
            bigItemChildrenItems = [Item](bigItemChildrenItems.prefix(options.bigItemChildrenCount))
            let bigItemId: String = UUID().uuidStringV4
            let bigItem: Item = .init(id: bigItemId, parentId: nil, title: "Big item", url: nil, iconId: "bigItemIcon", color: "000000", addFields: false)
            bigItemChildrenItems = bigItemChildrenItems.map {
                var item: Item = $0
                item.parentId = bigItemId
                item.addFields = false
                return item
            }
            templateItems.append(contentsOf: bigItemChildrenItems)
            templateItems.insert(bigItem, at: 0)
        }

        if options.deepItemDescendanceLevelsCount > 0 {
            var deepItemChildrenItems: [Item] = itemsSet
            while deepItemChildrenItems.count < options.bigItemChildrenCount {
                deepItemChildrenItems.append(contentsOf: itemsSet)
            }
            deepItemChildrenItems = [Item](deepItemChildrenItems.prefix(options.deepItemDescendanceLevelsCount))
            let deepItemId: String = UUID().uuidStringV4
            let deepItem: Item = .init(id: deepItemId, parentId: nil, title: "Deep item", url: nil, iconId: "deepItemIcon", color: "000000", addFields: false)
            deepItemChildrenItems = recursivelyGenerateItemDescendance(parent: deepItem, remainingChildren: deepItemChildrenItems)
            templateItems.append(contentsOf: deepItemChildrenItems)
            templateItems.insert(deepItem, at: 0)
        }

        if options.recursiveItem {
            var recursiveItem: [Item] = itemsSet
            let recItemId: String = UUID().uuidStringV4
            let recItem: Item = .init(id: recItemId, parentId: recItemId, title: "☣️ Recursive item", url: nil, iconId: nil, color: "000000", addFields: false, isFavorite: true)
            templateItems.insert(recItem, at: 0)
        }

        let archiveUrl: URL = try await ArchiveManager.backup(templateItems: templateItems,
                                                              templateFields: templateFields,
                                                              password: options.password,
                                                              iterationsCount: options.iterationsCount,
                                                              itemCount: options.itemsCount,
                                                              outputDirectory: options.outputDirectory)
        print(archiveUrl.absoluteString)
        exit(0)
    } catch {
        print(error)
        exit(1)
    }
}

RunLoop.main.run()
