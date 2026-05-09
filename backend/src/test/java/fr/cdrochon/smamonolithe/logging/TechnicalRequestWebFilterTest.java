package fr.cdrochon.smamonolithe.logging;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class TechnicalRequestWebFilterTest {

    @Test
    void shouldPassRequestThroughChain() {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter(NoopAuditServiceFactory.noop());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/queries/vehicules").build());
        WebFilterChain chain = mock(WebFilterChain.class);

        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
        verify(chain, times(1)).filter(exchange);
    }

    @Test
    void shouldPropagateErrorFromChain() {
        TechnicalRequestWebFilter filter = new TechnicalRequestWebFilter(NoopAuditServiceFactory.noop());
        MockServerWebExchange exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/queries/error").build());
        WebFilterChain chain = mock(WebFilterChain.class);

        when(chain.filter(exchange)).thenReturn(Mono.error(new IllegalStateException("boom")));

        StepVerifier.create(filter.filter(exchange, chain))
                .expectError(IllegalStateException.class)
                .verify();
        verify(chain, times(1)).filter(exchange);
    }
}

