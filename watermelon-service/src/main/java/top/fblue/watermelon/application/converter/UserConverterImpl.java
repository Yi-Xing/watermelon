package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.domain.user.entity.User;

/**
 * 用户转换器实现
 */
@Component
public class UserConverterImpl implements UserConverterInterface {
    
    @Override
    public User toDomain(CreateUserDTO dto) {
        if (dto == null) return null;
        
        User user = new User(dto.getName(), dto.getEmail(), dto.getPassword());
        user.setPhone(dto.getPhone());
        user.setState(dto.getState());
        user.setRemark(dto.getRemark());
        return user;
    }
    
    @Override
    public UserVO toVO(User domain) {
        if (domain == null) return null;
        
        UserVO vo = new UserVO();
        vo.setId(domain.getId());
        vo.setName(domain.getUsername());
        vo.setEmail(domain.getEmail());
        vo.setPhone(domain.getPhone());
        vo.setState(domain.getState());
        vo.setStateDesc(domain.isActive() ? "启用" : "禁用");
        vo.setRemark(domain.getRemark());
        return vo;
    }
} 