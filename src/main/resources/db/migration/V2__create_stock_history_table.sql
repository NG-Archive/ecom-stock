CREATE TABLE stock_history
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_id        BIGINT      NOT NULL,
    order_id        BIGINT,
    type            VARCHAR(20) NOT NULL,
    change_quantity BIGINT      NOT NULL,
    total_quantity  BIGINT      NOT NULL,
    created_at      DATETIME(6) NOT NULL,
    updated_at      DATETIME(6) NOT NULL
);