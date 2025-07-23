package top.fblue.watermelon.application.converter;

import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;
import top.fblue.watermelon.domain.user.entity.User;
import top.fblue.watermelon.infrastructure.po.UserPO;

/**
 * 用户转换器接口
 */
public interface UserConverterInterface {
    
    /**
     * DTO转Domain
     */
    User toDomain(CreateUserDTO dto);
    
    /**
     * Domain转VO
     */
    UserVO toVO(User domain);

} 