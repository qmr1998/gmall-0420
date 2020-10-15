package com.atguigu.gmall.ums.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    // 用户数据的校验，主要包括对：手机号、用户名、邮箱的唯一性校验
    @Override
    public Boolean checkData(String data, Integer type) {
        // type：要校验的数据类型：1，用户名；2，手机；3，邮箱
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();

        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }
        // 等于0表示数据库中未查到，可用
        return this.baseMapper.selectCount(wrapper) == 0;
    }

    // 用户注册
    @Override
    public void register(UserEntity userEntity, String code) {
        // 1. TODO：查询redis中的验证码，校验验证码

        // 2. 生成随机字符串作为salt
        String salt = UUID.randomUUID().toString().substring(0, 6);
        userEntity.setSalt(salt);

        // 3.对密码进行盐加密
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword() + salt));

        // 4. 新增用户信息
        userEntity.setLevelId(1L);
        userEntity.setStatus(1);
        userEntity.setCreateTime(new Date());
        userEntity.setSourceType(1);
        userEntity.setIntegration(1000);
        userEntity.setGrowth(1000);
        this.save(userEntity);

        // 5. TODO：删除redis中的短信验证码
    }

    // 查询用户
    @Override
    public UserEntity queryUser(String loginName, String password) {
        // 1. 根据登录名查询用户信息
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        UserEntity userEntity = this.getOne(wrapper.eq("username", loginName).or().eq("phone", loginName).or().eq("email", loginName));

        // 2. 判断用户是否存在
        if (userEntity == null) {
            return null;
        }

        // 3. 获取用户信息中的盐，对用户输入明文密码加盐加密
        String salt = userEntity.getSalt();
        password = DigestUtils.md5Hex(password + salt);

        // 4. 拿数据库中密码 和 上一步 加盐加密后的密码比较
        String dbPassword = userEntity.getPassword();
        if (!StringUtils.equals(dbPassword, password)) {
            return null;
        }

        return userEntity;
    }
}