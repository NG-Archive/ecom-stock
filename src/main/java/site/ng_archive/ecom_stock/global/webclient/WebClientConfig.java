package site.ng_archive.ecom_stock.global.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productWebClient(
        WebClient.Builder builder,
        @Value("${app.product-api.url}") String url
    ) {
        return builder.baseUrl(url).build();
    }
}
