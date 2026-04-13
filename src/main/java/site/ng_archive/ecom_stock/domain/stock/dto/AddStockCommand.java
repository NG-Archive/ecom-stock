package site.ng_archive.ecom_stock.domain.stock.dto;

public record AddStockCommand(Long productId, Long quantity, Long memberId) {
}
