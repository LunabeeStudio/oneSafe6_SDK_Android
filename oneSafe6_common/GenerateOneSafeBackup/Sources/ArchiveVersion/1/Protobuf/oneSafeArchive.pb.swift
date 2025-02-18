// DO NOT EDIT.
// swift-format-ignore-file
//
// Generated by the Swift generator plugin for the protocol buffer compiler.
// Source: oneSafeArchive.proto
//
// For information on using the generated types, please see the documentation:
//   https://github.com/apple/swift-protobuf/

import Foundation
import SwiftProtobuf

// If the compiler emits an error on this type, it is because this file
// was generated by a version of the `protoc` Swift plug-in that is
// incompatible with the version of SwiftProtobuf to which you are linking.
// Please ensure that you are building against the same version of the API
// that was used to generate this file.
fileprivate struct _GeneratedWithProtocGenSwiftVersion: SwiftProtobuf.ProtobufAPIVersionCheck {
  struct _2: SwiftProtobuf.ProtobufAPIVersion_2 {}
  typealias Version = _2
}

struct ArchiveMetadata {
  // SwiftProtobuf.Message conformance is added in an extension below. See the
  // `Message` and `Message+*Additions` files in the SwiftProtobuf library for
  // methods supported on all messages.

  var isFromOneSafePlus: Bool = false

  /// The archive spec version. Currently 1.
  var archiveVersion: Int32 = 0

  /// "ios" or "android" (free text for support purpose)
  var fromPlatform: String = String()

  /// Using ISO8601 format like this: 2022-11-29T06:18:08.095Z
  var createdAt: String = String()

  var itemsCount: Int32 = 0

  var archiveKind: ArchiveMetadata.ArchiveKind = .unspecified

  var unknownFields = SwiftProtobuf.UnknownStorage()

  enum ArchiveKind: SwiftProtobuf.Enum {
    typealias RawValue = Int
    case unspecified // = 0
    case backup // = 1
    case sharing // = 2
    case UNRECOGNIZED(Int)

    init() {
      self = .unspecified
    }

    init?(rawValue: Int) {
      switch rawValue {
      case 0: self = .unspecified
      case 1: self = .backup
      case 2: self = .sharing
      default: self = .UNRECOGNIZED(rawValue)
      }
    }

    var rawValue: Int {
      switch self {
      case .unspecified: return 0
      case .backup: return 1
      case .sharing: return 2
      case .UNRECOGNIZED(let i): return i
      }
    }

  }

  init() {}
}

#if swift(>=4.2)

extension ArchiveMetadata.ArchiveKind: CaseIterable {
  // The compiler won't synthesize support with the UNRECOGNIZED case.
  static var allCases: [ArchiveMetadata.ArchiveKind] = [
    .unspecified,
    .backup,
    .sharing,
  ]
}

#endif  // swift(>=4.2)

struct Archive {
  // SwiftProtobuf.Message conformance is added in an extension below. See the
  // `Message` and `Message+*Additions` files in the SwiftProtobuf library for
  // methods supported on all messages.

  var salt: Data = Data()

  var items: [ArchiveSafeItem] = []

  var fields: [ArchiveSafeItemField] = []

  var keys: [ArchiveSafeItemKey] = []

  var unknownFields = SwiftProtobuf.UnknownStorage()

  init() {}
}

struct ArchiveSafeItem {
  // SwiftProtobuf.Message conformance is added in an extension below. See the
  // `Message` and `Message+*Additions` files in the SwiftProtobuf library for
  // methods supported on all messages.

  var id: String = String()

  var encName: Data {
    get {return _encName ?? Data()}
    set {_encName = newValue}
  }
  /// Returns true if `encName` has been explicitly set.
  var hasEncName: Bool {return self._encName != nil}
  /// Clears the value of `encName`. Subsequent reads from it will return its default value.
  mutating func clearEncName() {self._encName = nil}

  var encColor: Data {
    get {return _encColor ?? Data()}
    set {_encColor = newValue}
  }
  /// Returns true if `encColor` has been explicitly set.
  var hasEncColor: Bool {return self._encColor != nil}
  /// Clears the value of `encColor`. Subsequent reads from it will return its default value.
  mutating func clearEncColor() {self._encColor = nil}

