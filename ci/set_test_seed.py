#  Copyright (c) 2024 Lunabee Studio
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#  Created by Lunabee Studio / Date - 1/15/2024 - for the oneSafe6 SDK.
#  Last modified 1/15/24, 12:10 PM

import argparse
import random

parser = argparse.ArgumentParser()

parser.add_argument(
    "--seed",
    help="Provide a seed",
    default=random.randint(-32768, 32767),
    type=int,
)

args = parser.parse_args()

test_seed = args.seed
os_test_utils_path = "common-test/src/main/kotlin/studio/lunabee/onesafe/test/OSTestUtils.kt"

with open(os_test_utils_path, "r") as f:
    content = f.read()

content = content.replace("private val seed = Random.nextInt()", f"private val seed = {test_seed}")

with open(os_test_utils_path, "w") as f:
    f.write(content)

print(f"Use seed {test_seed} for testing")
