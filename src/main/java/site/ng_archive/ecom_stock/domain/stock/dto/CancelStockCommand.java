package site.ng_archive.ecom_stock.domain.stock.dto;

import jakarta.validation.constraints.NotNull;

public record CancelStockCommand(Long productId, Long orderId) {
}
