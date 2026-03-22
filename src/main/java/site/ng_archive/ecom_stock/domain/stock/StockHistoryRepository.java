package site.ng_archive.ecom_stock.domain.stock;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface StockHistoryRepository extends R2dbcRepository<StockHistory, Long> {
    Flux<StockHistory> findByStockId(Long stockId);
    Flux<StockHistory> findByStockIdAndOrderId(Long stockId, Long orderId);
}
