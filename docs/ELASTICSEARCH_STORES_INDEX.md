# Elasticsearch `stores` 인덱스 (Phase 2)

이 문서는 **가게 검색용 Elasticsearch 인덱스(`stores`)의 매핑/세팅**과 로컬 생성 방법을 정리합니다.

## 목적

- MySQL `stores` 데이터를 검색 최적화 형태로 **ES에 미러링**합니다.
- MySQL은 정본(Source of Truth), ES는 검색용 사본(Search Index)입니다.

## 이 인덱스가 의미하는 것 (분산 시스템 관점)

`src/main/resources/elasticsearch/stores-index.v1.json`의 설정은 “ES를 단순 저장소가 아니라 **분산 검색 시스템**으로 운영하겠다”는 선언에 가깝습니다.

### `number_of_shards: 3` (Scale-out / 병렬 처리)

- **의미**: 인덱스를 3개의 Primary Shard로 분할합니다.
- **효과(직관)**: 문서가 120만 건이라면 대략 3등분되어 저장/처리됩니다.
- **검색 성능**: 검색 쿼리는 샤드 단위로 병렬 실행될 수 있어(노드/리소스가 충분하다는 전제 하에) 처리 지연이 줄어들 수 있습니다.
- **주의**: 샤드 수는 인덱스 생성 후 변경이 어렵습니다(보통 “재색인”이 필요). 벤치마크/운영 규모가 커질수록 “너무 적음”이 더 큰 문제라 3으로 시작하는 선택은 합리적일 때가 많습니다.

### `number_of_replicas: 2` (HA / Read Throughput)

- **의미**: 각 Primary Shard마다 Replica를 2개 둡니다.
  - Primary 3개 + Replica 6개 = **총 9개 샤드(Shard Copy)** 가 클러스터에 배치됩니다.
- **고가용성(HA)**: 특정 노드 장애 시에도 Replica가 남아 데이터 유실/서비스 중단 위험이 크게 줄어듭니다.
- **읽기 처리량**: 검색 요청은 Replica에서도 처리 가능하므로, **Read(검색) 처리량**이 증가할 수 있습니다.

### “샤드 3 + 레플리카 2”가 진짜 의미 있으려면?

- **권장**: 최소 3대 이상의 ES 노드가 있어야 Replica까지 고르게 배치되어 설정의 의도가 살아납니다.
- **로컬 1노드 개발 환경**: Replica를 2로 두면 Replica를 배치할 노드가 부족해져 인덱스/클러스터가 **Yellow(Replica 미할당)** 상태가 되는 것이 일반적입니다.
  - 개발/로컬 벤치에서 상태 노이즈를 줄이려면 **로컬에서는 Replica를 0(또는 1)** 로 낮추는 편이 정신 건강에 좋습니다.

## 핵심 옵션: `dynamic: false` (스키마 통제)

Elasticsearch는 기본적으로 “필드가 없으면 자동 생성”하는 유연함이 강점이지만, 운영 환경에서는 그 유연함이 **인덱스 오염/비용 증가/검색 품질 저하**로 되돌아오기도 합니다.

### 왜 `dynamic: false`로 설정하나? (Strict Schema)

- **인덱스 오염 방지**: 예를 들어 `location`을 실수로 `loaction`(오타)으로 보내면,
  - `dynamic: true`라면 ES가 새 필드를 자동 생성해버리고,
  - 이후 `location`으로 검색할 때 오타 필드로 들어간 문서는 검색에서 누락되어 “조용히” 품질이 깨집니다.
- **리소스 절약**: 새 필드가 추가될 때마다 ES는 역색인(inverted index) 및 관련 자료구조를 생성합니다. 무분별한 필드 생성은 **디스크/메모리 낭비 + 색인 성능 저하**로 이어집니다.
- **데이터 무결성/예측 가능성**: “검색에 쓰는 필드는 우리가 정의한 것만”이라는 강한 계약을 만들면, 애플리케이션 코드(예: Store → ES 문서 변환)도 안정적으로 유지됩니다.

### `dynamic: false`일 때, 매핑에 없는 필드는 어떻게 되나?

- **저장(`_source`)**: 문서는 통째로 저장되므로, 개별 문서 조회(`GET /index/_doc/id`)에서는 값이 보일 수 있습니다.
- **검색/집계(Indexing)**: 하지만 매핑에 없던 필드는 **역색인에 기록되지 않기 때문에** 그 필드로 검색/정렬/집계를 수행해도 원하는 결과가 나오지 않습니다.

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
- `dynamic: false`로 설정되어 있어, **매핑에 없는 필드는 검색 인덱싱에서 제외**됩니다. (문서 `_source`에는 남을 수 있습니다.)

### 주요 필드

- `id`, `ownerId`, `categoryId`: 숫자 필터용
- `name`, `address`, `introduction`, `intro`: 텍스트 검색용
- `location`: `geo_point` (위치 기반 검색/정렬)
- `tags`: `keyword` 배열(태그 필터)

## 주의사항

- 한국어 검색 품질(형태소 분석기 nori 등)은 **후속 Phase**에서 필요해질 때 추가합니다.
- `stores-index.v1.json` 변경은 “인덱스 버전업(재색인)”을 전제로 해야 합니다.
