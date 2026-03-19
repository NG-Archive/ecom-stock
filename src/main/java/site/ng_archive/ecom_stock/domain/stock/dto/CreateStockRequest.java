package site.ng_archive.ecom_stock.domain.stock.dto;

public record CreateStockRequest(Long productId, Long quantity) {
    public CreateStockCommand toCommand() {
        return new CreateStockCommand(productId, quantity);
    }
}
