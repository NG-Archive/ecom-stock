package site.ng_archive.ecom_stock.domain.stock;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface StockRepository extends R2dbcRepository<Stock, Long> {

    @Query("SELECT s.* FROM stock s WHERE s.product_id = :productId FOR UPDATE")
    Mono<Stock> findByProductIdForUpdate(Long productId);

    Mono<Stock> findByProductId(Long productId);
}
