package site.ng_archive.ecom_stock.domain.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.ng_archive.ecom_common.config.EnableCommonTestFixtures;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockCommand;
import site.ng_archive.ecom_stock.domain.stock.dto.DeductStockCommand;
import site.ng_archive.ecom_stock.domain.stock.dto.ProductResponse;

import java.io.IOException;

@Slf4j
@EnableCommonTestFixtures
@Component
public class StockTestTemplate {


    private final MockWebServer mockProductServer;

    @Autowired
    private StockService stockService;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper objectMapper;

    public StockTestTemplate() {
        this.mockProductServer = new MockWebServer();
    }

    public void serverInit() {
        try {
            mockProductServer.start(8081);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void serverDestroy() {
        try {
            mockProductServer.close();
            mockProductServer.shutdown();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Stock createStock(Long productId, Long quantity) {
        createProductResponse(productId);
        CreateStockCommand createStockCommand = new CreateStockCommand(productId, quantity);
        return stockService.createStock(createStockCommand).block();
    }

    public void createProductResponse(Long productId) {
        mockProductServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(toJson(new ProductResponse(productId, "상품명", 20000L))));
    }

    public Stock deduct(Long productId, Long orderId, Long deductQuantity) {
        createProductResponse(productId);
        DeductStockCommand deductStockCommand = new DeductStockCommand(productId, orderId, deductQuantity);
        return stockService.deductStock(deductStockCommand).block();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
