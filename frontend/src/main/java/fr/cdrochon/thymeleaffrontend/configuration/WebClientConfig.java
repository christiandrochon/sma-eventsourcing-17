package fr.cdrochon.thymeleaffrontend.configuration;

import fr.cdrochon.thymeleaffrontend.logging.FrontendLoggers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public WebClientConfig(ClientRegistrationRepository clientRegistrationRepository,
                           OAuth2AuthorizedClientRepository authorizedClientRepository) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .clientCredentials()
                .build();
        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientRepository);
        manager.setAuthorizedClientProvider(authorizedClientProvider);
        this.authorizedClientManager = manager;
    }

    @Bean
    public WebClient webClient() {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2.setDefaultOAuth2AuthorizedClient(true);

        return WebClient.builder()
                        .baseUrl(externalServiceUrl)
                        .apply(oauth2.oauth2Configuration())
                        .filter((request, next) -> {
                            long startMs = System.currentTimeMillis();
                            return next.exchange(ClientRequest.from(request).build())
                                       .retryWhen(reactor.util.retry.Retry.backoff(3, java.time.Duration.ofMillis(200))
                                               .doBeforeRetry(signal -> FrontendLoggers.tech().warn(
                                                       "UI_TECH_RETRY attempt={} method={} uri={}",
                                                       signal.totalRetries() + 1,
                                                       request.method(),
                                                       request.url()
                                               )))
                                       .doOnNext(response -> {
                                            long durationMs = System.currentTimeMillis() - startMs;
                                            FrontendLoggers.tech().info(
                                                    "UI_TECH_EXT_WEBCLIENT method={} uri={} status={} durationMs={}",
                                                     request.method(),
                                                     request.url(),
                                                    response.statusCode().value(),
                                                    durationMs
                                            );
                                        })
                                        .doOnError(exception -> {
                                            long durationMs = System.currentTimeMillis() - startMs;
                                            FrontendLoggers.error().error(
                                                    "UI_TECH_EXT_WEBCLIENT_ERROR method={} uri={} durationMs={} message={}",
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
