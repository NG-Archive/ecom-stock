package site.ng_archive.ecom_stock.domain.stock;

import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import site.ng_archive.ecom_common.auth.Role;
import site.ng_archive.ecom_common.auth.UserContext;
import site.ng_archive.ecom_common.auth.token.TokenUtil;
import site.ng_archive.ecom_common.config.AcceptedTest;
import site.ng_archive.ecom_common.error.ErrorResponse;
import site.ng_archive.ecom_stock.EcomStockApplication;
import site.ng_archive.ecom_stock.domain.stock.dto.AddStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.CancelStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.CreateStockResponse;
import site.ng_archive.ecom_stock.domain.stock.dto.DeductStockRequest;
import site.ng_archive.ecom_stock.domain.stock.dto.ReadStockResponse;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static io.restassured.module.webtestclient.RestAssuredWebTestClient.given;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(classes = {EcomStockApplication.class})
class StockControllerTest extends AcceptedTest {

    @Autowired
    private StockTestTemplate stockTestTemplate;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @BeforeAll
    void init() {
        stockTestTemplate.serverInit();
    }

    @AfterAll
    void destroy()  {
        stockTestTemplate.serverDestroy();
    }

    @AfterEach
    void afterEach() {
        stockHistoryRepository.deleteAll().block();
        stockRepository.deleteAll().block();
    }

    @Test
    void 재고단건조회() {

        Long productId = 1L;
        Long memberId = 10L;
        stockTestTemplate.createStock(productId, 100L, memberId);
        stockTestTemplate.createProductResponse(productId, memberId);

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
        Assertions.assertThat(response.quantity()).isEqualTo(100L);

    }

    @Test
    void 재고생성() {

        Long productId = 1L;
        Long memberId = 10L;
        CreateStockRequest createStockRequest = new CreateStockRequest(productId, 100L);
        stockTestTemplate.createProductResponse(productId, memberId);
        String token = createTestJwtToken(memberId, Role.ROLES.SELLER);

        CreateStockResponse response =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
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
        Long memberId = 10L;
        String token = createTestJwtToken(memberId, Role.ROLES.SELLER);

        ErrorResponse response =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
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
        Stock stock = stockRepository.findByProductId(createStockRequest.productId()).block();
        Assertions.assertThat(stock).isNull();
    }

