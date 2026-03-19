package site.ng_archive.ecom_stock.domain.stock;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockResponse;
import site.ng_archive.ecom_stock.domain.stock.dto.ReadStockResponse;

@Slf4j
@RequiredArgsConstructor
@RestController
public class StockController {

    private final StockService stockService;

    @GetMapping("/{productId}/stock")
    public Mono<ReadStockResponse> readStock(@PathVariable Long productId) {
        return stockService.readStock(productId)
            .map(ReadStockResponse::from);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{productId}/stock")
    public Mono<CreateStockResponse> createStock(
        @PathVariable Long productId,
        @Valid @RequestBody CreateStockRequest request
    ) {
        if (!productId.equals(request.productId())) {
            return Mono.error(new IllegalArgumentException("stock.invalid.productid"));
        }

        return stockService.createStock(request.toCommand())
            .map(CreateStockResponse::from);
    }
}
