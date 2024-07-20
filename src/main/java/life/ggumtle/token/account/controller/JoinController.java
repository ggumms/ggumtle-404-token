package life.ggumtle.token.account.controller;

import life.ggumtle.token.common.entity.Users;
import life.ggumtle.token.account.dto.JoinRequestDto;
import life.ggumtle.token.account.service.JoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class JoinController {

    private final JoinService joinService;

    @PostMapping("/join")
    public Mono<ResponseEntity<Users>> join(
            @RequestPart("data") Mono<JoinRequestDto> joinRequestDtoMono,
            @RequestPart("profileImage") Mono<FilePart> profileImageMono) {

        return Mono.zip(joinRequestDtoMono, profileImageMono)
                .flatMap(tuple -> {
                    JoinRequestDto joinRequestDto = tuple.getT1();
                    FilePart profileImage = tuple.getT2();
                    return joinService.join(joinRequestDto, profileImage);
                })
                .map(user -> ResponseEntity.ok().body(user))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }
}