    @Test
    void 재고생성_소유자아님() {

        Long productId = 1L;
        Long memberId = 10L;
        Long forbiddenMemberId = 20L;
        CreateStockRequest createStockRequest = new CreateStockRequest(productId, 100L);
        stockTestTemplate.createProductResponse(productId, forbiddenMemberId);
        String token = createTestJwtToken(memberId, Role.ROLES.SELLER);

        ErrorResponse response =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
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
                            field(ErrorResponse.class, "errorCode", "오류 코드"),
                            field(ErrorResponse.class, "message", "오류 메시지")
                        )
                ))
                .post("/{productId}/stock")
                .then()
                .status(HttpStatus.FORBIDDEN)
                .log().all()
                .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("stock.forbidden");
        Assertions.assertThat(response.message()).isEqualTo("해당 상품에 대한 재고 관리 권한이 없습니다.");
        Stock stock = stockRepository.findByProductId(createStockRequest.productId()).block();
        Assertions.assertThat(stock).isNull();
    }

    @Test
    void 재고차감() {

        Long productId = 1L;
        Long orderId = 1L;
        Long deductQuantity = 30L;
        Long memberId = 10L;
        Stock stock = stockTestTemplate.createStock(productId, 100L, memberId);
        stockTestTemplate.createProductResponse(productId, memberId);
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
        Long memberId = 10L;
        Stock stock = stockTestTemplate.createStock(productId, 10L, memberId);
        stockTestTemplate.createProductResponse(productId, memberId);
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
        Assertions.assertThat(response.message()).isEqualTo("수량 정보가 올바르지 않습니다.");

        Stock reloaded = stockRepository.findByProductId(productId).block();
        List<StockHistory> histories = stockHistoryRepository.findByStockId(stock.id()).collectList().block();
        Assertions.assertThat(reloaded.quantity()).isEqualTo(10L);
        Assertions.assertThat(histories).hasSize(1);
    }

    @Test
    void 재고차감_동시성테스트() throws InterruptedException {

        long productId = 1L;
        int threadCount = 10;
        Long memberId = 10L;

        stockTestTemplate.createStock(productId, (long) threadCount, memberId);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            long orderId = i;
            executorService.submit(() -> {
                try {
                    stockTestTemplate.deduct(productId, orderId, 1L, memberId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Stock stock = stockRepository.findByProductId(productId).block();
        List<StockHistory> histories = stockHistoryRepository.findByStockId(stock.id()).collectList().block();
        Assertions.assertThat(histories.size()).isEqualTo(threadCount+1);
        Assertions.assertThat(stock.quantity()).isEqualTo(0L);
    }

    @Test
    void 재고추가() {

        Long productId = 1L;
        Long addQuantity = 50L;
        Long memberId = 10L;
        Stock stock = stockTestTemplate.createStock(productId, 100L, memberId);
        stockTestTemplate.createProductResponse(productId, memberId);
        AddStockRequest request = new AddStockRequest(productId, addQuantity);
        String token = createTestJwtToken(memberId, Role.ROLES.SELLER);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .pathParam("productId", productId)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Stock")
                    .summary("재고 추가")
                    .description("상품 ID를 사용하여 재고를 추가합니다.")
                    .pathParameters(
                        parameterWithName("productId").description("상품 아이디")
                    )
                    .requestFields(
                        field(AddStockRequest.class, "productId", "상품 ID"),
                        field(AddStockRequest.class, "quantity", "추가할 수량")
                    )
            ))
            .patch("/{productId}/stock/add")
            .then()
            .status(HttpStatus.NO_CONTENT)
            .log().all();

        Stock updated = stockRepository.findByProductId(productId).block();
        StockHistory history = stockHistoryRepository.findByStockId(stock.id())
            .filter(h -> h.type() == ChangeType.IN && h.changeQuantity().equals(addQuantity))
            .blockFirst();

        Assertions.assertThat(updated.quantity()).isEqualTo(150L);
        Assertions.assertThat(history.stockId()).isEqualTo(stock.id());
        Assertions.assertThat(history.type()).isEqualTo(ChangeType.IN);
        Assertions.assertThat(history.changeQuantity()).isEqualTo(addQuantity);
        Assertions.assertThat(history.totalQuantity()).isEqualTo(150L);
    }

    @Test
    void 재고추가_오류응답() {

        Long invalidProductId = 1L;
        Long addQuantity = 50L;
        Long memberId = 10L;
        stockTestTemplate.createProductResponse(invalidProductId, memberId);
        AddStockRequest request = new AddStockRequest(invalidProductId, addQuantity);
        String token = createTestJwtToken(memberId, Role.ROLES.SELLER);

        ErrorResponse response =
            given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .pathParam("productId", invalidProductId)
                .body(request)
                .consumeWith(document(
                    info()
                        .tag("Stock")
                        .summary("재고 추가")
                        .description("상품 ID를 사용하여 재고를 추가합니다.")
                        .pathParameters(
                            parameterWithName("productId").description("상품 아이디")
                        )
                        .requestFields(
                            field(AddStockRequest.class, "productId", "상품 ID"),
                            field(AddStockRequest.class, "quantity", "추가할 수량")
                        )
                        .responseFields(
                            field(ErrorResponse.class, "errorCode", "오류 코드"),
                            field(ErrorResponse.class, "message", "오류 메시지")
                        )
                ))
                .patch("/{productId}/stock/add")
                .then()
                .status(HttpStatus.NOT_FOUND)
                .log().all()
                .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("stock.notfound");
        Assertions.assertThat(response.message()).isEqualTo("재고 데이터가 존재하지 않습니다.");
    }

    @Test
    void 재고추가_동시성테스트() throws InterruptedException {

        long productId = 1L;
        int threadCount = 10;
        long addQuantity = 5L;
        Long memberId = 10L;

        stockTestTemplate.createStock(productId, 0L, memberId);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockTestTemplate.add(productId, addQuantity, memberId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        Stock stock = stockRepository.findByProductId(productId).block();
        List<StockHistory> histories = stockHistoryRepository.findByStockId(stock.id()).collectList().block();
        Assertions.assertThat(histories.size()).isEqualTo(threadCount + 1);
        Assertions.assertThat(stock.quantity()).isEqualTo(50L);
    }

    @Test
    void 재고추가_소유자아님() {

        Long productId = 1L;
        Long addQuantity = 50L;
        Long memberId = 10L;
        Long forbiddenMemberId = 20L;
        stockTestTemplate.createStock(productId, 100L, memberId);
        stockTestTemplate.createProductResponse(productId, memberId);
        AddStockRequest request = new AddStockRequest(productId, addQuantity);
        String token = createTestJwtToken(forbiddenMemberId, Role.ROLES.SELLER);

        ErrorResponse response = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .pathParam("productId", productId)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Stock")
                    .summary("재고 추가")
                    .description("상품 ID를 사용하여 재고를 추가합니다.")
                    .pathParameters(
                        parameterWithName("productId").description("상품 아이디")
                    )
                    .responseFields(
                        field(ErrorResponse.class, "errorCode", "오류 코드"),
                        field(ErrorResponse.class, "message", "오류 메시지")
                    )
            ))
            .patch("/{productId}/stock/add")
            .then()
            .status(HttpStatus.FORBIDDEN)
            .log().all()
            .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("stock.forbidden");
        Assertions.assertThat(response.message()).isEqualTo("해당 상품에 대한 재고 관리 권한이 없습니다.");
    }

    @Test
    void 재고취소() {

        Long productId = 1L;
        Long orderId = 1L;
        Long deductQuantity = 30L;
        Long cancelQuantity = 30L;
        Long memberId = 10L;

        Stock stock = stockTestTemplate.createStock(productId, 100L, memberId);
        stockTestTemplate.deduct(productId, orderId, deductQuantity, memberId);
        stockTestTemplate.createProductResponse(productId, memberId);

        CancelStockRequest request = new CancelStockRequest(productId, orderId);

        given()
            .contentType(ContentType.JSON)
            .pathParam("productId", productId)
            .body(request)
            .consumeWith(document(
                info()
                    .tag("Stock")
                    .summary("재고 취소")
                    .description("주문 ID를 사용하여 차감된 재고를 취소하고 복구합니다.")
                    .pathParameters(
                        parameterWithName("productId").description("상품 아이디")
                    )
                    .requestFields(
                        field(CancelStockRequest.class, "productId", "상품 ID"),
                        field(CancelStockRequest.class, "orderId", "주문 ID")
                    )
            ))
            .patch("/{productId}/stock/cancel")
            .then()
            .status(HttpStatus.NO_CONTENT)
            .log().all();

        Stock updated = stockRepository.findByProductId(productId).block();
        StockHistory history = stockHistoryRepository.findByStockId(stock.id())
            .filter(h -> h.type() == ChangeType.CANCEL && h.orderId() != null && h.orderId().equals(orderId))
            .blockFirst();

        Assertions.assertThat(updated.quantity()).isEqualTo(100L);
        Assertions.assertThat(history.stockId()).isEqualTo(stock.id());
        Assertions.assertThat(history.orderId()).isEqualTo(orderId);
        Assertions.assertThat(history.type()).isEqualTo(ChangeType.CANCEL);
        Assertions.assertThat(history.changeQuantity()).isEqualTo(cancelQuantity);
        Assertions.assertThat(history.totalQuantity()).isEqualTo(100L);
    }

    @Test
    void 재고취소_오류응답() {

        Long productId = 1L;
        Long invalidOrderId = 999L;
        Long memberId = 10L;

        stockTestTemplate.createStock(productId, 100L, memberId);
        stockTestTemplate.createProductResponse(productId, memberId);

        CancelStockRequest request = new CancelStockRequest(productId, invalidOrderId);

        ErrorResponse response =
            given()
                .contentType(ContentType.JSON)
                .pathParam("productId", productId)
                .body(request)
                .consumeWith(document(
                    info()
                        .tag("Stock")
                        .summary("재고 취소")
                        .description("주문 ID를 사용하여 차감된 재고를 취소하고 복구합니다.")
                        .pathParameters(
                            parameterWithName("productId").description("상품 아이디")
                        )
                        .requestFields(
                            field(CancelStockRequest.class, "productId", "상품 ID"),
                            field(CancelStockRequest.class, "orderId", "주문 ID")
                        )
                        .responseFields(
                            field(ErrorResponse.class, "errorCode", "오류 코드"),
                            field(ErrorResponse.class, "message", "오류 메시지")
                        )
                ))
                .patch("/{productId}/stock/cancel")
                .then()
                .status(HttpStatus.BAD_REQUEST)
                .log().all()
                .extract().body().as(ErrorResponse.class);

        Assertions.assertThat(response.errorCode()).isEqualTo("stock.history.notfound");
        Assertions.assertThat(response.message()).isEqualTo("재고 변동 내역을 찾을 수 없습니다.");

        Stock stock = stockRepository.findByProductId(productId).block();
        Assertions.assertThat(stock.quantity()).isEqualTo(100L);
    }

    private String createTestJwtToken(Long memberId, String role) {
        return TokenUtil.getSign(UserContext.of(memberId, role));
    }

}
