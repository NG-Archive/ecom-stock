package site.ng_archive.ecom_stock.domain.stock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
public class StockController {

    @GetMapping("/stock/{id}")
    public Mono<String> readMember(@PathVariable Long id) {
        return Mono.just("hello stock");
    }
}
