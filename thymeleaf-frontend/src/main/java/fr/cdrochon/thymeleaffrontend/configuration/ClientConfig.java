package fr.cdrochon.thymeleaffrontend.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Configuration
public class ClientConfig {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
        @Autowired
        private ObjectMapper objectMapper;

    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                        .codecs(configurer -> {
                            configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
                            configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
                        })
                        .filter((request, next) -> {
                            System.out.println("Request: " + request.url());
                            return next.exchange(request).doOnNext(response -> {
                                response.body((clientHttpResponse, context) -> {
                                    clientHttpResponse.getBody().subscribe(dataBuffer -> {
                                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(bytes);
                                        System.out.println("Response body: " + new String(bytes, StandardCharsets.UTF_8));
                                    });
                                    return Mono.empty();
                                });
                            });
                        })
                        .baseUrl(externalServiceUrl)
                        .build();
    }
    
    @Bean
    public RestClient restClient() {
        return RestClient.create(externalServiceUrl);
    }
}
