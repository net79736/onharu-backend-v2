#!/usr/bin/env bash
set -euo pipefail

ES_URI="${SPRING_ELASTICSEARCH_URIS:-http://localhost:9200}"
INDEX_NAME="${ES_STORES_INDEX_NAME:-stores}"
MAPPING_FILE="${ES_STORES_MAPPING_FILE:-src/main/resources/elasticsearch/stores-index.v1.json}"

if [[ ! -f "$MAPPING_FILE" ]]; then
  echo "mapping file not found: $MAPPING_FILE" >&2
  exit 1
fi

echo "Creating index: $INDEX_NAME (ES: $ES_URI)"
curl -sS -X PUT "$ES_URI/$INDEX_NAME" \
  -H 'Content-Type: application/json' \
  --data-binary @"$MAPPING_FILE"
echo

