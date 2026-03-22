package site.ng_archive.ecom_stock.domain.stock.dto;

import jakarta.validation.constraints.NotNull;

public record AddStockRequest(@NotNull Long productId, @NotNull Long quantity) {
    public AddStockCommand toCommand() {
        return new AddStockCommand(productId, quantity);
    }
}
