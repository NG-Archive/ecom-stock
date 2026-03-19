package site.ng_archive.ecom_stock.domain.stock;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table
public record StockHistory(
    @Id
    Long id,

    Long stockId,

    Long orderId,

    ChangeType type,

    Long changeQuantity,

    Long totalQuantity,

    @CreatedDate
    LocalDateTime createdAt,

    @LastModifiedDate
    LocalDateTime updatedAt

) {
    public static StockHistory create(Stock stock) {
        return new StockHistory(
            null,
            stock.id(),
            null,
            ChangeType.IN,
            stock.quantity(),
            stock.quantity(),
            null,
            null
        );
    }
}
