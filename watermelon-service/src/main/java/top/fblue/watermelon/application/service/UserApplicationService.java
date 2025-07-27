package top.fblue.watermelon.application.service;

import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UserQueryDTO;
import top.fblue.watermelon.application.vo.PageVO;
import top.fblue.watermelon.application.vo.UserVO;

/**
 * 用户应用服务接口
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
     * 删除用户
     */
    boolean deleteUser(Long id);
    
    /**
     * 分页查询用户列表
     */
    PageVO<UserVO> getUserList(UserQueryDTO queryDTO);
}
