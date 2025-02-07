import Foundation

extension UUID {
    var uuidStringV4: String { uuidString.lowercased() }
}

extension Data {
    func string(using encoding: String.Encoding) -> String? { String(data: self, encoding: encoding) }
}

extension Optional where Wrapped == String {
    var orEmpty: String {
        switch self {
        case .some(let value):
            return value
        case .none:
            return ""
        }
    }
}

extension AsyncSequence {
    func collect() async rethrows -> [Element] {
        try await reduce(into: [Element]()) { $0.append($1) }
    }
}

extension URL {
    func appendingDirectory(path: String) throws -> URL {
        let directoryUrl: URL = appending(path: path)
        if !FileManager.default.fileExists(atPath: directoryUrl.path, isDirectory: nil) {
            try FileManager.default.createDirectory(at: directoryUrl, withIntermediateDirectories: false, attributes: nil)
        }
        return directoryUrl
    }
}
