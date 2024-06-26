//package fr.cdrochon.smamonolithe.garage.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiKey;
//import springfox.documentation.service.AuthorizationScope;
//import springfox.documentation.service.SecurityReference;
//import springfox.documentation.service.SecurityScheme;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.Docket;
//
//import java.util.Collections;
//import java.util.List;
//
//@Configuration
//public class SwaggerConfig {
//
////    @Bean
////    public Docket api() {
////        return new Docket(DocumentationType.OAS_30)
////                .select()
////                .apis(RequestHandlerSelectors.any())
////                .paths(PathSelectors.any())
////                .build()
////                .securitySchemes(Collections.singletonList(apiKey()))
////                .securityContexts(Collections.singletonList(securityContext()));
////    }
////
////    private SecurityScheme apiKey() {
////        return new ApiKey("JWT", "Authorization", "header");
////    }
////
////    private SecurityContext securityContext() {
////        return SecurityContext.builder()
////                              .securityReferences(defaultAuth())
////                              .build();
////    }
//
//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[] { authorizationScope };
//        return Collections.singletonList(new SecurityReference("JWT", authorizationScopes));
//    }
//}
