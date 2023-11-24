import argparse
import os

import requests

parser = argparse.ArgumentParser(
    description='''
    Delete auto translated locales
    ''',
    formatter_class=argparse.RawTextHelpFormatter,
)
parser.add_argument(
    "--id",
    help="String id to delete",
    required=True,
    type=str,
)
parser.add_argument(
    "--key",
    help="The Loco full access api key",
    type=str,
)
args = parser.parse_args()

api_key = args.key or os.environ["LOCO_OS6_API_KEY"]

headers = {
    'Authorization': f'Loco {api_key}'
}

for locale in ["ar", "de", "it", "ja", "ko", "pl", "pt", "ru", "zh-Hans", "es", "zh-Hant", "uk"]:
    url = f"https://localise.biz/api/translations/{args.id}/{locale}"
    rep = requests.post(url, headers=headers)
    print(rep)
