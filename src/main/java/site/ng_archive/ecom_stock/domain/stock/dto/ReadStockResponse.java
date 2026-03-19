package site.ng_archive.ecom_stock.domain.stock.dto;

import site.ng_archive.ecom_stock.domain.stock.Stock;

public record ReadStockResponse(Long productId, Long quantity) {

    public static ReadStockResponse from(Stock entity) {
        return new ReadStockResponse(entity.productId(), entity.quantity());
    }
}
