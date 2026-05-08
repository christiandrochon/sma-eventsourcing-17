package fr.cdrochon.smamonolithe.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class ClientWebConfig {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            long startMs = System.currentTimeMillis();
            try {
                var response = execution.execute(request, body);
                long durationMs = System.currentTimeMillis() - startMs;
                log.info(
                        "TECH_EXT_REST method={} uri={} status={} durationMs={}",
                        request.getMethod(),
                        request.getURI(),
                        response.getStatusCode().value(),
                        durationMs
                );
                return response;
            } catch (Exception exception) {
                long durationMs = System.currentTimeMillis() - startMs;
                log.error(
                        "TECH_EXT_REST_ERROR method={} uri={} durationMs={} message={}",
                        request.getMethod(),
                        request.getURI(),
                        durationMs,
                        exception.getMessage(),
                        exception
                );
                throw exception;
            }
        });
        return restTemplate;
    }
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                        .baseUrl(externalServiceUrl)
                        .filter((request, next) -> {
                            long startMs = System.currentTimeMillis();
                            return next.exchange(request)
                                       .doOnNext(response -> {
                                           long durationMs = System.currentTimeMillis() - startMs;
                                           log.info(
                                                   "TECH_EXT_WEBCLIENT method={} uri={} status={} durationMs={}",
                                                   request.method(),
                                                   request.url(),
                                                   response.statusCode().value(),
                                                   durationMs
                                           );
                                       })
                                       .doOnError(exception -> {
                                           long durationMs = System.currentTimeMillis() - startMs;
                                           log.error(
                                                   "TECH_EXT_WEBCLIENT_ERROR method={} uri={} durationMs={} message={}",
                                                   request.method(),
                                                   request.url(),
                                                   durationMs,
                                                   exception.getMessage(),
                                                   exception
                                           );
                                       });
                        })
                        .build();
    }
}
