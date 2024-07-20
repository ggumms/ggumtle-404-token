package life.ggumtle.token.account.dto;

import life.ggumtle.token.common.entity.Provider;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class JoinRequestDto {
    private Provider provider;
    private String code;
    private String nickname;
    private MultipartFile profileImage;
    private List<String> surveyResult;
}
