package newdt.dsc.ws.tio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import org.tio.http.common.HttpRequest;

/**
 * Websocket 连接的参数。
 */
@Getter
@Setter
public class WsParams {
    /**
     * 客户端 ID。
     */
    private String clientId;

    /**
     * 客户端登录的 token。
     */
    private String token;

    /**
     * 用户 ID。
     */
    private int userId;

    /**
     * 是否有效。
     */
    private boolean validated;

    /**
     * 解析 Websocket 连接的参数
     *
     * @param request Websocket 请求
     * @return 返回参数对象。
     */
    public static WsParams parseParams(HttpRequest request) {
        WsParams params = new WsParams();
        params.setClientId(StringUtils.trimWhitespace(request.getParam("clientId")));
        params.setToken(StringUtils.trimWhitespace(request.getParam("token")));

        // TODO: 验证 token 有效性并且提取出用户 ID。
        params.setUserId(1);

        params.setValidated(true);

        return params;
    }
}
