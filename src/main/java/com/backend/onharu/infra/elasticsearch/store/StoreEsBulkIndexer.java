package com.backend.onharu.infra.elasticsearch.store;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MySQL → Elasticsearch `stores` 인덱스 벌크 적재 로직.
 */
@Component
@ConditionalOnProperty(name = "onharu.elasticsearch.enabled", havingValue = "true")
public class StoreEsBulkIndexer {

    private static final Logger log = LoggerFactory.getLogger(StoreEsBulkIndexer.class);

    private final ElasticsearchClient elasticsearchClient;
    private final StoreEsReindexJdbcPort jdbcPort;

    public StoreEsBulkIndexer(ElasticsearchClient elasticsearchClient, StoreEsReindexJdbcPort jdbcPort) {
        this.elasticsearchClient = elasticsearchClient;
        this.jdbcPort = jdbcPort;
    }

    public ReindexResult reindex(ReindexCommand cmd) throws Exception {
        Instant startedAt = Instant.now();

        long lastId = cmd.fromId();
        long totalLoaded = 0;
        long totalIndexed = 0;
        long totalFailed = 0;

        while (true) {
            List<StoreEsReindexJdbcPort.StoreRow> rows = jdbcPort.loadStoreRowsAfterId(lastId, cmd.batchSize());
            if (rows.isEmpty()) {
                break;
            }

            totalLoaded += rows.size();
            lastId = rows.get(rows.size() - 1).id();

            if (cmd.dryRun()) {
                totalIndexed += rows.size();
                if (cmd.logEveryBatches() > 0 && (totalLoaded / cmd.batchSize()) % cmd.logEveryBatches() == 0) {
                    log.info("[DRY-RUN] stores reindex progress: loaded={}, indexed(simulated)={}, lastId={}",
                            totalLoaded, totalIndexed, lastId);
                }
                continue;
            }

            BulkRequest bulkRequest = buildBulkRequest(cmd.indexName(), rows);
            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);

            if (bulkResponse.errors()) {
                long failed = bulkResponse.items().stream().filter(item -> item.error() != null).count();
                totalFailed += failed;
                totalIndexed += (rows.size() - failed);
                log.warn("stores bulk had errors: total={}, failed={}, lastId={}, sampleError={}",
                        rows.size(), failed, lastId,
                        bulkResponse.items().stream().filter(i -> i.error() != null).findFirst().map(i -> i.error().reason()).orElse("-"));
            } else {
                totalIndexed += rows.size();
            }

            if (cmd.logEveryBatches() > 0 && (totalLoaded / cmd.batchSize()) % cmd.logEveryBatches() == 0) {
                log.info("stores reindex progress: loaded={}, indexed={}, failed={}, lastId={}",
                        totalLoaded, totalIndexed, totalFailed, lastId);
            }
        }

        Duration elapsed = Duration.between(startedAt, Instant.now());
        return new ReindexResult(cmd.indexName(), cmd.dryRun(), totalLoaded, totalIndexed, totalFailed, lastId, elapsed.toMillis());
    }

    private BulkRequest buildBulkRequest(String indexName, List<StoreEsReindexJdbcPort.StoreRow> rows) {
        BulkRequest.Builder builder = new BulkRequest.Builder().index(indexName);

        for (StoreEsReindexJdbcPort.StoreRow row : rows) {
            StoreSearchDocument document = toDocument(row);
            builder.operations(op -> op.index(idx -> idx
                    .id(Long.toString(document.id()))
                    .document(document)
            ));
        }

        return builder.build();
    }

    private StoreSearchDocument toDocument(StoreEsReindexJdbcPort.StoreRow row) {
        StoreSearchDocument.GeoPoint location = parseGeoPoint(row.lat(), row.lng()).orElse(null);
        List<String> tags = row.tags() == null ? List.of() : new ArrayList<>(row.tags());

        return new StoreSearchDocument(
                row.id(),
                row.ownerId(),
                row.categoryId(),
                row.name(),
                row.address(),
                row.phone(),
                row.introduction(),
                row.intro(),
                row.isOpen(),
                row.isSharing(),
                row.createdAt(),
                row.createdBy(),
                location,
                tags
        );
    }

    private Optional<StoreSearchDocument.GeoPoint> parseGeoPoint(String lat, String lng) {
        if (lat == null || lng == null) {
            return Optional.empty();
        }
        try {
            double latValue = Double.parseDouble(lat);
            double lonValue = Double.parseDouble(lng);
            return Optional.of(new StoreSearchDocument.GeoPoint(latValue, lonValue));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public record ReindexCommand(
            String indexName,
            boolean dryRun,
            int batchSize,
            long fromId,
            int logEveryBatches
    ) {
        public static ReindexCommand of(String indexName, boolean dryRun, int batchSize, long fromId, int logEveryBatches) {
            return new ReindexCommand(indexName, dryRun, batchSize, fromId, logEveryBatches);
        }
    }

    public record ReindexResult(
            String indexName,
            boolean dryRun,
            long loaded,
            long indexed,
            long failed,
            long lastId,
            long elapsedMs
    ) {
    }
}

