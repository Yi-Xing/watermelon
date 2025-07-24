package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.application.vo.UserInfoVO;
import top.fblue.watermelon.common.enums.StateEnum;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.domain.user.entity.UserWithRelatedInfo;
import top.fblue.watermelon.domain.user.entity.UserBasicInfo;
import top.fblue.watermelon.common.utils.DateTimeUtil;

/**
 * 用户转换器
 * Application层的转换器，负责Domain对象到VO的转换
 */
@Component
public class UserConverter {
    
    /**
     * CreateUserDTO转换为User实体
     */
    public User toDomain(CreateUserDTO dto) {
        if (dto == null) return null;
        
        return User.builder()
                .username(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .password(dto.getPassword())
                .state(dto.getState())
                .remark(dto.getRemark())
                .build();
    }
    
    /**
     * User实体转换为UserVO（简单版本，不包含关联用户信息）
     */
    public UserVO toVO(User domain) {
        if (domain == null) return null;
        
        UserVO vo = new UserVO();
        vo.setId(domain.getId());
        vo.setName(domain.getUsername());
        vo.setEmail(domain.getEmail());
        vo.setPhone(domain.getPhone());
        vo.setState(domain.getState());
        vo.setStateDesc(StateEnum.fromCode(domain.getState()).getDesc());
        vo.setRemark(domain.getRemark());
        
        // 格式化时间
        vo.setCreatedTime(DateTimeUtil.formatDateTime(domain.getCreatedTime()));
        vo.setUpdatedTime(DateTimeUtil.formatDateTime(domain.getUpdatedTime()));
        
        return vo;
    }
    
    /**
     * UserWithRelatedInfo转换为UserVO（完整版本，包含关联用户信息）
     */
    public UserVO toVO(UserWithRelatedInfo domainWithInfo) {
        if (domainWithInfo == null || domainWithInfo.getUser() == null) {
            return null;
        }
        
        User user = domainWithInfo.getUser();
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setState(user.getState());
        vo.setStateDesc(StateEnum.fromCode(user.getState()).getDesc());
        vo.setRemark(user.getRemark());
        
        // 格式化时间
        vo.setCreatedTime(DateTimeUtil.formatDateTime(user.getCreatedTime()));
        vo.setUpdatedTime(DateTimeUtil.formatDateTime(user.getUpdatedTime()));
        
        // 设置关联用户信息
        vo.setCreatedBy(convertToUserInfoVO(domainWithInfo.getCreatedByUser()));
        vo.setUpdatedBy(convertToUserInfoVO(domainWithInfo.getUpdatedByUser()));
        
        return vo;
    }
    
    /**
     * 手动转换方法（兼容现有代码）
     * 
     * @param domain 用户实体
     * @param createdByInfo 创建人信息
     * @param updatedByInfo 更新人信息
     * @return UserVO
     */
    public UserVO toVO(User domain, UserInfoVO createdByInfo, UserInfoVO updatedByInfo) {
        if (domain == null) return null;
        
        UserVO vo = new UserVO();
        vo.setId(domain.getId());
        vo.setName(domain.getUsername());
        vo.setEmail(domain.getEmail());
        vo.setPhone(domain.getPhone());
        vo.setState(domain.getState());
        vo.setStateDesc(StateEnum.fromCode(domain.getState()).getDesc());
        vo.setRemark(domain.getRemark());
        
        // 格式化时间
        vo.setCreatedTime(DateTimeUtil.formatDateTime(domain.getCreatedTime()));
        vo.setUpdatedTime(DateTimeUtil.formatDateTime(domain.getUpdatedTime()));
        
        // 设置关联用户信息
        vo.setCreatedBy(createdByInfo);
        vo.setUpdatedBy(updatedByInfo);
        
        return vo;
    }
    
    /**
     * Domain层的UserBasicInfo转换为Application层的UserInfoVO
     */
    private UserInfoVO convertToUserInfoVO(UserBasicInfo basicInfo) {
        if (basicInfo == null) {
            return null;
        }
        
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(basicInfo.getId());
        userInfo.setName(basicInfo.getUsername());
        
        return userInfo;
    }
} 