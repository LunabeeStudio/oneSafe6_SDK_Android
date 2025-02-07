/*
 * Copyright (c) 2024 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 6/12/2024 - for the oneSafe6 SDK.
 * Last modified 6/12/24, 9:40 AM
 */

package studio.lunabee.onesafe.storage.utils

import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Add triggers to prevent recursive items.
 */
internal fun SupportSQLiteDatabase.addRecursiveCheckTriggers() {
    execSQL(
        """
             CREATE TRIGGER IF NOT EXISTS recursive_item_insert
             BEFORE INSERT
             ON SafeItem
             WHEN NEW.id = NEW.parent_id OR NEW.id = NEW.deleted_parent_id
             BEGIN
                 SELECT RAISE(ABORT, 'Recursive item forbidden');
             END;
             """,
    )
    execSQL(
        """
             CREATE TRIGGER IF NOT EXISTS recursive_item_update
             BEFORE UPDATE OF parent_id, deleted_parent_id
             ON SafeItem
             WHEN NEW.id = NEW.parent_id OR NEW.id = NEW.deleted_parent_id
             BEGIN
                 SELECT RAISE(ABORT, 'Recursive item forbidden');
             END;
             """,
    )
}

/**
 * Add triggers to nullify other biometric keys before inserting a new one.
 */
internal fun SupportSQLiteDatabase.addUniqueBiometricKeyTrigger() {
    execSQL(
        """
            CREATE TRIGGER IF NOT EXISTS nullify_other_biometric
            BEFORE INSERT ON Safe
            FOR EACH ROW
            WHEN NEW.crypto_biometric_crypto_material IS NOT NULL
            BEGIN
                UPDATE Safe
                SET crypto_biometric_crypto_material = NULL
                WHERE id != NEW.id;
            END;
             """,
    )
    execSQL(
        """
            CREATE TRIGGER IF NOT EXISTS nullify_other_biometric_update
            BEFORE UPDATE ON Safe
            FOR EACH ROW
            WHEN NEW.crypto_biometric_crypto_material IS NOT NULL
            BEGIN
                UPDATE Safe
                SET crypto_biometric_crypto_material = NULL
                WHERE id != NEW.id;
            END;
             """,
    )
}
