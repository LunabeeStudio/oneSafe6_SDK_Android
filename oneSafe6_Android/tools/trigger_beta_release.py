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
#  Created by Lunabee Studio / Date - 2/26/2024 - for the oneSafe6 SDK.
#  Last modified 2/26/24, 1:06 PM

import argparse
import json
import logging
import os

import requests

logging.basicConfig(level=logging.DEBUG)

parser = argparse.ArgumentParser(
    description='''
    Trigger a playstore open beta release
    ''',
    formatter_class=argparse.RawTextHelpFormatter,
)
parser.add_argument(
    "--server",
    help="TeamCity server url",
    required=True,
    type=str,
)
parser.add_argument(
    "--token",
    help="TeamCity server oauth token",
    required=True,
    type=str,
)
parser.add_argument(
    "--branch",
    help="Git branch to build",
    required=True,
    type=str,
)
args = parser.parse_args()

server_url = args.server
api_token = args.token
branch = args.branch

endpoint = server_url + '/app/rest/buildQueue'
headers = {
    'Content-Type': 'application/json; charset=utf-8',
    'Accept': 'application/json',
    'Authorization': f'Bearer {api_token}'
}
with open(os.path.dirname(os.path.realpath(__file__)) + "/beta_trigger_teamcity_payload.json") as json_file:
    payload = json.load(json_file)
payload["branchName"] = branch
param = {
    'moveToTop': 'true',
}
response = requests.post(endpoint, json=param, headers=headers, data=json.dumps(payload).encode('utf8'))
print(response)
