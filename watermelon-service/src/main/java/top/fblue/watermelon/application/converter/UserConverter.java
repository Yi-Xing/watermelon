package top.fblue.watermelon.application.converter;

import org.springframework.stereotype.Component;
import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * 用户数据转换器
 */
@Component
public class UserConverter {

    /**
     * PO转Domain
     */
    public User toDomain(UserPO po) {
        if (po == null) return null;
        
        User user = new User();
        user.setId(po.getId());
        user.setUsername(po.getName());
        user.setEmail(po.getEmail());
        user.setPassword(po.getPassword());
        user.setPhone(po.getPhone());
        user.setState(po.getState());
        user.setRemark(po.getRemark());
        user.setActive(po.getState() == 1);
        return user;
    }

    /**
     * Domain转PO
     */
    public UserPO toPO(User domain) {
        if (domain == null) return null;
        
        UserPO po = new UserPO();
        po.setId(domain.getId());
        po.setName(domain.getUsername());
        po.setEmail(domain.getEmail());
        po.setPassword(domain.getPassword());
        po.setPhone(domain.getPhone());
        po.setState(domain.getState());
        po.setRemark(domain.getRemark());
        return po;
    }
    
    /**
     * Domain转VO
     */
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
    
    /**
     * DTO转Domain
     */
    public User toDomain(CreateUserDTO dto) {
        if (dto == null) return null;
        
        User user = new User(dto.getName(), dto.getEmail(), dto.getPassword());
        user.setPhone(dto.getPhone());
        user.setState(dto.getState());
        user.setRemark(dto.getRemark());
        return user;
    }
} 