package life.ggumtle.token.log.service;

import life.ggumtle.token.common.S3.S3Service;
import life.ggumtle.token.common.entity.Survey;
import life.ggumtle.token.common.entity.Users;
import life.ggumtle.token.common.jwt.JwtManager;
import life.ggumtle.token.common.repository.SurveyRepository;
import life.ggumtle.token.common.repository.UsersRepository;
import life.ggumtle.token.common.response.Response;
import life.ggumtle.token.common.response.ResponseFail;
import life.ggumtle.token.log.dto.JoinRequestDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final UsersRepository usersRepository;
    private final SurveyRepository surveyRepository;
    private final JwtManager jwtManager;
    private final S3Service s3Service;

    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();
    static {
        CATEGORY_MAP.put("환경", "environment");
        CATEGORY_MAP.put("자선활동", "charity");
        CATEGORY_MAP.put("인간관계", "relationships");
        CATEGORY_MAP.put("휴식", "relaxation");
        CATEGORY_MAP.put("연애", "romance");
        CATEGORY_MAP.put("운동", "exercise");
        CATEGORY_MAP.put("여행", "travel");
        CATEGORY_MAP.put("언어", "lang");
        CATEGORY_MAP.put("문화", "culture");
        CATEGORY_MAP.put("도전", "challenge");
        CATEGORY_MAP.put("취미", "hobby");
        CATEGORY_MAP.put("직장", "workplace");
    }

    public Mono<Response> join(JoinRequestDto joinRequestDto, FilePart profileImage, ServerWebExchange exchange) {
        return jwtManager.checkAccessToken(exchange)
                .flatMap(internalId -> usersRepository.findByInternalId(internalId)
                        .flatMap(user -> checkNicknameAndSave(user, joinRequestDto, profileImage, exchange)))
                .then(Mono.just(new Response()))
                .onErrorResume(e -> Mono.just(new ResponseFail("AUTH_ERROR", e.getMessage())));
    }

    private Mono<Void> checkNicknameAndSave(Users user, JoinRequestDto joinRequestDto, FilePart profileImage, ServerWebExchange exchange) {
        return usersRepository.existsByNickname(joinRequestDto.getNickname())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Nickname already exists"));
                    } else {
                        return updateUserInfo(user, joinRequestDto, profileImage, exchange);
                    }
                });
    }

    private Mono<Void> updateUserInfo(Users user, JoinRequestDto joinRequestDto, FilePart profileImage, ServerWebExchange exchange) {
        user.setNickname(joinRequestDto.getNickname());
        return usersRepository.save(user)
                .then(s3Service.uploadFile(profileImage)
                        .flatMap(profileImageUrl -> {
                            user.setProfileImage(profileImageUrl);
                            user.setHasAccount(true);
                            return usersRepository.save(user);
                        }))
                .then(saveSurveyResult(user.getId(), joinRequestDto.getSurveyResult()))
                .then(jwtManager.createAccessToken(user.getInternalId(), exchange))
                .then(jwtManager.createRefreshToken(user.getInternalId(), exchange));
    }

    private Mono<Void> saveSurveyResult(Long userId, List<String> surveyResult) {
        Survey survey = new Survey();
        survey.setUserId(userId);
        for (String category : surveyResult) {
            String fieldName = CATEGORY_MAP.get(category);
            if (fieldName != null) {
                try {
                    Survey.class.getDeclaredField(fieldName).setBoolean(survey, true);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    String errorMessage = String.format("Failed to set survey field for category: %s", category);
                    Logger logger = LoggerFactory.getLogger(JoinService.class);
                    logger.error(errorMessage, e);
                    throw new RuntimeException(errorMessage, e);
                }
            }
        }
        return surveyRepository.save(survey).then();
    }
}
