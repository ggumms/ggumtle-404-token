package life.ggumtle.token.log.controller;

import life.ggumtle.token.common.response.Response;
import life.ggumtle.token.log.dto.LoginResponseDto;
import life.ggumtle.token.log.service.KakaoService;
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
    public Mono<LoginResponseDto> kakaoLogin(@RequestParam("code") String code, ServerWebExchange exchange) {
        return kakaoService.kakaoLogin(code, exchange);
    }

}