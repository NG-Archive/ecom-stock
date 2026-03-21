package site.ng_archive.ecom_stock.domain.stock.dto;

import jakarta.validation.constraints.NotNull;

public record CreateStockRequest(@NotNull Long productId, @NotNull Long quantity) {
    public CreateStockCommand toCommand() {
        return new CreateStockCommand(productId, quantity);
    }
}
