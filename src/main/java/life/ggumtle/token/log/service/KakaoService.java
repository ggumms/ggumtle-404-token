package life.ggumtle.token.log.service;

import com.fasterxml.jackson.databind.JsonNode;
import life.ggumtle.token.common.entity.Provider;
import life.ggumtle.token.common.entity.Users;
import life.ggumtle.token.common.exception.CustomException;
import life.ggumtle.token.common.exception.ExceptionType;
import life.ggumtle.token.common.jwt.JwtManager;
import life.ggumtle.token.common.repository.UsersRepository;
import life.ggumtle.token.common.response.Response;
import life.ggumtle.token.common.response.ResponseFail;
import life.ggumtle.token.log.dto.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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
    private final UsersRepository usersRepository;
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

    public Mono<Response> kakaoLogin(String authenticationCode, ServerWebExchange exchange) {
        return requestAccessToken(authenticationCode)
                .flatMap(this::fetchUsersInfo)
                    .flatMap(kakaoUserInfo -> {
                        String providerId = kakaoUserInfo.get("sub").asText();
                        return usersRepository.findByProviderAndProviderId(Provider.KAKAO, providerId)
                                .flatMap(user -> handleExistingUser(user, exchange))
                                .switchIfEmpty(handleNewUser(kakaoUserInfo, exchange));
                    })
                .onErrorResume(e -> Mono.just(new ResponseFail("AUTH_ERROR", e.getMessage())));
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

    private Mono<Response> handleExistingUser(Users user, ServerWebExchange exchange) {
        if (user.getHasAccount()) {
            return jwtManager.createAccessToken(user.getInternalId(), exchange)
                    .then(jwtManager.createRefreshToken(user.getInternalId(), exchange))
                    .then(Mono.just(new Response("loginResponse", LoginResponseDto.builder()
                            .hasAccount(true)
                            .build())));
        } else {
            return jwtManager.createAccessToken(user.getInternalId(), exchange)
                    .then(Mono.just(new Response("loginResponse", LoginResponseDto.builder()
                            .hasAccount(false)
                            .build())));
        }
    }

    private Mono<Response> handleNewUser(JsonNode kakaoUserInfo, ServerWebExchange exchange) {
        String providerId = kakaoUserInfo.get("sub").asText();
        String nickname = kakaoUserInfo.path("properties").path("nickname").asText();

        Users newUser = new Users();
        newUser.setProvider(Provider.KAKAO);
        newUser.setProviderId(providerId);
        newUser.setInternalId(UUID.randomUUID().toString());

        return usersRepository.save(newUser)
                .then(jwtManager.createAccessToken(newUser.getInternalId(), exchange))
                .then(usersRepository.existsByNickname(nickname))
                    .flatMap(isDuplicate ->
                        Mono.just(new Response("loginResponse", LoginResponseDto.builder()
                            .hasAccount(false)
                            .nickname(nickname)
                            .nicknameDuplicate(isDuplicate)
                            .build())));
    }

}
