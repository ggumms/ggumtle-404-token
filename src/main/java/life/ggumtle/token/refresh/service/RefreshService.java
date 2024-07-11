package life.ggumtle.token.refresh.service;

import life.ggumtle.token.common.jwt.JwtManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RefreshService {

    private final JwtManager jwtManager;

    public Mono<Void> refresh(ServerWebExchange exchange) {
        return jwtManager.checkRefreshToken(exchange)
                .then();
    }
}
