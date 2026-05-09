package fr.cdrochon.thymeleaffrontend.configuration;

import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;

    private final OAuth2AuthorizedClientService authorizedClientService;

    public WebClientConfig(ObjectProvider<OAuth2AuthorizedClientService> authorizedClientServiceProvider) {
        this.authorizedClientService = authorizedClientServiceProvider.getIfAvailable();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                        .baseUrl(externalServiceUrl)
                        .filter((request, next) -> {
                            long startMs = System.currentTimeMillis();
                            ClientRequest authenticatedRequest = withBearerTokenIfAvailable(request);
                            return next.exchange(authenticatedRequest)
                                       .doOnNext(response -> {
                                           long durationMs = System.currentTimeMillis() - startMs;
                                           FrontendLoggers.tech().info(
                                                   "UI_TECH_EXT_WEBCLIENT method={} uri={} status={} durationMs={}",
                                                    authenticatedRequest.method(),
                                                    authenticatedRequest.url(),
                                                   response.statusCode().value(),
                                                   durationMs
                                           );
                                       })
                                       .doOnError(exception -> {
                                           long durationMs = System.currentTimeMillis() - startMs;
                                           FrontendLoggers.error().error(
                                                   "UI_TECH_EXT_WEBCLIENT_ERROR method={} uri={} durationMs={} message={}",
                                                    authenticatedRequest.method(),
                                                    authenticatedRequest.url(),
                                                   durationMs,
                                                   exception.getMessage(),
                                                   exception
                                           );
                                       });
                        })
                        .build();
    }

    private ClientRequest withBearerTokenIfAvailable(ClientRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authorizedClientService == null || !(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
                return request;
            }

            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (client == null || client.getAccessToken() == null || client.getAccessToken().getTokenValue() == null) {
                return request;
            }

            return ClientRequest.from(request)
                    .headers(headers -> headers.setBearerAuth(client.getAccessToken().getTokenValue()))
                    .build();
        } catch (Exception exception) {
            FrontendLoggers.tech().warn("UI_TECH_AUTH_RELAY_SKIPPED reason={}", exception.getMessage());
            return request;
        }
    }

    //UTILE POUR DEBUGUER ET VOIR LE CONTENU DU JSON mais genere un httpcode 302 de redirection (en plus des requetes avec httpclient)
//        @Bean
//        public WebClient webClient() {
//            return WebClient.builder()
//                            .baseUrl(externalServiceUrl)
//                            .filter((request, next) -> {
//                                System.out.println("Request: " + request.url());
//                                return next.exchange(request).doOnNext(response -> {
//                                    response.body((clientHttpResponse, context) -> {
//                                        clientHttpResponse.getBody().subscribe(dataBuffer -> {
//                                            byte[] bytes = new byte[dataBuffer.readableByteCount()];
//                                            dataBuffer.read(bytes);
//                                            System.out.println("Response body: " + new String(bytes, StandardCharsets.UTF_8));
//                                        });
//                                        return Mono.empty();
//                                    });
//                                });
//                            })
//                            .build();
//        }
    
    // PERMET DE LIMITER LA PROFONDEUR DU JSON
    //    @Bean
    //    public WebClient webClient() {
    //        ObjectMapper objectMapper = new ObjectMapper();
    //        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    //
    //        // Create custom StreamWriteConstraints
    //        StreamWriteConstraints streamWriteConstraints = StreamWriteConstraints.builder()
    //                                                                              .maxNestingDepth(2000)
    //                                                                              .build();
    //
    //        // Set custom StreamWriteConstraints to ObjectMapper
    //        objectMapper.getFactory().setStreamWriteConstraints(streamWriteConstraints);
    //
    //        return WebClient.builder()
    //                        .baseUrl(externalServiceUrl)
    //                        .exchangeStrategies(ExchangeStrategies.builder()
    //                                                              .codecs(configurer -> configurer.defaultCodecs().jackson2JsonDecoder(new
    //                                                              Jackson2JsonDecoder(objectMapper)))
    //                                                              .codecs(configurer -> configurer.defaultCodecs().jackson2JsonEncoder(new
    //                                                              Jackson2JsonEncoder(objectMapper)))
    //                                                              .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10
    //                                                              MB buffer size
    //                                                              .build())
    //                        .build();
    //    }
    
    
    //        @Autowired
    //        private ObjectMapper objectMapper;
    //    public ObjectMapper objectMapper() {
    //        ObjectMapper mapper = new ObjectMapper();
    //        mapper.registerModule(new JavaTimeModule());
    //        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    //        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
    //        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    //        return mapper;
    //    }
    
    //    @Bean
    //    public WebClient webClient() {
    //        ExchangeStrategies strategies =
    //                ExchangeStrategies.builder()
    //                                  .codecs(clientDefaultCodecsConfigurer -> {
    //                                      clientDefaultCodecsConfigurer.defaultCodecs()
    //                                                                   .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper()));
    //                                      clientDefaultCodecsConfigurer.defaultCodecs()
    //                                                                   .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper()));
    //                                  })
    //                                  .build();
    //
    //        return WebClient.builder()
    //                        .exchangeStrategies(strategies)
    //                        .baseUrl(externalServiceUrl)
    //                        .build();
    //    }
    
    //    @Bean
    //    public WebClient webClient() {
    //        return WebClient.builder()
    //                        .codecs(configurer -> {
    //                            configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper()));
    //                            configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper()));
    //                        })
    //                        //                        .filter((request, next) -> {
    //                        //                            System.out.println("Request: " + request.url());
    //                        //                            return next.exchange(request).doOnNext(response -> {
    //                        //                                response.body((clientHttpResponse, context) -> {
    //                        //                                    clientHttpResponse.getBody().subscribe(dataBuffer -> {
    //                        //                                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
    //                        //                                        dataBuffer.read(bytes);
    //                        //                                        System.out.println("Response body: " + new String(bytes, StandardCharsets.UTF_8));
    //                        //                                    });
    //                        //                                    return Mono.empty();
    //                        //                                });
    //                        //                            });
    //                        //                        })
    //                        .baseUrl(externalServiceUrl)
    //                        .build();
    //    }
    
    @Bean
    public RestClient restClient() {
        return RestClient.create(externalServiceUrl);
    }
}
