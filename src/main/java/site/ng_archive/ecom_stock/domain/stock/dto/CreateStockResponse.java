package site.ng_archive.ecom_stock.domain.stock.dto;

import site.ng_archive.ecom_stock.domain.stock.Stock;

public record CreateStockResponse(Long id, Long productId, Long quantity) {
    public static CreateStockResponse from(Stock entity) {
        return new CreateStockResponse(entity.id(), entity.productId(), entity.quantity());
    }
}
