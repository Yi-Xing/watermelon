package top.fblue.watermelon.auth.application.converter;

import org.junit.jupiter.api.Test;
import top.fblue.watermelon.api.response.CodeExchangeResponse;
import top.fblue.watermelon.auth.application.dto.CallbackResponse;
import top.fblue.watermelon.auth.domain.user.entity.AuthCodeInfo;
import top.fblue.watermelon.auth.domain.user.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SsoAuthorizationConverterTest {

    private final SsoAuthorizationConverter converter = new SsoAuthorizationConverter();

    @Test
    void shouldBuildCallbackAndCodeExchangeResponses() {
        CallbackResponse callback = converter.toCallbackResponse(
                "http://banana.fblue.top:5173/sso/callback", "code value", "state value");
        CodeExchangeResponse exchange = converter.toCodeExchangeResponse(
                User.builder().id(7L).username("visitor").build(),
                new AuthCodeInfo(7L, "sid", "banana",
                        "http://banana.fblue.top:5173/sso/callback", 12345L));

        assertTrue(callback.getCallbackUrl().contains("code=code%20value"));
        assertTrue(callback.getCallbackUrl().contains("state=state%20value"));
        assertEquals(7L, exchange.getUserId());
        assertEquals("visitor", exchange.getUsername());
        assertEquals("sid", exchange.getSid());
        assertEquals(12345L, exchange.getSessionExpireAt());
    }
}
