package com.xuexian.jigsaw.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xuexian.jigsaw.dto.CasPageLogin;
import com.xuexian.jigsaw.dto.LoginFormDTO;
import com.xuexian.jigsaw.dto.UserDTO;
import com.xuexian.jigsaw.entity.User;
import com.xuexian.jigsaw.service.IUserService;
import com.xuexian.jigsaw.util.JwtUtil;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import io.jsonwebtoken.Claims;
import io.lettuce.core.models.stream.ClaimedMessages;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private  IUserService userService;

    /**
     * 统一认证拿到casId后重定向到默认拼图页面
     * @param token
     * @param response
     * @return
     */
    @ResponseBody
    @SneakyThrows
    @RequestMapping("/casLogin")
    public String login(@RequestParam String token, HttpServletResponse response) {
        // 后面挪到service

        // 存储在服务器上的key应该和存储在服务器上的key相同
        Claims claims = JwtUtil.parseToken(token);
        String casId = claims.get("casID").toString();
        // 如果解密token失败，那么这是一个非法的登入
        if (casId == null) {
            // 非法登入逻辑处理
            return "登入失败，token不合法！";
        }
        // 如果解密token成功，那么这是一个合法的登入
        else {
            // 合法登入逻辑处理
            response.sendRedirect(CasPageLogin.DEFAULT_FORWARD + "?casId=" + casId);
            return null;
        }
    }


    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm) {
        // 查询用户
        User user = userService.getOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUserName, loginForm.getUserName())
        );

        if (user == null) {
            return Result.fail("用户名不存在");
        }

        if (!user.getPassword().equals(loginForm.getPassword())) {
            return Result.fail("密码错误");
        }

        // 构建 UserDTO
        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getNickName(),
                user.getIcon(),
                user.getLevel(),
                List.of("USER") // 暂定,到时候从表拿
        );

        // 存入 UserHolder
        UserHolder.saveUser(userDTO);

        // 生成 JWT
        String jwt = JwtUtil.generateToken(user.getId(), user.getNickName(), userDTO.getRoles());

        return Result.ok(Map.of(
                "user", userDTO,
                "token", jwt
        ));
    }
}
