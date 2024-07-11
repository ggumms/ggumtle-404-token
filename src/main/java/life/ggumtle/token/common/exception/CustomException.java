package life.ggumtle.token.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException{

    private final ExceptionType exceptionType;

    @Override
    public String getMessage(){
        return exceptionType.getMsg();
    }
}
