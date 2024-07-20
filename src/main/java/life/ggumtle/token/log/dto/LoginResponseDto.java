package life.ggumtle.token.log.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDto {
    private Boolean hasAccount;
    private String nickname;
    private Boolean nicknameDuplicate;
}
