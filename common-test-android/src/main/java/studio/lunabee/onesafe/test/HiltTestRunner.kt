/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:30 AM
 */

package studio.lunabee.onesafe.test

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

// A custom runner to set up the instrumented application class for tests.
class HiltTestRunner : AndroidJUnitRunner() {

    lateinit var app: Application

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        app = super.newApplication(cl, HiltTestApplication::class.java.name, context)
        return app
    }

    override fun onCreate(arguments: Bundle?) {
        super.onCreate(arguments)
        EmojiCompat.init(BundledEmojiCompatConfig(app))
    }
}
