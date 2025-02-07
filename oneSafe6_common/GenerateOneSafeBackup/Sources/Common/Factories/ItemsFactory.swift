import Foundation

final class ItemsFactory {
    static func items() -> [Item] {
        let data: Data = try! .init(contentsOf: URL(fileURLWithPath: "./Resources/websites.json"))
        return try! JSONDecoder().decode([Item].self, from: data)
    }
}

struct Item: Decodable {
    var id: String?
    var parentId: String?
    let title: String
    var url: String?
    let iconId: String?
    let color: String
    var addFields: Bool = true
    var isFavorite: Bool = false

    enum CodingKeys: String, CodingKey {
        case title
        case url
        case iconId
        case color
    }
}
