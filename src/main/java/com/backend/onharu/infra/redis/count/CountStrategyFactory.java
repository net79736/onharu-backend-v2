package com.backend.onharu.infra.redis.count;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class CountStrategyFactory {

    private final Map<DomainType, CountStrategy> strategies;

    /**
     * 전략 리스트를 초기화합니다.
     *  - StoreViewCountStrategy: STORE 도메인 지원
     * 
     * @param strategyList
     */
    public CountStrategyFactory(List<CountStrategy> strategyList) {
        strategies = new EnumMap<>(DomainType.class);
        for (CountStrategy strategy : strategyList) {
            strategies.put(strategy.getSupportedDomain(), strategy);
        }
    }

    /**
     * 지원하는 도메인 타입에 따른 전략을 반환합니다.
     * @param type
     * @return 지원하는 도메인 타입에 따른 전략
     */
    public CountStrategy getStrategy(DomainType type) {
        return strategies.get(type);
    }
}