package life.ggumtle.token.log.service;

import com.fasterxml.jackson.databind.JsonNode;
import life.ggumtle.token.common.entity.Provider;
import life.ggumtle.token.common.entity.Users;
import life.ggumtle.token.common.exception.CustomException;
import life.ggumtle.token.common.exception.ExceptionType;
import life.ggumtle.token.common.jwt.JwtManager;
import life.ggumtle.token.common.repository.UserRepository;
import life.ggumtle.token.log.dto.LoginDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class KakaoService {

    private final WebClient webClient = WebClient.create();
    private final UserRepository usersRepository;
    private final JwtManager jwtManager;

    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String kakaoTokenUri;
    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String kakaoUserInfoUri;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectUri;

    public Mono<LoginDto> kakaoLogin(String authenticationCode, ServerWebExchange exchange) {
        return requestAccessToken(authenticationCode)
                .flatMap(this::fetchUsersInfo)
                .flatMap(userInfo -> {
                    JsonNode kakaoAccountNode = userInfo.path("kakao_account");
                    String email = kakaoAccountNode.path("email").asText();
                    String nickname = userInfo.path("properties").path("nickname").asText();

                    return usersRepository.findByProviderAndProviderId(Provider.KAKAO, email)
                            .flatMap(user -> jwtManager.createAccessToken(user.getInternalId(), exchange)
                                    .then(jwtManager.createRefreshToken(user.getInternalId(), exchange))
                                    .thenReturn(LoginDto.builder()
                                            .login(true)
                                            .hasAccount(true)
                                            .build()))
                            .switchIfEmpty(usersRepository.existsByNickname(nickname)
                                    .flatMap(exists -> {
                                        if (exists) {
                                            return Mono.just(LoginDto.builder()
                                                    .login(false)
                                                    .hasAccount(false)
                                                    .nickname(nickname)
                                                    .nicknameDuplicate(true)
                                                    .build());
                                        } else {
                                            return registerUsers(userInfo)
                                                    .flatMap(user -> jwtManager.createAccessToken(user.getInternalId(), exchange)
                                                            .then(jwtManager.createRefreshToken(user.getInternalId(), exchange))
                                                            .thenReturn(LoginDto.builder()
                                                                    .login(true)
                                                                    .hasAccount(false)
                                                                    .nickname(nickname)
                                                                    .nicknameDuplicate(false)
                                                                    .build()));
                                        }
                                    }));
                })
                .defaultIfEmpty(LoginDto.builder()
                        .login(false)
                        .hasAccount(false)
                        .build());
    }

    private Mono<JsonNode> requestAccessToken(String authenticationCode) {
        return webClient.post()
                .uri(kakaoTokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("code", authenticationCode)
                        .with("redirect_uri", redirectUri)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new CustomException(ExceptionType.CLIENT_ERROR)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new CustomException(ExceptionType.SERVER_ERROR)))
                .bodyToMono(JsonNode.class)
                .handle((response, sink) -> {
                    JsonNode accessToken = response.get("access_token");
                    if (accessToken == null) {
                        sink.error(new CustomException(ExceptionType.NOT_VALID_TOKEN));
                        return;
                    }
                    sink.next(accessToken);
                });
    }

    private Mono<JsonNode> fetchUsersInfo(JsonNode accessToken) {
        return webClient.get()
                .uri(kakaoUserInfoUri)
                .headers(headers -> headers.setBearerAuth(accessToken.asText()))
                .retrieve()
                .bodyToMono(JsonNode.class);
    }

    private Mono<Users> registerUsers(JsonNode UsersInfo) {
        Users newUsers = new Users();
        newUsers.setInternalId(String.valueOf(UUID.randomUUID()));
        newUsers.setProvider(Provider.KAKAO);

        JsonNode kakaoAccountNode = UsersInfo.path("kakao_account");
        String email = kakaoAccountNode.path("email").asText();
        newUsers.setProviderId(email);

        String nickname = UsersInfo.path("properties").path("nickname").asText();
        newUsers.setNickname(nickname);

        return usersRepository.save(newUsers);
    }
}
