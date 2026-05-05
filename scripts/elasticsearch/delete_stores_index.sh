#!/usr/bin/env bash
set -euo pipefail

ES_URI="${SPRING_ELASTICSEARCH_URIS:-http://localhost:9200}"
INDEX_NAME="${ES_STORES_INDEX_NAME:-stores}"

echo "Deleting index: $INDEX_NAME (ES: $ES_URI)"
curl -sS -X DELETE "$ES_URI/$INDEX_NAME"
echo

