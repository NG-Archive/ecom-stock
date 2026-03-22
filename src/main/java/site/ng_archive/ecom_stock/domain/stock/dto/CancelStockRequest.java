package site.ng_archive.ecom_stock.domain.stock.dto;

import jakarta.validation.constraints.NotNull;

public record CancelStockRequest(@NotNull Long productId, @NotNull Long orderId) {
    public CancelStockCommand toCommand() {
        return new CancelStockCommand(productId, orderId);
    }
}
