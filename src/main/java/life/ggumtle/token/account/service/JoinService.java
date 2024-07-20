package life.ggumtle.token.account.service;

import life.ggumtle.token.common.S3.S3Service;
import life.ggumtle.token.common.entity.Survey;
import life.ggumtle.token.common.entity.Users;
import life.ggumtle.token.common.repository.SurveyRepository;
import life.ggumtle.token.common.repository.UserRepository;
import life.ggumtle.token.account.dto.JoinRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JoinService {

    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final S3Service s3Service;

    public Mono<Users> join(JoinRequestDto joinRequestDto, FilePart profileImage) {
        Users user = new Users();
        user.setProvider(joinRequestDto.getProvider());
        user.setNickname(joinRequestDto.getNickname());
        user.setProviderId(joinRequestDto.getCode());

        return s3Service.uploadFile(profileImage)
                .flatMap(profileImageUrl -> {
                    user.setProfileImage(profileImageUrl);

                    return userRepository.save(user)
                            .flatMap(savedUser -> {
                                Survey survey = createSurvey(savedUser.getId(), joinRequestDto.getSurveyResult());
                                return surveyRepository.save(survey)
                                        .thenReturn(savedUser);
                            });
                });
    }

    private Survey createSurvey(Long userId, List<String> surveyResult) {
        Survey survey = new Survey();
        survey.setUserId(userId);

        Map<String, Boolean> surveyFields = getStringBooleanMap(surveyResult);

        survey.setEnvironment(surveyFields.get("environment"));
        survey.setCharity(surveyFields.get("charity"));
        survey.setRelationships(surveyFields.get("relationships"));
        survey.setRelaxation(surveyFields.get("relaxation"));
        survey.setRomance(surveyFields.get("romance"));
        survey.setExercise(surveyFields.get("exercise"));
        survey.setTravel(surveyFields.get("travel"));
        survey.setLang(surveyFields.get("lang"));
        survey.setCulture(surveyFields.get("culture"));
        survey.setChallenge(surveyFields.get("challenge"));
        survey.setHobby(surveyFields.get("hobby"));
        survey.setWorkplace(surveyFields.get("workplace"));

        return survey;
    }

    private static Map<String, Boolean> getStringBooleanMap(List<String> surveyResult) {
        Map<String, Boolean> surveyFields = new HashMap<>();
        surveyFields.put("environment", false);
        surveyFields.put("charity", false);
        surveyFields.put("relationships", false);
        surveyFields.put("relaxation", false);
        surveyFields.put("romance", false);
        surveyFields.put("exercise", false);
        surveyFields.put("travel", false);
        surveyFields.put("lang", false);
        surveyFields.put("culture", false);
        surveyFields.put("challenge", false);
        surveyFields.put("hobby", false);
        surveyFields.put("workplace", false);

        for (String result : surveyResult) {
            surveyFields.put(result.toLowerCase(), true);
        }
        return surveyFields;
    }
}
