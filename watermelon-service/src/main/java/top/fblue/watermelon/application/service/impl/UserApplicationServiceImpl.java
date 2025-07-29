package top.fblue.watermelon.application.service.impl;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.fblue.watermelon.application.converter.UserConverter;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UserQueryDTO;
import top.fblue.watermelon.application.service.UserApplicationService;
import top.fblue.watermelon.application.vo.PageVO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.common.utils.StringUtil;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.service.UserDomainService;

import java.util.ArrayList;
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
    private UserConverter userConverter;

    @Override
    @Transactional
    public UserVO createUser(CreateUserDTO createUserDTO) {
        // 设置默认值，避免数据库存储null
        String phone = StringUtil.getNonEmptyString(createUserDTO.getPhone());
        String email = StringUtil.getNonEmptyString(createUserDTO.getEmail());
        String password = StringUtil.getNonEmptyString(createUserDTO.getPassword());

        // 调用领域服务创建用户
        User user = userDomainService.createUser(
                createUserDTO.getName(),
                email,
                phone,
                password,
                createUserDTO.getState(),
                createUserDTO.getRemark()
        );

        // 创建完成后，获取包含详细信息的用户数据
        User userDetail = userDomainService.getUserById(user.getId());

        // 直接转换并返回
        return userConverter.toVO(userDetail);
    }

    @Override
    public UserVO getUserById(Long id) {
        // 获取基础用户数据
        User user = userDomainService.getUserById(id);

        // 组装详细信息
        List<Long> userIdList  = new ArrayList<>();
        userIdList.add(user.getCreatedBy());
        userIdList.add(user.getUpdatedBy());

        Map<Long, User> userIDMap = userDomainService.getUserMapByIds(userIdList);

        return userConverter.toVO(user,userIDMap);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        return userDomainService.deleteUser(id);
    }

    @Override
    public PageVO<UserVO> getUserList(UserQueryDTO queryDTO) {

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
        return new PageVO<>(
                userVOs,
                total,
                queryDTO.getPageNum(),
                queryDTO.getPageSize()
        );
    }
}
