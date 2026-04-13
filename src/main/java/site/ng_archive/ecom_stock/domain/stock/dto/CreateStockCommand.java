package site.ng_archive.ecom_stock.domain.stock.dto;

import site.ng_archive.ecom_stock.domain.stock.Stock;

public record CreateStockCommand(Long productId, Long quantity, Long memberId) {
    public Stock toEntity() {
        return new Stock(null, productId, quantity);
    }
}
