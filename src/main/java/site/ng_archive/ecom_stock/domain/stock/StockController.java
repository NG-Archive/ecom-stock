package site.ng_archive.ecom_stock.domain.stock;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_common.auth.Role;
import site.ng_archive.ecom_common.auth.UserContext;
import site.ng_archive.ecom_common.auth.aspect.LoginUser;
import site.ng_archive.ecom_common.auth.aspect.RequireRoles;
import site.ng_archive.ecom_stock.domain.stock.dto.AddStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.CancelStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockResponse;
import site.ng_archive.ecom_stock.domain.stock.dto.DeductStockRequest;
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

    @RequireRoles
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{productId}/stock")
    public Mono<CreateStockResponse> createStock(
        @LoginUser UserContext user,
        @PathVariable Long productId,
        @Valid @RequestBody CreateStockRequest request
    ) {
        if (!productId.equals(request.productId())) {
            return Mono.error(new IllegalArgumentException("stock.invalid.productid"));
        }

        return stockService.createStock(request.toCommand(user.id()))
            .map(CreateStockResponse::from);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{productId}/stock/deduct")
    public Mono<Void> deductStock(
        @PathVariable Long productId,
        @Valid @RequestBody DeductStockRequest request
    ) {
        if (!productId.equals(request.productId())) {
            return Mono.error(new IllegalArgumentException("stock.invalid.productid"));
        }

        return stockService.deductStock(request.toCommand()).then();
    }

    @RequireRoles
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{productId}/stock/add")
    public Mono<Void> addStock(
        @LoginUser UserContext user,
        @PathVariable Long productId,
        @Valid @RequestBody AddStockRequest request
    ) {
        if (!productId.equals(request.productId())) {
            return Mono.error(new IllegalArgumentException("stock.invalid.productid"));
        }

        return stockService.addStock(request.toCommand(user.id())).then();
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/{productId}/stock/cancel")
    public Mono<Void> cancelStock(
        @PathVariable Long productId,
        @Valid @RequestBody CancelStockRequest request
    ) {
        if (!productId.equals(request.productId())) {
            return Mono.error(new IllegalArgumentException("stock.invalid.productid"));
        }

        return stockService.cancelStock(request.toCommand()).then();
    }


}
