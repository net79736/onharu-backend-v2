package com.backend.onharu.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 채팅 메시지 AFTER_COMMIT 후속 작업(chat_rooms.last_message_id 갱신, RabbitMQ 발행 등)을 돌리는 전용 풀.
     * 기본 SimpleAsyncTaskExecutor 는 매 호출마다 스레드를 새로 만들어 부하에 취약하므로,
     * 바운디드 풀 + 큐 + CallerRuns 폴리시로 과부하를 방어합니다.
     */
    @Bean(name = "chatEventExecutor")
    public Executor chatEventExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(8);
        exec.setMaxPoolSize(32);
        exec.setQueueCapacity(500);
        exec.setThreadNamePrefix("chat-event-");
        exec.setKeepAliveSeconds(60);
        exec.setWaitForTasksToCompleteOnShutdown(true);
        exec.setAwaitTerminationSeconds(5);
        // 큐가 가득 차면 호출 스레드가 직접 실행 → STOMP inbound 를 끝까지 막진 않도록 최소한의 백프레셔.
        exec.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        exec.initialize();
        return exec;
    }
}