  var iconID: String {
    get {return _iconID ?? String()}
    set {_iconID = newValue}
  }
  /// Returns true if `iconID` has been explicitly set.
  var hasIconID: Bool {return self._iconID != nil}
  /// Clears the value of `iconID`. Subsequent reads from it will return its default value.
  mutating func clearIconID() {self._iconID = nil}

  var parentID: String {
    get {return _parentID ?? String()}
    set {_parentID = newValue}
  }
  /// Returns true if `parentID` has been explicitly set.
  var hasParentID: Bool {return self._parentID != nil}
  /// Clears the value of `parentID`. Subsequent reads from it will return its default value.
  mutating func clearParentID() {self._parentID = nil}

  var deletedParentID: String {
    get {return _deletedParentID ?? String()}
    set {_deletedParentID = newValue}
  }
  /// Returns true if `deletedParentID` has been explicitly set.
  var hasDeletedParentID: Bool {return self._deletedParentID != nil}
  /// Clears the value of `deletedParentID`. Subsequent reads from it will return its default value.
  mutating func clearDeletedParentID() {self._deletedParentID = nil}

  var isFavorite: Bool = false

  /// Using ISO8601 format like this: 2022-11-29T06:18:08.095Z
  var createdAt: String = String()

  /// Using ISO8601 format like this: 2022-11-29T06:18:08.095Z
  var updatedAt: String = String()

  /// Using ISO8601 format like this: 2022-11-29T06:18:08.095Z
  var deletedAt: String {
    get {return _deletedAt ?? String()}
    set {_deletedAt = newValue}
  }
  /// Returns true if `deletedAt` has been explicitly set.
  var hasDeletedAt: Bool {return self._deletedAt != nil}
  /// Clears the value of `deletedAt`. Subsequent reads from it will return its default value.
  mutating func clearDeletedAt() {self._deletedAt = nil}

  var position: Double = 0

  var unknownFields = SwiftProtobuf.UnknownStorage()

  init() {}

  fileprivate var _encName: Data? = nil
  fileprivate var _encColor: Data? = nil
  fileprivate var _iconID: String? = nil
  fileprivate var _parentID: String? = nil
  fileprivate var _deletedParentID: String? = nil
  fileprivate var _deletedAt: String? = nil
}

struct ArchiveSafeItemField {
  // SwiftProtobuf.Message conformance is added in an extension below. See the
  // `Message` and `Message+*Additions` files in the SwiftProtobuf library for
  // methods supported on all messages.

  var id: String = String()

  var encName: Data {
    get {return _encName ?? Data()}
    set {_encName = newValue}
  }
  /// Returns true if `encName` has been explicitly set.
  var hasEncName: Bool {return self._encName != nil}
  /// Clears the value of `encName`. Subsequent reads from it will return its default value.
  mutating func clearEncName() {self._encName = nil}

  var position: Double = 0

  var itemID: String = String()

  var encPlaceholder: Data {
    get {return _encPlaceholder ?? Data()}
    set {_encPlaceholder = newValue}
  }
  /// Returns true if `encPlaceholder` has been explicitly set.
  var hasEncPlaceholder: Bool {return self._encPlaceholder != nil}
  /// Clears the value of `encPlaceholder`. Subsequent reads from it will return its default value.
  mutating func clearEncPlaceholder() {self._encPlaceholder = nil}

  var encValue: Data {
    get {return _encValue ?? Data()}
    set {_encValue = newValue}
  }
  /// Returns true if `encValue` has been explicitly set.
  var hasEncValue: Bool {return self._encValue != nil}
  /// Clears the value of `encValue`. Subsequent reads from it will return its default value.
  mutating func clearEncValue() {self._encValue = nil}

  var encKind: Data {
    get {return _encKind ?? Data()}
    set {_encKind = newValue}
  }
  /// Returns true if `encKind` has been explicitly set.
  var hasEncKind: Bool {return self._encKind != nil}
  /// Clears the value of `encKind`. Subsequent reads from it will return its default value.
  mutating func clearEncKind() {self._encKind = nil}

