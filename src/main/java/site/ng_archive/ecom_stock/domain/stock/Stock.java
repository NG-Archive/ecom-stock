package site.ng_archive.ecom_stock.domain.stock;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table
public record Stock(
    @Id
    Long id,

    Long productId,

    Long quantity
) {
    public Stock deduct(Long quantity) {
        if (quantity > this.quantity) {
            throw new IllegalArgumentException("stock.invalid.quantity");
        }
        return new Stock(id, productId, this.quantity - quantity);
    }
}
