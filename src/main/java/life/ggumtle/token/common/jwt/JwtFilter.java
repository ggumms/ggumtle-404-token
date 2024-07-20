package life.ggumtle.token.common.jwt;

import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(-1)
@RequiredArgsConstructor
public class JwtFilter implements WebFilter {

    private final JwtManager jwtManager;

    private static final List<String> AUTHORIZED_URL_ARRAY = List.of(
            "/logout/.*", "/join/.*"
    );

    @Override
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        boolean requiresAuthorization = AUTHORIZED_URL_ARRAY.stream().anyMatch(path::matches);
        if (!requiresAuthorization) {
            return chain.filter(exchange);
        }

        return jwtManager.checkAccessToken(exchange)
                .flatMap(internalId -> jwtManager.getAuthentication(internalId)
                        .flatMap(auth -> chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))))
                .onErrorResume(e -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Access token not found or invalid")));
    }
}
