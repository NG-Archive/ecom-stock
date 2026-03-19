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
}
