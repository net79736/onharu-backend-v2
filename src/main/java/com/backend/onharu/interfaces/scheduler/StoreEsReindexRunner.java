package com.backend.onharu.interfaces.scheduler;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.backend.onharu.infra.elasticsearch.store.StoreEsBulkIndexer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 운영/로컬에서 수동 실행하는 MySQL → Elasticsearch `stores` 벌크 적재 러너.
 *
 * <p>Spring Batch를 도입하기 전, 대량 재색인(초기 적재/재적재) 용도로 사용합니다.</p>
 */
@Component
@ConditionalOnProperty(
        name = {"onharu.elasticsearch.enabled", "onharu.elasticsearch.reindex.stores.enabled"},
        havingValue = "true"
)
public class StoreEsReindexRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StoreEsReindexRunner.class);

    private final StoreEsBulkIndexer bulkIndexer;
    private final Environment environment;

    public StoreEsReindexRunner(StoreEsBulkIndexer bulkIndexer, Environment environment) {
        this.bulkIndexer = bulkIndexer;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String indexName = environment.getProperty("onharu.elasticsearch.reindex.stores.index-name", "stores");
        boolean dryRun = Boolean.parseBoolean(environment.getProperty("onharu.elasticsearch.reindex.stores.dry-run", "true"));
        int batchSize = Integer.parseInt(environment.getProperty("onharu.elasticsearch.reindex.stores.batch-size", "1000"));
        long fromId = Long.parseLong(environment.getProperty("onharu.elasticsearch.reindex.stores.from-id", "0"));
        int logEveryBatches = Integer.parseInt(environment.getProperty("onharu.elasticsearch.reindex.stores.log-every-batches", "10"));

        log.info("stores reindex starting: indexName={}, dryRun={}, batchSize={}, fromId={}, logEveryBatches={}",
                indexName, dryRun, batchSize, fromId, logEveryBatches);

        StoreEsBulkIndexer.ReindexResult result = bulkIndexer.reindex(
                StoreEsBulkIndexer.ReindexCommand.of(indexName, dryRun, batchSize, fromId, logEveryBatches)
        );

        log.info("stores reindex finished: indexName={}, dryRun={}, loaded={}, indexed={}, failed={}, lastId={}, elapsedMs={}",
                result.indexName(), result.dryRun(), result.loaded(), result.indexed(), result.failed(), result.lastId(), result.elapsedMs());
    }
}

