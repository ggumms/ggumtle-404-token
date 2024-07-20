package life.ggumtle.token.log.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class JoinRequestDto {
    private String nickname;
    private List<String> surveyResult;
}
