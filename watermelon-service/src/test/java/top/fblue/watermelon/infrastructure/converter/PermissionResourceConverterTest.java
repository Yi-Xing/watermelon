package top.fblue.watermelon.infrastructure.converter;

import org.junit.jupiter.api.Test;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PermissionResourceConverterTest {

    private final PermissionResourceConverter converter = new PermissionResourceConverter();

    @Test
    void shouldConvertRuntimePermissionResourcesAndIgnoreDirectory() {
        List<ResourceNodePO> resources = List.of(
                resource("banana:home.page", ResourceTypeEnum.PAGE),
                resource("banana:oss.add.button", ResourceTypeEnum.BUTTON),
                resource("banana:POST:/api/oss", ResourceTypeEnum.API),
                resource("banana:oss.directory", ResourceTypeEnum.DIRECTORY)
        );

        List<PermissionResource> result = converter.toPermissionResources(resources);

        assertEquals(List.of(
                new PermissionResource("banana:home.page", PermissionResourceTypeEnum.PAGE),
                new PermissionResource("banana:oss.add.button", PermissionResourceTypeEnum.BUTTON),
                new PermissionResource("banana:POST:/api/oss", PermissionResourceTypeEnum.API)
        ), result);
    }

    private ResourceNodePO resource(String code, ResourceTypeEnum type) {
        return ResourceNodePO.builder().code(code).type(type.getCode()).build();
    }
}
