CREATE TABLE stock
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity   BIGINT NOT NULL
);
CREATE INDEX idx_stock_product_id ON stock (product_id);