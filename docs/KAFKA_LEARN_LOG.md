# 카프카 학습

## 컨슈머 그룹이란?

Kafka 에서 메시지를 소비하는 컨슈머(Consumer) 들을 그룹(Group) 으로 묶을 수 있다.
그룹을 지정하는 이유는 하나의 토픽(Topic)에서 여러 컨슈머가 효율적으로 메시지를 분산 처리 하기 위함.

같은 주제를 구독하는 컨슈머들에게 group id를 지정하면 컨슈머들이 같은 컨슈머 그룹에 속하게 됨.
같은 그룹 안에서는 각 메시지가 하나의 컨슈머에게만 전달됨 (중복 소비 X).
즉, 한 개의 메시지는 그룹 내 한 컨슈머만 읽을 수 있다.

여러 개의 컨슈머가 같은 그룹에 속해 있으면, 각각 다른 메시지를 소비하게 되어 부하 분산이 가능해짐

### 컨슈머 그룹 없이 모든 컨슈머가 메시지를 받는 경우

* 토픽 : orders
* 컨슈머 3개 (모두 같은 메시지를 받음)
  | Partition | Message | Consumer 1 | Consumer 2 | Consumer 3 |
  |-----------|---------|------------|------------|------------|
  | 0 | A | ✅ | ✅ | ✅ |
  | 1 | B | ✅ | ✅ | ✅ |
  | 2 | C | ✅ | ✅ | ✅ |

🐰 모든 컨슈머가 동일한 메시지를 받음 (브로드캐스트 방식).

### 같은 group.id를 설정한 경우

* 토픽: orders
* 3개의 파티션, 3개의 컨슈머 (같은 group.id = "group1" 설정)

| Partition | Message | Consumer 1 | Consumer 2 | Consumer 3 |
|-----------|---------|------------|------------|------------|
| 0         | A       | ✅          |            |            |
| 1         | B       |            | ✅          |            |
| 2         | C       |            |            | ✅          |

🐰 각 컨슈머가 다른 메시지를 소비하면서 부하를 나눠 가짐.  
🐰 한 메시지는 그룹 내 한 컨슈머만 읽을 수 있음.
----

```java

@Bean
public ProducerFactory<String, KafkaEntity> producerFactory() {
    Map<String, Object> configProps = new HashMap<>();
    // 사용할 카프카 서버 주소를 설정
    configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
    // KafkaTemplate<String, KafkaEntity> 에서 Key 타입은 String 이므로 StringSerializer 사용
    configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    // KafkaTemplate<String, KafkaEntity> 에서 Value 타입은 객체이므로, JSON 으로 데이터를 넘겨줘야한다. 따라서 JsonSerializer 사용
    configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(configProps);
}
```

1. container_name: 컨테이너 이름을 지정
2. ports: docker 내부 포트: docker 외부 포트
3. 터미널에서 쓸 때는 왼쪽, 타 시스템에서 접속할 때는 오른쪽 포트를 사용
4. ALLOW_PLAINTEXT_LISTENER: 카프카 브로커가 Plaintext 프로토콜을 사용하는 리스너(Listener)를 허용할지 여부를 결정하는 속성
5. KAFKA_CFG_LISTENERS: 클라이언트가 Kafka 브로커에 연결을 연결할 때 사용할 리스너(Listener)를 정의
6. KAFKA_CFG_ADVERTISED_LISTENERS: 클라이언트가 브로커에 연결할 때 사용할 주소를 결정

- PLAINTEXT://Kafka02Service:9092 => Docker 내부에서 브로커에 접속할 때 사용하는 리스너 (터미널에서 사용)
- EXTERNAL://127.0.0.1:10002 => Docker 외부에서 브로커에 접속할 때 사용하는 리스너 (spring boot에서 사용)