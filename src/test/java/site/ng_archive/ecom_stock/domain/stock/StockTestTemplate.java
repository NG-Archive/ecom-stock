package site.ng_archive.ecom_stock.domain.stock;

import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.ng_archive.ecom_common.config.EnableCommonTestFixtures;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockCommand;

@EnableCommonTestFixtures
@Component
public class StockTestTemplate {

    @Autowired
    private StockService stockService;

    @Autowired
    private Faker faker;

    public Stock createStock(Long productId, Long quantity) {
        CreateStockCommand createStockCommand = new CreateStockCommand(productId, quantity);
        return stockService.createStock(createStockCommand).block();
    }


}
