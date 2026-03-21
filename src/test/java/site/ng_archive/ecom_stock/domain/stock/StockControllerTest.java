package site.ng_archive.ecom_stock.domain.stock;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
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
import site.ng_archive.ecom_stock.domain.stock.dto.DeductStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.ReadStockResponse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;

@Slf4j
@ContextConfiguration(classes = {EcomStockApplication.class})
class StockControllerTest extends AcceptedTest {

    @Autowired
    private StockTestTemplate stockTestTemplate;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @BeforeEach
    void init() throws IOException {
        stockTestTemplate.serverInit();
    }

    @AfterEach
    void destroy() throws IOException {
        stockTestTemplate.serverDestroy();
    }

    @Test
    void 재고단건조회() {

        Long productId = 1L;
        stockTestTemplate.createStock(productId, 100L);
        stockTestTemplate.createProductResponse(productId);

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
        stockTestTemplate.createProductResponse(productId);

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
    }

    @Test
    void 재고생성_오류응답() {

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

    @Test
    void 재고차감() {

        Long productId = 1L;
        Long orderId = 1L;
        Long deductQuantity = 30L;
        Stock stock = stockTestTemplate.createStock(productId, 100L);
        stockTestTemplate.createProductResponse(productId);
        DeductStockRequest request = new DeductStockRequest(productId, orderId, deductQuantity);

        given()
            .contentType(ContentType.JSON)
            .pathParam("productId", productId)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Stock")
                    .summary("재고 차감")
                    .description("상품ID와 주문 ID를 사용하여 재고를 차감합니다.")
                    .pathParameters(
                        parameterWithName("productId").description("상품 아이디")
                    )
                    .requestFields(
                        field(DeductStockRequest.class, "productId", "상품 ID"),
                        field(DeductStockRequest.class, "orderId", "주문 ID"),
                        field(DeductStockRequest.class, "quantity", "차감할 수량(기존 재고보다 클 수 없음)")
                    )
            ))
            .patch("/{productId}/stock/deduct")
            .then()
            .status(HttpStatus.NO_CONTENT)
            .log().all();

        Stock updated = stockRepository.findByProductId(productId).block();
        StockHistory history = stockHistoryRepository.findByStockId(stock.id())
            .filter(h -> h.orderId() != null && h.orderId().equals(orderId))
            .blockFirst();

        Assertions.assertThat(updated.quantity()).isEqualTo(70L);
        Assertions.assertThat(history.stockId()).isEqualTo(stock.id());
        Assertions.assertThat(history.orderId()).isEqualTo(orderId);
        Assertions.assertThat(history.type()).isEqualTo(ChangeType.OUT);
        Assertions.assertThat(history.changeQuantity()).isEqualTo(deductQuantity);
        Assertions.assertThat(history.totalQuantity()).isEqualTo(70L);
    }

    @Test
    void 재고차감_재고초과() {

        Long productId = 1L;
        Long orderId = 1L;
        Long deductQuantity = 11L;
        Stock stock = stockTestTemplate.createStock(productId, 10L);
        stockTestTemplate.createProductResponse(productId);
        DeductStockRequest request = new DeductStockRequest(productId, orderId, deductQuantity);

        ErrorResponse response =
        given()
            .contentType(ContentType.JSON)
            .pathParam("productId", productId)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Stock")
                    .summary("재고 차감")
                    .description("상품ID와 주문 ID를 사용하여 재고를 차감합니다.")
                    .pathParameters(
                        parameterWithName("productId").description("상품 아이디")
                    )
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
            ))
            .patch("/{productId}/stock/deduct")
            .then()
            .status(HttpStatus.BAD_REQUEST)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("stock.invalid.quantity");
        Assertions.assertThat(response.message()).isEqualTo("재고 보다 많이 차감할 수 없습니다.");
    }

    @Test
    void 재고차감_동시성테스트() throws InterruptedException {

        long productId = 1L;
        int threadCount = 10;

        stockTestTemplate.createStock(productId, (long) threadCount);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long orderId = i;
            executorService.submit(() -> {
                try {
                    stockTestTemplate.deduct(productId, orderId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findByProductId(productId).block();
        List<StockHistory> histories = stockHistoryRepository.findByStockId(stock.id()).collectList().block();
        log.info("histories: {}", histories);
        Assertions.assertThat(histories.size()).isEqualTo(threadCount+1);
        Assertions.assertThat(stock.quantity()).isEqualTo(0L);
    }


}