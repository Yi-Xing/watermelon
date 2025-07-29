package top.fblue.watermelon.application.service;

import top.fblue.watermelon.application.dto.CreateUserDTO;
import top.fblue.watermelon.application.dto.UpdateUserDTO;
import top.fblue.watermelon.application.dto.ResetPasswordDTO;
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
     * 根据ID获取用户详情（包含关联角色）
     */
    UserVO getUserDetailById(Long id);
    
    /**
     * 根据ID获取用户基本信息
     */
    UserVO getUserById(Long id);
    
    /**
     * 更新用户
     */
    boolean updateUser(UpdateUserDTO updateUserDTO);
    
    /**
     * 重设密码
     */
    boolean resetPassword(ResetPasswordDTO resetPasswordDTO);
    
    /**
     * 删除用户
     */
    boolean deleteUser(Long id);
    
    /**
     * 分页查询用户列表
     */
    PageVO<UserVO> getUserList(UserQueryDTO queryDTO);
}