  var showPrediction: Bool = false

  /// Using ISO8601 format like this: 2022-11-29T06:18:08.095Z
  var createdAt: String = String()

  /// Using ISO8601 format like this: 2022-11-29T06:18:08.095Z
  var updatedAt: String = String()

  var isItemIdentifier: Bool = false

  var encFormattingMask: Data {
    get {return _encFormattingMask ?? Data()}
    set {_encFormattingMask = newValue}
  }
  /// Returns true if `encFormattingMask` has been explicitly set.
  var hasEncFormattingMask: Bool {return self._encFormattingMask != nil}
  /// Clears the value of `encFormattingMask`. Subsequent reads from it will return its default value.
  mutating func clearEncFormattingMask() {self._encFormattingMask = nil}

  var encSecureDisplayMask: Data {
    get {return _encSecureDisplayMask ?? Data()}
    set {_encSecureDisplayMask = newValue}
  }
  /// Returns true if `encSecureDisplayMask` has been explicitly set.
  var hasEncSecureDisplayMask: Bool {return self._encSecureDisplayMask != nil}
  /// Clears the value of `encSecureDisplayMask`. Subsequent reads from it will return its default value.
  mutating func clearEncSecureDisplayMask() {self._encSecureDisplayMask = nil}

  var isSecured: Bool = false

  var unknownFields = SwiftProtobuf.UnknownStorage()

  init() {}

  fileprivate var _encName: Data? = nil
  fileprivate var _encPlaceholder: Data? = nil
  fileprivate var _encValue: Data? = nil
  fileprivate var _encKind: Data? = nil
  fileprivate var _encFormattingMask: Data? = nil
  fileprivate var _encSecureDisplayMask: Data? = nil
}

struct ArchiveSafeItemKey {
  // SwiftProtobuf.Message conformance is added in an extension below. See the
  // `Message` and `Message+*Additions` files in the SwiftProtobuf library for
  // methods supported on all messages.

  var id: String = String()

  var value: Data = Data()

  var unknownFields = SwiftProtobuf.UnknownStorage()

  init() {}
}

#if swift(>=5.5) && canImport(_Concurrency)
extension ArchiveMetadata: @unchecked Sendable {}
extension ArchiveMetadata.ArchiveKind: @unchecked Sendable {}
extension Archive: @unchecked Sendable {}
extension ArchiveSafeItem: @unchecked Sendable {}
extension ArchiveSafeItemField: @unchecked Sendable {}
extension ArchiveSafeItemKey: @unchecked Sendable {}
#endif  // swift(>=5.5) && canImport(_Concurrency)

// MARK: - Code below here is support for the SwiftProtobuf runtime.

