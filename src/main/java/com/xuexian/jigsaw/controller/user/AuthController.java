package com.xuexian.jigsaw.controller.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xuexian.jigsaw.dto.CasPageLogin;
import com.xuexian.jigsaw.dto.LoginFormDTO;
import com.xuexian.jigsaw.dto.UserDTO;
import com.xuexian.jigsaw.entity.User;
import com.xuexian.jigsaw.service.IUserService;
import com.xuexian.jigsaw.util.JwtUtil;
import com.xuexian.jigsaw.util.PasswordEncoder;
import com.xuexian.jigsaw.vo.Result;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private  IUserService userService;

    /**
     * 统一认证拿到一次jwt,解析后重定向到默认拼图页面
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
            // 往数据库里存用户名和密码
            User user = userService.getOne(Wrappers.<User>lambdaQuery()
                    .eq(User::getUserName, casId));
            if (user == null) {
                user = new User()
                        .setUserName(casId)
                        .setPassword(PasswordEncoder.encode(casId))
                        .setNickName("用户" + casId) // 给个默认昵称
                        .setCreateTime(LocalDateTime.now())
                        .setUpdateTime(LocalDateTime.now())
                        .setLevel(1L); // 默认等级
                userService.save(user);
            }
            response.sendRedirect(CasPageLogin.DEFAULT_FORWARD + "?casId=" + casId);
            return null;
        }
    }

    /**
     * 从拼图首页登陆的自定义登录业务
     * @param loginForm
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm) {
        User user = null;
        // 查询用户
        user = userService.getOne(
                Wrappers.<User>lambdaQuery()
                        .eq(User::getUserName, loginForm.getUserName())
        );



        if (!PasswordEncoder.matches( user.getPassword(),loginForm.getPassword())) {
            return Result.error("密码错误");
        }

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getNickName(),
                List.of("USER") // 暂定,到时候从表拿
        );



        // 生成token
        String jwt = JwtUtil.generateToken(user.getId(), user.getUserName(), userDTO.getRoles());
        log.info("jwt:{}", jwt);

        return Result.success(Map.of(
                "user", userDTO,
                "token", jwt
        ));
    }
}
