package site.ng_archive.ecom_stock.global.webclient;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productWebClient(
        WebClient.Builder builder,
        @Value("${app.product-api.url:http://localhost:8081}") String url,
        @Value("${app.product-api.pool-size:100}") Integer poolSize,
        @Value("${app.product-api.pool-acquire-time:3}") Integer acquireTime,
        @Value("${app.product-api.pool-idle-time:30}") Integer idleTime,
        @Value("${app.product-api.pool-max-time:60}") Integer maxTime,
        @Value("${app.product-api.pool-evict-time:30}") Integer evictTime,
        @Value("${app.product-api.time-out:3}") Integer timeout
    ) {
        return createWebClient(
            builder,
            "product-api",
            url,
            poolSize,
            acquireTime,
            idleTime,
            maxTime,
            evictTime,
            timeout
        );
    }

    private WebClient createWebClient(
        WebClient.Builder builder,
        String poolName,
        String baseUrl,
        Integer poolSize,
        Integer acquireTime,
        Integer idleSeconds,
        Integer maxTime,
        Integer evictTime,
        Integer timeout) {

        // 1. Connection Pool 설정 (더 세밀하게 제어)
        ConnectionProvider provider = ConnectionProvider.builder(poolName)
            .maxConnections(poolSize)
            // 연결을 가져오기 위해 기다리는 시간
            .pendingAcquireTimeout(Duration.ofSeconds(acquireTime))
            // 유휴 연결 유지 시간
            .maxIdleTime(Duration.ofSeconds(idleSeconds))
            // 연결의 총 수명
            .maxLifeTime(Duration.ofSeconds(maxTime))
            // 백그라운드에서 유휴 연결 제거 시간
            .evictInBackground(Duration.ofSeconds(evictTime))
            .build();

        // 2. HttpClient 설정 (Connect & Read/Write Timeout)
        HttpClient httpClient = HttpClient.create(provider)
            // 커넥션 타임아웃
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.min(timeout * 1000, 3000))
            // 응답 타임아웃
            .responseTimeout(Duration.ofSeconds(timeout));

        // 3. WebClient 빌드
        return builder.clone()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

}
