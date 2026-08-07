package top.fblue.watermelon.auth.infrastructure.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.watermelon.auth.application.service.PermissionApplicationService;
import top.fblue.watermelon.auth.infrastructure.config.AuthProperties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PermissionAuthInterceptorTest {

    @Test
    void shouldReturnUnauthorizedWhenAuthenticationPrincipalIsMissing() {
        AuthProperties authProperties = new AuthProperties();
        PermissionApplicationService permissionService = mock(PermissionApplicationService.class);
        PermissionAuthInterceptor interceptor = new PermissionAuthInterceptor(authProperties, permissionService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(permissionService, never()).hasApiPermission(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldDelegateApiPermissionDecisionToAuthApplicationService() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setServerClientId("watermelon");
        PermissionApplicationService permissionService = mock(PermissionApplicationService.class);
        PermissionAuthInterceptor interceptor = new PermissionAuthInterceptor(authProperties, permissionService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        SsoPrincipal principal = mock(SsoPrincipal.class);
        when(principal.getUserId()).thenReturn(7L);
        when(request.getAttribute(SsoConstants.CURRENT_USER_ATTRIBUTE)).thenReturn(principal);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/admin/users");
        when(permissionService.hasApiPermission(
                7L, "watermelon", "watermelon:GET:/api/admin/users")).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(permissionService).hasApiPermission(
                7L, "watermelon", "watermelon:GET:/api/admin/users");
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void shouldReturnForbiddenWhenApiPermissionIsMissing() {
        AuthProperties authProperties = new AuthProperties();
        authProperties.setServerClientId("watermelon");
        PermissionApplicationService permissionService = mock(PermissionApplicationService.class);
        PermissionAuthInterceptor interceptor = new PermissionAuthInterceptor(authProperties, permissionService);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        SsoPrincipal principal = mock(SsoPrincipal.class);
        when(principal.getUserId()).thenReturn(7L);
        when(request.getAttribute(SsoConstants.CURRENT_USER_ATTRIBUTE)).thenReturn(principal);
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/admin/users/9");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
