package site.ng_archive.ecom_stock.domain.stock;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import site.ng_archive.ecom_common.config.AcceptedTest;
import site.ng_archive.ecom_common.error.ErrorResponse;
import site.ng_archive.ecom_stock.EcomStockApplication;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockResponse;
import site.ng_archive.ecom_stock.domain.stock.dto.ProductResponse;
import site.ng_archive.ecom_stock.domain.stock.dto.ReadStockResponse;

import java.io.IOException;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;

@Slf4j
@ContextConfiguration(classes = {EcomStockApplication.class})
class StockControllerTest extends AcceptedTest {

    private final MockWebServer mockProductServer;

    @Autowired
    private StockTestTemplate stockTestTemplate;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    StockControllerTest() {
        mockProductServer = new MockWebServer();
    }

    @BeforeEach
    void init() throws IOException {
        mockProductServer.start(8081);
    }

    @AfterEach
    void destroy() throws IOException {
        mockProductServer.close();
        mockProductServer.shutdown();
    }

    @Test
    void 재고단건조회() {

        Long productId = 1L;
        stockTestTemplate.createStock(productId, 100L);

        mockProductServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(toJson(new ProductResponse(productId, "상품명", 20000L))));

        ReadStockResponse response =
            given()
                .contentType(ContentType.JSON)
                .pathParam("productId", productId)
                .consumeWith(document(
                    info()
                        .tag("Stock")
                        .summary("재고 조회")
                        .description("상품 ID를 사용하여 해당 상품의 재고를 조회합니다.")
                        .pathParameters(
                            parameterWithName("productId").description("상품 아이디")
                        )
                        .responseFields(
                            field(ReadStockResponse.class, "productId", "상품 ID"),
                            field(ReadStockResponse.class, "quantity", "재고 수량")
                        )
                ))
                .get("/{productId}/stock")
                .then()
                .status(HttpStatus.OK)
                .log().all()
                .extract().body().as(ReadStockResponse.class);

        Assertions.assertThat(response.productId()).isEqualTo(productId);

    }

    @Test
    void 재고생성() {

        Long productId = 1L;
        CreateStockRequest createStockRequest = new CreateStockRequest(productId, 100L);

        mockProductServer.enqueue(new MockResponse()
            .addHeader("Content-Type", "application/json")
            .setBody(toJson(new ProductResponse(productId, "상품명", 20000L))));

        CreateStockResponse response =
            given()
                .contentType(ContentType.JSON)
                .pathParam("productId", productId)
                .body(createStockRequest)
                .consumeWith(document(
                    info()
                        .tag("Stock")
                        .summary("재고 생성")
                        .description("상품 ID를 사용하여 재고를 생성합니다.")
                        .pathParameters(
                            parameterWithName("productId").description("상품 아이디")
                        )
                        .requestFields(
                            field(CreateStockRequest.class, "productId", "상품 ID"),
                            field(CreateStockRequest.class, "quantity", "재고 수량")
                        )
                        .responseFields(
                            field(CreateStockResponse.class, "id", "재고 ID"),
                            field(CreateStockResponse.class, "productId", "상품 ID"),
                            field(CreateStockResponse.class, "quantity", "재고 수량")
                        )
                ))
                .post("/{productId}/stock")
                .then()
                .status(HttpStatus.CREATED)
                .log().all()
                .extract().body().as(CreateStockResponse.class);

        Assertions.assertThat(response.productId()).isEqualTo(productId);
        Assertions.assertThat(response.quantity()).isEqualTo(100L);

        Stock saved = stockRepository.findByProductId(productId).block();
        StockHistory history = stockHistoryRepository.findByStockId(saved.id()).blockFirst();

        Assertions.assertThat(history.stockId()).isEqualTo(saved.id());
        Assertions.assertThat(history.changeQuantity()).isEqualTo(100L);
        Assertions.assertThat(history.totalQuantity()).isEqualTo(100L);
        log.info("history.createdAt()={}", history.createdAt());
    }

    @Test
    void 재고생성_유효하지않은_productId() {

        Long invalidProductId = 2L;
        CreateStockRequest createStockRequest = new CreateStockRequest(1L, 100L);

        ErrorResponse response =
            given()
                .contentType(ContentType.JSON)
                .pathParam("productId", invalidProductId)
                .body(createStockRequest)
                .consumeWith(document(
                    info()
                        .tag("Stock")
                        .summary("재고 생성")
                        .description("상품 ID를 사용하여 재고를 생성합니다.")
                        .pathParameters(
                            parameterWithName("productId").description("상품 아이디")
                        )
                        .requestFields(
                            field(CreateStockRequest.class, "productId", "상품 ID"),
                            field(CreateStockRequest.class, "quantity", "재고 수량")
                        )
                        .responseFields(
                            field(ErrorResponse.class, "errorCode", "오류 코드"),
                            field(ErrorResponse.class, "message", "오류 메시지")
                        )
                ))
                .post("/{productId}/stock")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .log().all()
                .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("stock.invalid.productid");
        Assertions.assertThat(response.message()).isEqualTo("요청한 productId가 올바르지 않습니다.");
    }

}