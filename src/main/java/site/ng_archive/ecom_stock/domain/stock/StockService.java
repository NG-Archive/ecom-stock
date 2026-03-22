package site.ng_archive.ecom_stock.domain.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;
import site.ng_archive.ecom_common.handler.EntityNotFoundException;
import site.ng_archive.ecom_stock.domain.stock.dto.AddStockCommand;
import site.ng_archive.ecom_stock.domain.stock.dto.CancelStockCommand;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockCommand;
import site.ng_archive.ecom_stock.domain.stock.dto.DeductStockCommand;
import site.ng_archive.ecom_stock.domain.stock.dto.ProductResponse;
import site.ng_archive.ecom_stock.domain.stock.requester.ProductRequester;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final ProductRequester productRequester;

    @Transactional
    public Mono<Stock> createStock(CreateStockCommand command) {
        return productRequester.getProduct(command.productId())
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))))
            .flatMap(product -> stockRepository.save(command.toEntity()))
            .onErrorMap(DuplicateKeyException.class, e -> new IllegalArgumentException("stock.already.exists"))
            .flatMap(stock -> stockHistoryRepository.save(StockHistory.create(stock))
                .thenReturn(stock));
    }

    public Mono<Stock> readStock(Long productId) {
        Mono<ProductResponse> productMono = productRequester.getProduct(productId)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))));

        Mono<Stock> stockMono = stockRepository.findByProductId(productId)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("stock.notfound"))));

        return Mono.zip(productMono, stockMono, (product, stock) -> stock);
    }

    @Transactional
    public Mono<Stock> deductStock(DeductStockCommand command) {
        return productRequester.getProduct(command.productId())
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))))
            .flatMap(product -> stockRepository.findByProductIdForUpdate(command.productId()))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("stock.notfound"))))
            .flatMap(stock -> stockRepository.save(stock.deduct(command.quantity())))
            .flatMap(deducted -> stockHistoryRepository
                .save(StockHistory.createDeduct(deducted, command.orderId(), command.quantity()))
                .thenReturn(deducted)
            );
    }

    @Transactional
    public Mono<Stock> addStock(AddStockCommand command) {
        return productRequester.getProduct(command.productId())
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))))
            .flatMap(product -> stockRepository.findByProductIdForUpdate(command.productId()))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("stock.notfound"))))
            .flatMap(stock -> stockRepository.save(stock.add(command.quantity())))
            .flatMap(added -> stockHistoryRepository
                .save(StockHistory.createAdded(added, command.quantity()))
                .thenReturn(added)
            );
    }

    @Transactional
    public Mono<Stock> cancelStock(CancelStockCommand command) {
        return productRequester.getProduct(command.productId())
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("product.notfound"))))
            .flatMap(product -> stockRepository.findByProductIdForUpdate(command.productId()))
            .switchIfEmpty(Mono.defer(() -> Mono.error(new EntityNotFoundException("stock.notfound"))))
            .zipWhen(stock -> stockHistoryRepository.findByStockIdAndOrderId(stock.id(), command.orderId())
                    .collectList())
            .flatMap(t -> {
                Stock stock = t.getT1();
                List<StockHistory> histories = t.getT2();
                if(histories.isEmpty()) return Mono.error(new IllegalArgumentException("stock.history.notfound"));

                Long outQuantitySum = getSumByType(histories, ChangeType.OUT);
                Long cancelledQuantitySum = getSumByType(histories, ChangeType.CANCEL);
                Long cancelQuantity = outQuantitySum - cancelledQuantitySum;
                if(cancelQuantity <= 0) return Mono.error(new IllegalArgumentException("stock.history.notfound"));

                return stockRepository.save(stock.cancel(cancelQuantity))
                    .map(canceled -> Tuples.of(canceled, cancelQuantity));
            })
            .flatMap(t -> {
                Stock canceled = t.getT1();
                Long cancelQuantity = t.getT2();
                return stockHistoryRepository
                    .save(StockHistory.createCanceled(canceled, command.orderId(), cancelQuantity))
                    .thenReturn(canceled);
                }
            );
    }

    private static long getSumByType(List<StockHistory> histories, ChangeType type) {
        return histories.stream()
            .filter(history -> history.type().equals(type))
            .mapToLong(StockHistory::changeQuantity)
            .sum();
    }
}