extension ArchiveMetadata: SwiftProtobuf.Message, SwiftProtobuf._MessageImplementationBase, SwiftProtobuf._ProtoNameProviding {
  static let protoMessageName: String = "ArchiveMetadata"
  static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
    2: .standard(proto: "is_from_one_safe_plus"),
    3: .standard(proto: "archive_version"),
    4: .standard(proto: "from_platform"),
    5: .standard(proto: "created_at"),
    6: .standard(proto: "items_count"),
    7: .standard(proto: "archive_kind"),
  ]

  mutating func decodeMessage<D: SwiftProtobuf.Decoder>(decoder: inout D) throws {
    while let fieldNumber = try decoder.nextFieldNumber() {
      // The use of inline closures is to circumvent an issue where the compiler
      // allocates stack space for every case branch when no optimizations are
      // enabled. https://github.com/apple/swift-protobuf/issues/1034
      switch fieldNumber {
      case 2: try { try decoder.decodeSingularBoolField(value: &self.isFromOneSafePlus) }()
      case 3: try { try decoder.decodeSingularInt32Field(value: &self.archiveVersion) }()
      case 4: try { try decoder.decodeSingularStringField(value: &self.fromPlatform) }()
      case 5: try { try decoder.decodeSingularStringField(value: &self.createdAt) }()
      case 6: try { try decoder.decodeSingularInt32Field(value: &self.itemsCount) }()
      case 7: try { try decoder.decodeSingularEnumField(value: &self.archiveKind) }()
      default: break
      }
    }
  }

  func traverse<V: SwiftProtobuf.Visitor>(visitor: inout V) throws {
    if self.isFromOneSafePlus != false {
      try visitor.visitSingularBoolField(value: self.isFromOneSafePlus, fieldNumber: 2)
    }
    if self.archiveVersion != 0 {
      try visitor.visitSingularInt32Field(value: self.archiveVersion, fieldNumber: 3)
    }
    if !self.fromPlatform.isEmpty {
      try visitor.visitSingularStringField(value: self.fromPlatform, fieldNumber: 4)
    }
    if !self.createdAt.isEmpty {
      try visitor.visitSingularStringField(value: self.createdAt, fieldNumber: 5)
    }
    if self.itemsCount != 0 {
      try visitor.visitSingularInt32Field(value: self.itemsCount, fieldNumber: 6)
    }
    if self.archiveKind != .unspecified {
      try visitor.visitSingularEnumField(value: self.archiveKind, fieldNumber: 7)
    }
    try unknownFields.traverse(visitor: &visitor)
  }

  static func ==(lhs: ArchiveMetadata, rhs: ArchiveMetadata) -> Bool {
    if lhs.isFromOneSafePlus != rhs.isFromOneSafePlus {return false}
    if lhs.archiveVersion != rhs.archiveVersion {return false}
    if lhs.fromPlatform != rhs.fromPlatform {return false}
    if lhs.createdAt != rhs.createdAt {return false}
    if lhs.itemsCount != rhs.itemsCount {return false}
    if lhs.archiveKind != rhs.archiveKind {return false}
    if lhs.unknownFields != rhs.unknownFields {return false}
    return true
  }
}

extension ArchiveMetadata.ArchiveKind: SwiftProtobuf._ProtoNameProviding {
  static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
    0: .same(proto: "UNSPECIFIED"),
    1: .same(proto: "BACKUP"),
    2: .same(proto: "SHARING"),
  ]
}

extension Archive: SwiftProtobuf.Message, SwiftProtobuf._MessageImplementationBase, SwiftProtobuf._ProtoNameProviding {
  static let protoMessageName: String = "Archive"
  static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
    1: .same(proto: "salt"),
    2: .same(proto: "items"),
    3: .same(proto: "fields"),
    4: .same(proto: "keys"),
  ]

  mutating func decodeMessage<D: SwiftProtobuf.Decoder>(decoder: inout D) throws {
    while let fieldNumber = try decoder.nextFieldNumber() {
      // The use of inline closures is to circumvent an issue where the compiler
      // allocates stack space for every case branch when no optimizations are
      // enabled. https://github.com/apple/swift-protobuf/issues/1034
      switch fieldNumber {
      case 1: try { try decoder.decodeSingularBytesField(value: &self.salt) }()
      case 2: try { try decoder.decodeRepeatedMessageField(value: &self.items) }()
      case 3: try { try decoder.decodeRepeatedMessageField(value: &self.fields) }()
      case 4: try { try decoder.decodeRepeatedMessageField(value: &self.keys) }()
      default: break
      }
    }
  }

  func traverse<V: SwiftProtobuf.Visitor>(visitor: inout V) throws {
    if !self.salt.isEmpty {
      try visitor.visitSingularBytesField(value: self.salt, fieldNumber: 1)
    }
    if !self.items.isEmpty {
      try visitor.visitRepeatedMessageField(value: self.items, fieldNumber: 2)
    }
    if !self.fields.isEmpty {
      try visitor.visitRepeatedMessageField(value: self.fields, fieldNumber: 3)
    }
    if !self.keys.isEmpty {
      try visitor.visitRepeatedMessageField(value: self.keys, fieldNumber: 4)
    }
    try unknownFields.traverse(visitor: &visitor)
  }

  static func ==(lhs: Archive, rhs: Archive) -> Bool {
    if lhs.salt != rhs.salt {return false}
    if lhs.items != rhs.items {return false}
    if lhs.fields != rhs.fields {return false}
    if lhs.keys != rhs.keys {return false}
    if lhs.unknownFields != rhs.unknownFields {return false}
    return true
  }
}

