# Elasticsearch `stores` 인덱스 (Phase 2)

이 문서는 **가게 검색용 Elasticsearch 인덱스(`stores`)의 매핑/세팅**과 로컬 생성 방법을 정리합니다.

## 목적

- MySQL `stores` 데이터를 검색 최적화 형태로 **ES에 미러링**합니다.
- MySQL은 정본(Source of Truth), ES는 검색용 사본(Search Index)입니다.

## 인덱스 생성

전제:

- `docker compose up -d elasticsearch`
- ES 보안은 로컬 벤치 목적으로 꺼져 있음 (`xpack.security.enabled=false`)

명령:

```bash
chmod +x scripts/elasticsearch/*.sh
scripts/elasticsearch/create_stores_index.sh
```

환경변수:

- `SPRING_ELASTICSEARCH_URIS`: 기본 `http://localhost:9200`
- `ES_STORES_INDEX_NAME`: 기본 `stores`
- `ES_STORES_MAPPING_FILE`: 기본 `src/main/resources/elasticsearch/stores-index.v1.json`

## 인덱스 삭제

```bash
scripts/elasticsearch/delete_stores_index.sh
```

## 매핑 파일

- `src/main/resources/elasticsearch/stores-index.v1.json`
- `dynamic: false`로 설정되어 있어, 매핑에 없는 필드는 무시됩니다.

### 주요 필드

- `id`, `ownerId`, `categoryId`: 숫자 필터용
- `name`, `address`, `introduction`, `intro`: 텍스트 검색용
- `location`: `geo_point` (위치 기반 검색/정렬)
- `tags`: `keyword` 배열(태그 필터)

## 주의사항

- 한국어 검색 품질(형태소 분석기 nori 등)은 **후속 Phase**에서 필요해질 때 추가합니다.
- `stores-index.v1.json` 변경은 “인덱스 버전업(재색인)”을 전제로 해야 합니다.

