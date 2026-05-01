-- 프로덕션에서 spring.jpa.hibernate.ddl-auto=validate 일 때 수동 적용용 (MySQL 8 예시).
-- 운영 DB에 맞게 조정 후 실행하세요.

CREATE TABLE IF NOT EXISTS outbox_events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NULL,
    target_topic VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    sent_at DATETIME(6) NULL
);
