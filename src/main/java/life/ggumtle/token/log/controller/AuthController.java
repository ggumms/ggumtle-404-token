package life.ggumtle.token.log.controller;

import life.ggumtle.token.common.response.Response;
import life.ggumtle.token.log.service.KakaoService;
import life.ggumtle.token.common.jwt.JwtManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoService kakaoService;

    @GetMapping("/kakao")
    public Mono<Response> kakaoLogin(@RequestParam String code, ServerWebExchange exchange) {
        return kakaoService.kakaoLogin(code, exchange)
                .map(result -> new Response("loginResponse", result));
    }

}