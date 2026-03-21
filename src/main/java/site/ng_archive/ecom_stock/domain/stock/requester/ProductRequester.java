package site.ng_archive.ecom_stock.domain.stock.requester;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import site.ng_archive.ecom_common.webclient.WebClientErrorHandler;
import site.ng_archive.ecom_stock.domain.stock.dto.ProductResponse;

@Component
@RequiredArgsConstructor
public class ProductRequester {

    private final WebClient productWebClient;

    public Mono<ProductResponse> getProduct(Long productId) {
        return productWebClient.get()
            .uri("/product/{id}", productId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, WebClientErrorHandler::handle)
            .bodyToMono(ProductResponse.class);
    }

    public Mono<Boolean> exists(Long productId) {
        return getProduct(productId)
            .hasElement();
    }
}
