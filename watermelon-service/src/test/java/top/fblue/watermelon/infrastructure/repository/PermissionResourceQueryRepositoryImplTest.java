package top.fblue.watermelon.infrastructure.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResource;
import top.fblue.watermelon.auth.domain.permission.entity.PermissionResourceTypeEnum;
import top.fblue.watermelon.common.enums.ResourceTypeEnum;
import top.fblue.watermelon.infrastructure.converter.PermissionResourceConverter;
import top.fblue.watermelon.infrastructure.mapper.ResourceNodeMapper;
import top.fblue.watermelon.infrastructure.po.ResourceNodePO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 运行时鉴权资源查询仓储测试。
 */
@ExtendWith(MockitoExtension.class)
class PermissionResourceQueryRepositoryImplTest {

    @Mock
    private ResourceNodeMapper resourceNodeMapper;

    private PermissionResourceQueryRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new PermissionResourceQueryRepositoryImpl(
                resourceNodeMapper, new PermissionResourceConverter());
    }

    @Test
    void shouldQueryEffectiveResourcesDirectlyFromMapper() {
        List<ResourceNodePO> resourceNodePOs = List.of(
                ResourceNodePO.builder()
                        .code("banana:home.page")
                        .type(ResourceTypeEnum.PAGE.getCode())
                        .build()
        );
        when(resourceNodeMapper.selectEffectiveResources(7L, "banana")).thenReturn(resourceNodePOs);

        List<PermissionResource> result = repository.findEffectiveResources(7L, "banana");

        assertEquals(List.of(
                new PermissionResource("banana:home.page", PermissionResourceTypeEnum.PAGE)
        ), result);
        verify(resourceNodeMapper).selectEffectiveResources(7L, "banana");
    }
}
