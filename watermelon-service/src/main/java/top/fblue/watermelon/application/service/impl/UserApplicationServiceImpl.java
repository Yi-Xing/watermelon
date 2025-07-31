package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.UserConverter;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UpdateUserDTO;
import top.fblue.watermelon.application.dto.ResetPasswordDTO;
import top.fblue.watermelon.application.dto.UserQueryDTO;
import top.fblue.watermelon.application.service.UserApplicationService;
import top.fblue.watermelon.common.response.Page;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.domain.role.service.RoleDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.role.entity.Role;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户应用服务实现
 * 负责业务流程编排，事务管理
 * 简化后的版本：复杂的数据组装逻辑已移至Domain层
 */
@Service
public class UserApplicationServiceImpl implements UserApplicationService {

    @Resource
    private UserDomainService userDomainService;
    @Resource
    private RoleDomainService roleDomainService;
    @Resource
    private UserConverter userConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(CreateUserDTO createUserDTO) {
        // 校验角色是否存在
        roleDomainService.validateRoleIds(createUserDTO.getRoleIds());

        // 转换DTO为Domain实体
        User user = userConverter.toUser(createUserDTO);

        // 调用领域服务创建用户
        User createUser = userDomainService.createUser(user);

        // 创建用户和角色的关联关系
        userDomainService.createUserRole(createUser.getId(), createUserDTO.getRoleIds());

        // 直接转换并返回
        return userConverter.toVO(createUser);
    }

    @Override
    public UserVO getUserDetailById(Long id) {
        // 1. 获取基础用户数据
        User user = userDomainService.getUserById(id);

        // 2. 获取关联的用户信息
        List<Long> userIds = List.of(user.getCreatedBy(), user.getUpdatedBy());
        Map<Long, User> userMap = userDomainService.getUserMapByIds(userIds);

        // 3. 获取用户关联的角色信息
        List<Long> roleIds = userDomainService.getUserRoles(id);
        List<Role> roles = roleDomainService.getRoleByIds(roleIds);

        // 4. 组装详细信息（包含关联角色）
        return userConverter.toVO(user, userMap, roles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UpdateUserDTO updateUserDTO) {
        // 1. 校验角色是否存在
        roleDomainService.validateRoleIds(updateUserDTO.getRoleIds());

        // 2. 转换DTO为Domain实体
        User user = userConverter.toUser(updateUserDTO);

        // 3. 更新用户基本信息
        boolean userUpdated = userDomainService.updateUser(user);

        // 4. 更新用户角色关联关系
        userDomainService.updateUserRole(updateUserDTO.getId(), updateUserDTO.getRoleIds());

        return userUpdated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 调用领域服务重设密码
        return userDomainService.resetPassword(resetPasswordDTO.getId(), resetPasswordDTO.getPassword());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        return userDomainService.deleteUser(id);
    }

    @Override
    public Page<UserVO> getUserList(UserQueryDTO queryDTO) {

        // 查询用户列表
        List<User> users = userDomainService.getUserList(
                queryDTO.getKeyword(),
                queryDTO.getState(),
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );

        // 统计总数
        Long total = userDomainService.countUsers(
                queryDTO.getKeyword(),
                queryDTO.getState()
        );

        // 转换为VO
        List<UserVO> userVOs = users.stream()
                .map(userConverter::toVO)
                .collect(Collectors.toList());

        // 构建分页响应
        return new Page<>(
                userVOs,
                total,
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
    }


}
