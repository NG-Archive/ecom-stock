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

    public StockHistory {
        if (changeQuantity == null || changeQuantity < 0) {
            throw new IllegalArgumentException("stock.invalid.quantity");
        }
        if (totalQuantity == null || totalQuantity < 0) {
            throw new IllegalArgumentException("stock.invalid.quantity");
        }
        if (stockId == null) {
            throw new IllegalArgumentException("stock.invalid.stockid");
        }
        if (type == null) {
            throw new IllegalArgumentException("stock.invalid.type");
        }
        if (orderId == null && type != ChangeType.IN) {
            throw new IllegalArgumentException("stock.invalid.type");
        }
    }

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

    public static StockHistory createDeduct(Stock stock, Long orderId, Long changeQuantity) {
        return new StockHistory(
            null,
            stock.id(),
            orderId,
            ChangeType.OUT,
            changeQuantity,
            stock.quantity(),
            null,
            null
        );
    }

    public static StockHistory createAdded(Stock stock, Long quantity) {
        return new StockHistory(
            null,
            stock.id(),
            null,
            ChangeType.IN,
            quantity,
            stock.quantity(),
            null,
            null
        );
    }

    public static StockHistory createCanceled(Stock stock, Long orderId, Long quantity) {
        return new StockHistory(
            null,
            stock.id(),
            orderId,
            ChangeType.CANCEL,
            quantity,
            stock.quantity(),
            null,
            null
        );
    }
}
