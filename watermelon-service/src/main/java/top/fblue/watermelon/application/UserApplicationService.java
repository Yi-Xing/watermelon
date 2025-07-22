package top.fblue.watermelon.application;

import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.vo.UserVO;

import java.util.List;

/**
 * 用户应用服务接口
 * 编排领域服务，处理事务边界
 */
public interface UserApplicationService {
    
    /**
     * 创建用户
     */
    UserVO createUser(CreateUserDTO createUserDTO);
    
    /**
     * 根据ID获取用户
     */
    UserVO getUserById(Long id);
    
    /**
     * 获取所有用户
     */
    List<UserVO> getAllUsers();
    
    /**
     * 更新用户邮箱
     */
    UserVO updateUserEmail(Long userId, String newEmail);
    
    /**
     * 激活用户
     */
    UserVO activateUser(Long userId);
    
    /**
     * 停用用户
     */
    UserVO deactivateUser(Long userId);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long userId);
}