extension ArchiveSafeItem: SwiftProtobuf.Message, SwiftProtobuf._MessageImplementationBase, SwiftProtobuf._ProtoNameProviding {
  static let protoMessageName: String = "ArchiveSafeItem"
  static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
    1: .same(proto: "id"),
    2: .standard(proto: "enc_name"),
    3: .standard(proto: "enc_color"),
    4: .standard(proto: "icon_id"),
    5: .standard(proto: "parent_id"),
    6: .standard(proto: "deleted_parent_id"),
    7: .standard(proto: "is_favorite"),
    8: .standard(proto: "created_at"),
    9: .standard(proto: "updated_at"),
    10: .standard(proto: "deleted_at"),
    11: .same(proto: "position"),
  ]

  mutating func decodeMessage<D: SwiftProtobuf.Decoder>(decoder: inout D) throws {
    while let fieldNumber = try decoder.nextFieldNumber() {
      // The use of inline closures is to circumvent an issue where the compiler
      // allocates stack space for every case branch when no optimizations are
      // enabled. https://github.com/apple/swift-protobuf/issues/1034
      switch fieldNumber {
      case 1: try { try decoder.decodeSingularStringField(value: &self.id) }()
      case 2: try { try decoder.decodeSingularBytesField(value: &self._encName) }()
      case 3: try { try decoder.decodeSingularBytesField(value: &self._encColor) }()
      case 4: try { try decoder.decodeSingularStringField(value: &self._iconID) }()
      case 5: try { try decoder.decodeSingularStringField(value: &self._parentID) }()
      case 6: try { try decoder.decodeSingularStringField(value: &self._deletedParentID) }()
      case 7: try { try decoder.decodeSingularBoolField(value: &self.isFavorite) }()
      case 8: try { try decoder.decodeSingularStringField(value: &self.createdAt) }()
      case 9: try { try decoder.decodeSingularStringField(value: &self.updatedAt) }()
      case 10: try { try decoder.decodeSingularStringField(value: &self._deletedAt) }()
      case 11: try { try decoder.decodeSingularDoubleField(value: &self.position) }()
      default: break
      }
    }
  }

  func traverse<V: SwiftProtobuf.Visitor>(visitor: inout V) throws {
    // The use of inline closures is to circumvent an issue where the compiler
    // allocates stack space for every if/case branch local when no optimizations
    // are enabled. https://github.com/apple/swift-protobuf/issues/1034 and
    // https://github.com/apple/swift-protobuf/issues/1182
    if !self.id.isEmpty {
      try visitor.visitSingularStringField(value: self.id, fieldNumber: 1)
    }
    try { if let v = self._encName {
      try visitor.visitSingularBytesField(value: v, fieldNumber: 2)
    } }()
    try { if let v = self._encColor {
      try visitor.visitSingularBytesField(value: v, fieldNumber: 3)
    } }()
    try { if let v = self._iconID {
      try visitor.visitSingularStringField(value: v, fieldNumber: 4)
    } }()
    try { if let v = self._parentID {
      try visitor.visitSingularStringField(value: v, fieldNumber: 5)
    } }()
    try { if let v = self._deletedParentID {
      try visitor.visitSingularStringField(value: v, fieldNumber: 6)
    } }()
    if self.isFavorite != false {
      try visitor.visitSingularBoolField(value: self.isFavorite, fieldNumber: 7)
    }
    if !self.createdAt.isEmpty {
      try visitor.visitSingularStringField(value: self.createdAt, fieldNumber: 8)
    }
    if !self.updatedAt.isEmpty {
      try visitor.visitSingularStringField(value: self.updatedAt, fieldNumber: 9)
    }
    try { if let v = self._deletedAt {
      try visitor.visitSingularStringField(value: v, fieldNumber: 10)
    } }()
    if self.position != 0 {
      try visitor.visitSingularDoubleField(value: self.position, fieldNumber: 11)
    }
    try unknownFields.traverse(visitor: &visitor)
  }

  static func ==(lhs: ArchiveSafeItem, rhs: ArchiveSafeItem) -> Bool {
    if lhs.id != rhs.id {return false}
    if lhs._encName != rhs._encName {return false}
    if lhs._encColor != rhs._encColor {return false}
    if lhs._iconID != rhs._iconID {return false}
    if lhs._parentID != rhs._parentID {return false}
    if lhs._deletedParentID != rhs._deletedParentID {return false}
    if lhs.isFavorite != rhs.isFavorite {return false}
    if lhs.createdAt != rhs.createdAt {return false}
    if lhs.updatedAt != rhs.updatedAt {return false}
    if lhs._deletedAt != rhs._deletedAt {return false}
    if lhs.position != rhs.position {return false}
    if lhs.unknownFields != rhs.unknownFields {return false}
    return true
  }
}

