package io.yys.server.web.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.yys.server.common.model.ApiResponse;
import io.yys.server.common.model.PageRequest;
import io.yys.server.service.UserService;
import io.yys.server.service.model.UserDTO;
import io.yys.server.service.model.UserLoginDTO;
import io.yys.server.service.model.UserVO;
import jakarta.validation.Valid;

/**
 * 用户管理控制器
 * 提供用户注册、登录、信息管理等完整用户操作接口
 * 
 * @author Your Name
 * @version 1.0
 * @since 2024
 */
@Tag(name = "用户管理", description = "用户注册、登录、信息查询、修改、删除等完整用户生命周期管理接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    /**
     * 用户注册接口
     * 用于新用户注册账号，系统会验证用户名唯一性并创建用户记录
     */
    @Operation(
        summary = "用户注册", 
        description = "新用户注册接口，提交用户名、密码等基本信息完成账号注册。系统会自动校验用户名唯一性。"
    )
    @PostMapping("/register")
    public ApiResponse<UserVO> register(
        @Valid @RequestBody UserDTO userDTO) {
        return ApiResponse.success(userService.register(userDTO));
    }
    
    /**
     * 用户登录接口
     * 验证用户身份并返回访问令牌
     */
    @Operation(
        summary = "用户登录", 
        description = "用户登录接口，通过用户名和密码验证用户身份，登录成功返回JWT令牌用于后续接口认证。"
    )
    @PostMapping("/login")
    public ApiResponse<String> login(
        @Valid @RequestBody UserLoginDTO loginDTO) {
        return ApiResponse.success(userService.login(loginDTO));
    }
    
    /**
     * 用户登出接口
     * 使当前用户的令牌失效
     */
    @Operation(
        summary = "用户登出", 
        description = "用户登出接口，使当前用户的JWT令牌失效，需要携带有效的Authorization头信息。"
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
        @RequestHeader("Authorization") String token) {
        userService.logout(token);
        return ApiResponse.success(null);
    }
    
    /**
     * 获取用户详细信息
     * 根据用户ID查询用户完整信息
     */
    @Operation(
        summary = "获取用户信息", 
        description = "根据用户ID查询用户详细信息，包括基本信息、个人资料等。"
    )
    @GetMapping("/{userId}")
    public ApiResponse<UserVO> getUserInfo(
        @PathVariable Long userId) {
        return ApiResponse.success(userService.getUserInfo(userId));
    }
    
    /**
     * 分页查询用户列表
     * 支持按条件分页查询用户信息，用于管理后台用户列表展示
     */
    @Operation(
        summary = "分页查询用户列表", 
        description = "分页查询用户列表接口，支持按页码、每页大小等参数进行分页查询，返回分页后的用户数据。"
    )
    @GetMapping("/page")
    public ApiResponse<Page<UserVO>> getUserPage(
        @Valid PageRequest pageRequest) {
        return ApiResponse.success(userService.getUserPage(pageRequest));
    }
    
    /**
     * 更新用户信息
     * 修改用户基本信息，如昵称、邮箱、手机号等
     */
    @Operation(
        summary = "更新用户信息", 
        description = "更新用户基本信息接口，可以修改用户昵称、邮箱、手机号等个人信息。需要用户ID标识要更新的用户。"
    )
    @PutMapping
    public ApiResponse<UserVO> updateUser(
        @Valid @RequestBody UserDTO userDTO) {
        return ApiResponse.success(userService.updateUser(userDTO));
    }
    
    /**
     * 删除用户
     * 根据用户ID删除用户记录，支持逻辑删除或物理删除
     */
    @Operation(
        summary = "删除用户", 
        description = "删除用户接口，根据用户ID删除指定的用户记录。此操作可能需要管理员权限。"
    )
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(
        @PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.success(null);
    }
}