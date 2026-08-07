package top.fblue.watermelon.application.converter;

import org.junit.jupiter.api.Test;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionSnapshot;
import top.fblue.watermelon.application.vo.CurrentUserVO;
import top.fblue.watermelon.common.dto.UserTokenDTO;
import top.fblue.watermelon.domain.user.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class UserConverterTest {

    private final UserConverter userConverter = new UserConverter();

    @Test
    void shouldOnlyExposePageAndButtonPermissionsWithoutSystemPrefix() {
        User user = User.builder().id(7L).username("visitor").build();
        PermissionSnapshot permissionSnapshot = new PermissionSnapshot(
                7L, "watermelon", List.of(
                new PermissionResource("watermelon:admin.users.page", PermissionResourceTypeEnum.PAGE),
                new PermissionResource("watermelon:admin.users.add.button", PermissionResourceTypeEnum.BUTTON),
                new PermissionResource("watermelon:GET:/api/admin/users", PermissionResourceTypeEnum.API)
        ), 1L, 1L);

        CurrentUserVO result = userConverter.toVO(
                user, mock(UserTokenDTO.class), permissionSnapshot);

        assertEquals(List.of("admin.users.page"), result.getPageCodeList());
        assertEquals(List.of("admin.users.add.button"), result.getButtonCodeList());
    }
}
