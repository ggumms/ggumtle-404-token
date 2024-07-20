package life.ggumtle.token.common.response;

import java.io.Serial;

public class ResponseFail extends Response {

    @Serial
    private static final long serialVersionUID = 1L;

    public ResponseFail(String code, String message) {
        setData(Response.RESULT, Response.FAIL);
        setData(Response.CODE, code);
        setData(Response.MESSAGE, message);
    }
}
