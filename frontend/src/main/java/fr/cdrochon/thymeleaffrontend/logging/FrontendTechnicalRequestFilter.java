package fr.cdrochon.thymeleaffrontend.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
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
        String method = request.getMethod();
        String path = request.getRequestURI();
        boolean suspiciousMethod = !"GET".equals(method) && !"POST".equals(method) && !"HEAD".equals(method);
        boolean suspiciousPath = path.contains("..") || path.contains("//") || path.contains("%2e") || path.contains("%2f") || path.contains("\\");

        if(suspiciousMethod || suspiciousPath) {
            FrontendSecurityLoggers.security().warn(
                    "SEC_FRONTEND_ANOMALOUS_REQUEST method={} path={} suspiciousMethod={} suspiciousPath={}",
                    method,
                    path,
                    suspiciousMethod,
                    suspiciousPath
            );
        }

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
            int status = response.getStatus();

            if(status == HttpStatus.UNAUTHORIZED.value() || status == HttpStatus.FORBIDDEN.value()) {
                FrontendSecurityLoggers.security().warn(
                        "SEC_FRONTEND_ACCESS_DENIED method={} path={} status={} durationMs={}",
                        method,
                        path,
                        status,
                        durationMs
                );
            }

            FrontendLoggers.access().info(
                    "UI_ACCESS_HTTP method={} path={} status={} durationMs={}",
                    method,
                    path,
                    status,
                    durationMs
            );
            FrontendLoggers.tech().info(
                    "UI_TECH_HTTP method={} path={} status={} durationMs={}",
                    method,
                    path,
                    status,
                    durationMs
            );
        }
    }
}

