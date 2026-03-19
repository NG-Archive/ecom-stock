package site.ng_archive.ecom_stock.domain.stock;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface StockRepository extends R2dbcRepository<Stock, Long> {

    Mono<Stock> findByProductId(Long productId);
}
