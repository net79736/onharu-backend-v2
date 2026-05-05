package com.backend.onharu.config;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.elasticsearch.client.RestClient;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

/**
 * Elasticsearch Java API Client.
 * 자동 구성은 {@link com.backend.onharu.OnharuApplication} 에서 제외하고, 브로커 미기동 시에도 앱이 뜨도록
 * {@code onharu.elasticsearch.enabled=true} 일 때만 빈을 등록합니다.
 */
@Configuration
@ConditionalOnProperty(name = "onharu.elasticsearch.enabled", havingValue = "true")
public class ElasticsearchConfig {

    @Bean(destroyMethod = "close")
    public ElasticsearchTransport elasticsearchTransport(
            @Value("${spring.elasticsearch.uris:http://localhost:9200}") String uris
    ) {
        // spring.elasticsearch.uris 값은 여러 개의 ES 노드를 콤마(,)로 구분해서 입력할 수 있음.
        // RestClient.builder 는 여러 호스트를 받을 수 있으나, 여기서는 첫 번째 URI만 사용해서 해당 노드에 연결함.
        String firstUri = uris.split(",")[0].trim(); // 첫 번째 URI만 사용 (ex: http://localhost:9200)
        RestClient restClient = RestClient.builder(HttpHost.create(firstUri)).build(); // RestClient 빌더 생성
        return new RestClientTransport(restClient, new JacksonJsonpMapper()); // RestClientTransport 생성
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
