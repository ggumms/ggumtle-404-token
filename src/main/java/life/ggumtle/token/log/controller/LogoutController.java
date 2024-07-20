package life.ggumtle.token.log.controller;

import life.ggumtle.token.common.jwt.JwtManager;
import life.ggumtle.token.common.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class LogoutController {

    private final JwtManager jwtManager;

    @GetMapping("/logout")
    public Mono<Response> logout(ServerWebExchange exchange){
        return jwtManager.logout(exchange)
                .map(result -> new Response("logout", result));
    }
}
