package fr.cdrochon.thymeleaffrontend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder().baseUrl("external.service.url").build();
    }
    
    @Bean
    public RestClient restClient() {
        return RestClient.create("external.service.url");
    }
}
