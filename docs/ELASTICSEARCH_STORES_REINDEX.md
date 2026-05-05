# MySQL → Elasticsearch `stores` 벌크 적재 (Phase 3)

이 문서는 **MySQL `stores` 테이블의 데이터를 Elasticsearch `stores` 인덱스로 벌크 적재**하는 방법을 정리합니다.

## 설계 요약

- MySQL은 정본(Source of Truth)이고, Elasticsearch는 검색용 사본(Search Index)입니다.
- 초기 적재/재적재는 **대량(백만 건) 처리**를 고려하여
  - JPA 엔티티 로딩 대신 JDBC로 필요한 컬럼만 읽고
  - `Bulk API`로 일정 단위(batch-size)로 적재합니다.
- 안전장치로 기본값은 `dry-run=true` 입니다.

## 사전 준비

1. Elasticsearch 기동

```bash
docker compose up -d elasticsearch
```

2. `stores` 인덱스 생성 (Phase 2)

```bash
chmod +x scripts/elasticsearch/*.sh
scripts/elasticsearch/create_stores_index.sh
```

## 실행 방법

### (1) Dry-run (권장)

```bash
ONHARU_ELASTICSEARCH_ENABLED=true \
ONHARU_ELASTICSEARCH_REINDEX_STORES_ENABLED=true \
ONHARU_ELASTICSEARCH_REINDEX_STORES_DRY_RUN=true \
./gradlew bootRun
```

### (2) 실제 벌크 적재

```bash
ONHARU_ELASTICSEARCH_ENABLED=true \
ONHARU_ELASTICSEARCH_REINDEX_STORES_ENABLED=true \
ONHARU_ELASTICSEARCH_REINDEX_STORES_DRY_RUN=false \
ONHARU_ELASTICSEARCH_REINDEX_STORES_BATCH_SIZE=2000 \
./gradlew bootRun
```

### (3) 중간부터 재시작(from-id)

```bash
ONHARU_ELASTICSEARCH_ENABLED=true \
ONHARU_ELASTICSEARCH_REINDEX_STORES_ENABLED=true \
ONHARU_ELASTICSEARCH_REINDEX_STORES_DRY_RUN=false \
ONHARU_ELASTICSEARCH_REINDEX_STORES_FROM_ID=500000 \
./gradlew bootRun
```

## 주의사항

- 현재 구현은 `stores` 테이블만 적재합니다. `tags`는 빈 배열로 들어갑니다(후속 Phase에서 확장).
- MySQL의 `lat/lng`는 문자열이므로, 숫자 파싱 실패 시 `location`은 null로 적재됩니다.
- 이 러너는 `onharu.elasticsearch.reindex.stores.enabled=true`일 때만 실행됩니다.

