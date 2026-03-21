package site.ng_archive.ecom_stock.domain.stock.dto;

import jakarta.validation.constraints.NotNull;
import site.ng_archive.ecom_stock.domain.stock.Stock;

public record CreateStockResponse(@NotNull Long id, @NotNull Long productId, @NotNull Long quantity) {
    public static CreateStockResponse from(Stock entity) {
        return new CreateStockResponse(entity.id(), entity.productId(), entity.quantity());
    }
}
