package site.ng_archive.ecom_stock.domain.stock.dto;

public record DeductStockCommand(Long productId, Long orderId, Long quantity) {
}
