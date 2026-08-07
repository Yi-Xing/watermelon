package top.fblue.watermelon.auth.application.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.watermelon.api.request.CodeExchangeRequest;
import top.fblue.watermelon.api.request.LogoutRpcRequest;
import top.fblue.watermelon.auth.application.dto.CallbackRequest;
import top.fblue.watermelon.auth.domain.user.entity.SsoSessionInfo;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SsoAuthorizationValidatorTest {

    private static final String CALLBACK_URI = "http://banana.fblue.top:5173/sso/callback";

    private SsoAuthorizationValidator validator;

    @BeforeEach
    void setUp() {
        AuthProperties.Client client = new AuthProperties.Client();
        client.setEnabled(true);
        client.setRedirectUris(List.of(CALLBACK_URI));
        client.setDubboUrl("dubbo://banana.fblue.top:20881");

        AuthProperties properties = new AuthProperties();
        properties.setAllowInsecureRedirects(true);
        properties.setClients(Map.of("banana", client));
        validator = new SsoAuthorizationValidator(properties);
    }

    @Test
    void shouldValidateAuthorizationRequestAndCanonicalizeRedirectUri() {
        CallbackRequest request = callbackRequest("banana", CALLBACK_URI, "state");
        SsoPrincipal principal = SsoPrincipal.builder().userId(7L).sid("sid").build();

        assertEquals(CALLBACK_URI, validator.validateAuthorizationRequest(principal, request));
    }

    @Test
    void shouldRejectUnregisteredRedirectUriAndIncompleteCodeExchange() {
        CallbackRequest request = callbackRequest("banana", "http://evil.example/callback", "state");
        SsoPrincipal principal = SsoPrincipal.builder().userId(7L).sid("sid").build();

        assertThrows(SsoAuthException.class,
                () -> validator.validateAuthorizationRequest(principal, request));
        assertThrows(SsoAuthException.class,
                () -> validator.validateCodeExchangeRequest(CodeExchangeRequest.builder()
                        .clientId("banana")
                        .redirectUri(CALLBACK_URI)
                        .build()));
    }

    @Test
    void shouldValidateSessionOwnerAndLogoutClientBinding() {
        SsoPrincipal principal = SsoPrincipal.builder().userId(7L).sid("sid").build();
        SsoSessionInfo session = SsoSessionInfo.builder().userId(7L).sid("sid").build();
        LogoutRpcRequest request = LogoutRpcRequest.builder()
                .sid("sid")
                .clientId("banana")
                .build();

        validator.validateSessionOwner(principal, session);
        validator.validateLogoutRequest(request);
        validator.validateLogoutClientBinding(request, Set.of("banana"));

        session.setUserId(8L);
        assertThrows(SsoAuthException.class,
                () -> validator.validateSessionOwner(principal, session));
        assertThrows(SsoAuthException.class,
                () -> validator.validateLogoutClientBinding(request, Set.of()));
    }

    private CallbackRequest callbackRequest(String clientId, String redirectUri, String state) {
        CallbackRequest request = new CallbackRequest();
        request.setClientId(clientId);
        request.setRedirectUri(redirectUri);
        request.setState(state);
        return request;
    }
}
