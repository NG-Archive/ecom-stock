package site.ng_archive.ecom_stock.domain.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_common.handler.EntityNotFoundException;
import site.ng_archive.ecom_stock.domain.stock.client.ProductClient;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockCommand;
import site.ng_archive.ecom_stock.domain.stock.dto.DeductStockCommand;
import site.ng_archive.ecom_stock.domain.stock.dto.ProductResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final ProductClient productClient;

    @Transactional
    public Mono<Stock> createStock(CreateStockCommand command) {
        return productClient.getProduct(command.productId())
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))))
            .flatMap(product -> stockRepository.save(command.toEntity()))
            .flatMap(stock -> stockHistoryRepository.save(StockHistory.create(stock))
                .thenReturn(stock));
    }

    public Mono<Stock> readStock(Long productId) {
        Mono<ProductResponse> productMono = productClient.getProduct(productId)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))));

        Mono<Stock> stockMono = stockRepository.findByProductId(productId)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("stock.notfound"))));

        return Mono.zip(productMono, stockMono, (product, stock) -> stock);
    }

    @Transactional
    public Mono<Stock> deductStock(DeductStockCommand command) {
        Mono<ProductResponse> productMono = productClient.getProduct(command.productId())
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))));

        Mono<Stock> stockMono = stockRepository.findByProductIdForUpdate(command.productId())
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("stock.notfound"))));

        return Mono.zip(productMono, stockMono,
                (product, stock) -> stock.deduct(command.quantity()))
            .flatMap(stockRepository::save)
            .flatMap(deducted -> stockHistoryRepository
                .save(StockHistory.createDeduct(deducted, command.orderId(), command.quantity()))
                .thenReturn(deducted)
            );
    }
}