extension ArchiveSafeItemField: SwiftProtobuf.Message, SwiftProtobuf._MessageImplementationBase, SwiftProtobuf._ProtoNameProviding {
  static let protoMessageName: String = "ArchiveSafeItemField"
  static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
    1: .same(proto: "id"),
    2: .standard(proto: "enc_name"),
    3: .same(proto: "position"),
    4: .standard(proto: "item_id"),
    5: .standard(proto: "enc_placeholder"),
    6: .standard(proto: "enc_value"),
    7: .standard(proto: "enc_kind"),
    8: .standard(proto: "show_prediction"),
    9: .standard(proto: "created_at"),
    10: .standard(proto: "updated_at"),
    11: .standard(proto: "is_item_identifier"),
    12: .standard(proto: "enc_formatting_mask"),
    13: .standard(proto: "enc_secure_display_mask"),
    14: .standard(proto: "is_secured"),
  ]

  mutating func decodeMessage<D: SwiftProtobuf.Decoder>(decoder: inout D) throws {
    while let fieldNumber = try decoder.nextFieldNumber() {
      // The use of inline closures is to circumvent an issue where the compiler
      // allocates stack space for every case branch when no optimizations are
      // enabled. https://github.com/apple/swift-protobuf/issues/1034
      switch fieldNumber {
      case 1: try { try decoder.decodeSingularStringField(value: &self.id) }()
      case 2: try { try decoder.decodeSingularBytesField(value: &self._encName) }()
      case 3: try { try decoder.decodeSingularDoubleField(value: &self.position) }()
      case 4: try { try decoder.decodeSingularStringField(value: &self.itemID) }()
      case 5: try { try decoder.decodeSingularBytesField(value: &self._encPlaceholder) }()
      case 6: try { try decoder.decodeSingularBytesField(value: &self._encValue) }()
      case 7: try { try decoder.decodeSingularBytesField(value: &self._encKind) }()
      case 8: try { try decoder.decodeSingularBoolField(value: &self.showPrediction) }()
      case 9: try { try decoder.decodeSingularStringField(value: &self.createdAt) }()
      case 10: try { try decoder.decodeSingularStringField(value: &self.updatedAt) }()
      case 11: try { try decoder.decodeSingularBoolField(value: &self.isItemIdentifier) }()
      case 12: try { try decoder.decodeSingularBytesField(value: &self._encFormattingMask) }()
      case 13: try { try decoder.decodeSingularBytesField(value: &self._encSecureDisplayMask) }()
      case 14: try { try decoder.decodeSingularBoolField(value: &self.isSecured) }()
      default: break
      }
    }
  }

  func traverse<V: SwiftProtobuf.Visitor>(visitor: inout V) throws {
    // The use of inline closures is to circumvent an issue where the compiler
    // allocates stack space for every if/case branch local when no optimizations
    // are enabled. https://github.com/apple/swift-protobuf/issues/1034 and
    // https://github.com/apple/swift-protobuf/issues/1182
    if !self.id.isEmpty {
      try visitor.visitSingularStringField(value: self.id, fieldNumber: 1)
    }
    try { if let v = self._encName {
      try visitor.visitSingularBytesField(value: v, fieldNumber: 2)
    } }()
    if self.position != 0 {
      try visitor.visitSingularDoubleField(value: self.position, fieldNumber: 3)
    }
    if !self.itemID.isEmpty {
      try visitor.visitSingularStringField(value: self.itemID, fieldNumber: 4)
    }
    try { if let v = self._encPlaceholder {
      try visitor.visitSingularBytesField(value: v, fieldNumber: 5)
    } }()
    try { if let v = self._encValue {
      try visitor.visitSingularBytesField(value: v, fieldNumber: 6)
    } }()
    try { if let v = self._encKind {
      try visitor.visitSingularBytesField(value: v, fieldNumber: 7)
    } }()
    if self.showPrediction != false {
      try visitor.visitSingularBoolField(value: self.showPrediction, fieldNumber: 8)
    }
    if !self.createdAt.isEmpty {
      try visitor.visitSingularStringField(value: self.createdAt, fieldNumber: 9)
    }
    if !self.updatedAt.isEmpty {
      try visitor.visitSingularStringField(value: self.updatedAt, fieldNumber: 10)
    }
    if self.isItemIdentifier != false {
      try visitor.visitSingularBoolField(value: self.isItemIdentifier, fieldNumber: 11)
    }
    try { if let v = self._encFormattingMask {
      try visitor.visitSingularBytesField(value: v, fieldNumber: 12)
    } }()
    try { if let v = self._encSecureDisplayMask {
      try visitor.visitSingularBytesField(value: v, fieldNumber: 13)
    } }()
    if self.isSecured != false {
      try visitor.visitSingularBoolField(value: self.isSecured, fieldNumber: 14)
    }
    try unknownFields.traverse(visitor: &visitor)
  }

  static func ==(lhs: ArchiveSafeItemField, rhs: ArchiveSafeItemField) -> Bool {
    if lhs.id != rhs.id {return false}
    if lhs._encName != rhs._encName {return false}
    if lhs.position != rhs.position {return false}
    if lhs.itemID != rhs.itemID {return false}
    if lhs._encPlaceholder != rhs._encPlaceholder {return false}
    if lhs._encValue != rhs._encValue {return false}
    if lhs._encKind != rhs._encKind {return false}
    if lhs.showPrediction != rhs.showPrediction {return false}
    if lhs.createdAt != rhs.createdAt {return false}
    if lhs.updatedAt != rhs.updatedAt {return false}
    if lhs.isItemIdentifier != rhs.isItemIdentifier {return false}
    if lhs._encFormattingMask != rhs._encFormattingMask {return false}
    if lhs._encSecureDisplayMask != rhs._encSecureDisplayMask {return false}
    if lhs.isSecured != rhs.isSecured {return false}
    if lhs.unknownFields != rhs.unknownFields {return false}
    return true
  }
}

