package site.ng_archive.ecom_stock.config;


import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.Locale;
import java.util.function.Consumer;

@Configuration
public class RestDocsConfig {

    public OperationRequestPreprocessor getRequestPreprocessor() {
        return Preprocessors.preprocessRequest(Preprocessors.prettyPrint());
    }

    public OperationResponsePreprocessor getResponsePreprocessor() {
        return Preprocessors.preprocessResponse(Preprocessors.prettyPrint());
    }

    @Bean
    public Consumer<EntityExchangeResult<byte[]>> restDocs() {
        return WebTestClientRestDocumentation.document(
                "{class-name}/{method-name}",
                getRequestPreprocessor(),
                getResponsePreprocessor()
        );
    }

    @Bean
    public Faker faker() {
        return new Faker(new Locale("ko"));
    }
}