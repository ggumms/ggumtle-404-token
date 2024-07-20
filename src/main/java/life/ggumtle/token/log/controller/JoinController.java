package life.ggumtle.token.log.controller;

import life.ggumtle.token.common.response.Response;
import life.ggumtle.token.common.response.ResponseFail;
import life.ggumtle.token.log.dto.JoinRequestDto;
import life.ggumtle.token.log.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/join")
    public Mono<Response> join(@RequestPart("data") JoinRequestDto joinRequestDto,
                               @RequestPart("profileImage") FilePart profileImage,
                               ServerWebExchange exchange) {
        return joinService.join(joinRequestDto, profileImage, exchange)
                .then(Mono.just(new Response()))
                .onErrorResume(e -> Mono.just(new ResponseFail("REGISTRATION_ERROR", e.getMessage())));
    }
}