extension ArchiveSafeItemKey: SwiftProtobuf.Message, SwiftProtobuf._MessageImplementationBase, SwiftProtobuf._ProtoNameProviding {
  static let protoMessageName: String = "ArchiveSafeItemKey"
  static let _protobuf_nameMap: SwiftProtobuf._NameMap = [
    1: .same(proto: "id"),
    2: .same(proto: "value"),
  ]

  mutating func decodeMessage<D: SwiftProtobuf.Decoder>(decoder: inout D) throws {
    while let fieldNumber = try decoder.nextFieldNumber() {
      // The use of inline closures is to circumvent an issue where the compiler
      // allocates stack space for every case branch when no optimizations are
      // enabled. https://github.com/apple/swift-protobuf/issues/1034
      switch fieldNumber {
      case 1: try { try decoder.decodeSingularStringField(value: &self.id) }()
      case 2: try { try decoder.decodeSingularBytesField(value: &self.value) }()
      default: break
      }
    }
  }

  func traverse<V: SwiftProtobuf.Visitor>(visitor: inout V) throws {
    if !self.id.isEmpty {
      try visitor.visitSingularStringField(value: self.id, fieldNumber: 1)
    }
    if !self.value.isEmpty {
      try visitor.visitSingularBytesField(value: self.value, fieldNumber: 2)
    }
    try unknownFields.traverse(visitor: &visitor)
  }

  static func ==(lhs: ArchiveSafeItemKey, rhs: ArchiveSafeItemKey) -> Bool {
    if lhs.id != rhs.id {return false}
    if lhs.value != rhs.value {return false}
    if lhs.unknownFields != rhs.unknownFields {return false}
    return true
  }
}
