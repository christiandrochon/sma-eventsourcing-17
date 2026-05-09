package fr.cdrochon.thymeleaffrontend.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FrontendTechnicalRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startMs = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            long durationMs = System.currentTimeMillis() - startMs;
            FrontendLoggers.error().error(
                    "UI_TECH_HTTP_ERROR method={} path={} status={} durationMs={} message={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    exception.getMessage(),
                    exception
            );
            throw exception;
        } finally {
            long durationMs = System.currentTimeMillis() - startMs;
            FrontendLoggers.access().info(
                    "UI_ACCESS_HTTP method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );
            FrontendLoggers.tech().info(
                    "UI_TECH_HTTP method={} path={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs
            );
        }
    }
}

