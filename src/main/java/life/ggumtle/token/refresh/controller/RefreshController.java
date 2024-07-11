package life.ggumtle.token.refresh.controller;

import life.ggumtle.token.common.response.Response;
import life.ggumtle.token.refresh.service.RefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/refresh")
@RequiredArgsConstructor
public class RefreshController {

    private final RefreshService refreshService;

    @GetMapping
    public Mono<Response> refresh(ServerWebExchange exchange) {
        return refreshService.refresh(exchange)
                .then(Mono.just(new Response("refreshToken", "Token refreshed successfully")))
                .onErrorResume(e -> Mono.just(new Response("refreshToken", "Invalid token")).flatMap(response -> Mono.error(e)));
    }
}
