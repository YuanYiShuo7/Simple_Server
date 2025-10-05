package io.yys.server.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.yys.server.common.constant.RedisConstants;
import io.yys.server.common.model.PageRequest;
import io.yys.server.dao.entity.UserDO;
import io.yys.server.dao.mapper.UserMapper;
import io.yys.server.service.UserService;
import io.yys.server.service.exception.BusinessException;
import io.yys.server.service.model.UserDTO;
import io.yys.server.service.model.UserLoginDTO;
import io.yys.server.service.model.UserVO;
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public UserServiceImpl(UserMapper userMapper, RedisTemplate<String, Object> redisTemplate) {
        this.userMapper = userMapper;
        this.redisTemplate = redisTemplate;
        logger.debug("UserServiceImpl 初始化完成，依赖注入成功");
    }

    @Override
    public UserVO register(UserDTO userDTO) {
        logger.debug("开始用户注册流程，用户名: {}", userDTO.getUsername());
        
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, userDTO.getUsername()));
        logger.debug("用户名 {} 在数据库中存在的数量: {}", userDTO.getUsername(), count);
        
        if (count > 0) {
            logger.warn("用户名 {} 已存在，注册失败", userDTO.getUsername());
            throw new BusinessException("用户名已存在");
        }
        
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userDTO, userDO);
        
        // 密码加密
        userDO.setPassword(DigestUtils.md5DigestAsHex(
            userDTO.getPassword().getBytes(StandardCharsets.UTF_8)));
        userDO.setStatus(1);
        
        logger.debug("准备插入用户数据到数据库: {}", userDO);
        userMapper.insert(userDO);
        logger.debug("用户数据插入成功，用户ID: {}", userDO.getId());
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        
        logger.info("用户注册成功，用户名: {}, 用户ID: {}", userDTO.getUsername(), userDO.getId());
        return userVO;
    }
    
    @Override
    public String login(UserLoginDTO loginDTO) {
        logger.debug("开始用户登录流程，用户名: {}", loginDTO.getUsername());
        
        UserDO userDO = userMapper.selectOne(new LambdaQueryWrapper<UserDO>()
                .eq(UserDO::getUsername, loginDTO.getUsername())
                .eq(UserDO::getStatus, 1));
        
        if (userDO == null) {
            logger.warn("用户登录失败，用户名不存在或已被禁用: {}", loginDTO.getUsername());
            throw new BusinessException("用户不存在或已被禁用");
        }
        
        logger.debug("找到用户信息: {}", userDO);
        
        String encryptedPassword = DigestUtils.md5DigestAsHex(
            loginDTO.getPassword().getBytes(StandardCharsets.UTF_8));
        
        logger.debug("密码验证 - 输入密码加密后: {}, 数据库存储密码: {}", 
                    encryptedPassword, userDO.getPassword());
        
        if (!encryptedPassword.equals(userDO.getPassword())) {
            logger.warn("用户 {} 密码验证失败", loginDTO.getUsername());
            throw new BusinessException("密码错误");
        }
        
        // 生成token
        String token = UUID.randomUUID().toString().replace("-", "");
        logger.debug("为用户 {} 生成token: {}", loginDTO.getUsername(), token);
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        
        // 存储到Redis
        String redisKey = RedisConstants.USER_TOKEN_KEY + token;
        logger.debug("准备将用户信息存储到Redis，Key: {}", redisKey);
        
        redisTemplate.opsForValue().set(
            redisKey,
            userVO,
            RedisConstants.USER_TOKEN_TTL,
            TimeUnit.MINUTES
        );
        
        logger.info("用户登录成功，用户名: {}, token: {}", loginDTO.getUsername(), token);
        return token;
    }
    
    @Override
    public void logout(String token) {
        logger.debug("开始用户登出流程，token: {}", token);
        
        String redisKey = RedisConstants.USER_TOKEN_KEY + token;
        redisTemplate.delete(redisKey);
        
        logger.info("用户登出成功，token: {}", token);
    }
    
    @Override
    public UserVO getUserInfo(Long userId) {
        logger.debug("开始查询用户信息，用户ID: {}", userId);
        
        UserDO userDO = userMapper.selectById(userId);
        if (userDO == null) {
            logger.warn("查询用户信息失败，用户ID不存在: {}", userId);
            throw new BusinessException("用户不存在");
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDO, userVO);
        
        logger.debug("用户信息查询成功，用户ID: {}, 用户名: {}", userId, userVO.getUsername());
        return userVO;
    }
    
    @Override
    public Page<UserVO> getUserPage(PageRequest pageRequest) {
        logger.debug("开始分页查询用户列表，页码: {}, 页大小: {}", 
                    pageRequest.getPageNum(), pageRequest.getPageSize());
        
        Page<UserDO> page = new Page<>(pageRequest.getPageNum(), pageRequest.getPageSize());
        Page<UserDO> result = userMapper.selectPage(page, 
            new LambdaQueryWrapper<UserDO>().eq(UserDO::getStatus, 1));
        
        logger.debug("分页查询完成，总记录数: {}, 当前页记录数: {}", 
                    result.getTotal(), result.getRecords().size());
        
        return (Page<UserVO>) result.convert(userDO -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userDO, userVO);
            return userVO;
        });
    }
    
    @Override
    public UserVO updateUser(UserDTO userDTO) {
        logger.debug("开始更新用户信息，用户ID: {}", userDTO.getId());
        
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userDTO, userDO);
        
        int updateCount = userMapper.updateById(userDO);
        logger.debug("用户信息更新完成，影响行数: {}", updateCount);
        
        UserVO result = getUserInfo(userDTO.getId());
        logger.info("用户信息更新成功，用户ID: {}", userDTO.getId());
        
        return result;
    }
    @Override
    public void deleteUser(Long userId) {
        logger.debug("开始删除用户，用户ID: {}", userId);
        
        int deleteCount = userMapper.deleteById(userId);
        
        if (deleteCount > 0) {
            logger.info("用户删除成功，用户ID: {}", userId);
        } else {
            logger.warn("用户删除失败，可能用户不存在，用户ID: {}", userId);
        }
    }
}