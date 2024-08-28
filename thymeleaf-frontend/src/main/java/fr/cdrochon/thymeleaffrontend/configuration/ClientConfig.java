package fr.cdrochon.thymeleaffrontend.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {
    
    @Value("${external.service.url}")
    private String externalServiceUrl;
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                        .baseUrl(externalServiceUrl)
                        .build();
    }
    
    //UTILE POUR DEBUGUER ET VOIR LE CONTENU DU JSON mais genere un httpcode 302 de redirection (en plus des requetes avec httpclient)
    //    @Bean
    //    public WebClient webClient() {
    //        return WebClient.builder()
    //                        .baseUrl(externalServiceUrl)
    //                        .filter((request, next) -> {
    //                            System.out.println("Request: " + request.url());
    //                            return next.exchange(request).doOnNext(response -> {
    //                                response.body((clientHttpResponse, context) -> {
    //                                    clientHttpResponse.getBody().subscribe(dataBuffer -> {
    //                                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
    //                                        dataBuffer.read(bytes);
    //                                        System.out.println("Response body: " + new String(bytes, StandardCharsets.UTF_8));
    //                                    });
    //                                    return Mono.empty();
    //                                });
    //                            });
    //                        })
    //                        .build();
    //    }
    
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
