import Foundation

final class FieldsFactory {
    static func fields() -> [Field] {
        let data: Data = try! .init(contentsOf: URL(fileURLWithPath: "./Resources/fields.json"))
        return try! JSONDecoder().decode([Field].self, from: data)
    }
}

struct Field: Decodable {
    let formattingMask: String?
    let isItemIdentifier: Bool
    let isSecured: Bool
    let kind: String
    let name: String
    let placeholder: String
    let position: Double
    let secureDisplayMask: String?
    let showPrediction: Bool
    let value: String
}
