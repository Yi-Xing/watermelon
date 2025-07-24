package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.UserConverter;
import top.fblue.watermelon.application.service.UserApplicationService;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.domain.user.service.UserDomainService;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.UserWithRelatedInfo;
import top.fblue.watermelon.common.utils.StringUtil;

import java.util.List;
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
    private UserConverter userConverter;
    
    @Override
    @Transactional
    public UserVO createUser(CreateUserDTO createUserDTO) {
        // 设置默认值，避免数据库存储null
        String phone = StringUtil.getNonEmptyString(createUserDTO.getPhone());
        String email = StringUtil.getNonEmptyString(createUserDTO.getEmail());
        String password = StringUtil.getNonEmptyString(createUserDTO.getPassword());
        String remark = StringUtil.getNonEmptyString(createUserDTO.getRemark());
        
        // 调用领域服务创建用户
        User user = userDomainService.createUser(
                createUserDTO.getName(),
                email,
                phone,
                password,
                createUserDTO.getState(),
                remark
        );
        
        // 创建完成后，获取包含关联信息的完整用户数据
        UserWithRelatedInfo userWithInfo = userDomainService.getUserWithRelatedInfoById(user.getId());
        
        // 直接转换并返回
        return userConverter.toVO(userWithInfo);
    }
    
    @Override
    public UserVO getUserById(Long id) {
        // 直接获取包含关联信息的用户数据
        UserWithRelatedInfo userWithInfo = userDomainService.getUserWithRelatedInfoById(id);
        
        return userConverter.toVO(userWithInfo);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserVO getUserByPhone(String phone) {
        // 直接获取包含关联信息的用户数据
        UserWithRelatedInfo userWithInfo = userDomainService.getUserWithRelatedInfoByPhone(phone);
        
        // 简单转换并返回
        return userConverter.toVO(userWithInfo);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserVO> getAllUsers() {
        // 直接获取包含关联信息的用户列表
        List<UserWithRelatedInfo> usersWithInfo = userDomainService.getAllUsersWithRelatedInfo();
        
        // 批量转换并返回
        return usersWithInfo.stream()
                .map(userConverter::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        return userDomainService.deleteUser(userId);
    }
}
