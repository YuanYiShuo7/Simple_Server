package io.yys.server.service;

import io.yys.server.common.model.PageRequest;
import io.yys.server.service.model.UserDTO;
import io.yys.server.service.model.UserLoginDTO;
import io.yys.server.service.model.UserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface UserService {
    
    UserVO register(UserDTO userDTO);
    
    String login(UserLoginDTO loginDTO);
    
    void logout(String token);
    
    UserVO getUserInfo(Long userId);
    
    Page<UserVO> getUserPage(PageRequest pageRequest);
    
    UserVO updateUser(UserDTO userDTO);
    
    void deleteUser(Long userId);
}