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
    public Stock {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("stock.invalid.quantity");
        }

        if (productId == null) {
            throw new IllegalArgumentException("stock.invalid.productid");
        }
    }

    public Stock deduct(Long quantity) {
        if (quantity == null || quantity < 0 || quantity > this.quantity) {
            throw new IllegalArgumentException("stock.invalid.quantity");
        }
        return new Stock(id, productId, this.quantity - quantity);
    }

    public Stock add(Long quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("stock.invalid.quantity");
        }
        return new Stock(id, productId, this.quantity + quantity);
    }

    public Stock cancel(Long quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("stock.invalid.quantity");
        }
        return new Stock(id, productId, this.quantity + quantity);
    }
}
