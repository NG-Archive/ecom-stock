package site.ng_archive.ecom_stock.domain.stock.dto;

import jakarta.validation.constraints.NotNull;

public record DeductStockRequest(@NotNull Long productId, @NotNull Long orderId, @NotNull Long quantity) {
    public DeductStockCommand toCommand() {
        return new DeductStockCommand(productId, orderId, quantity);
    }
}
