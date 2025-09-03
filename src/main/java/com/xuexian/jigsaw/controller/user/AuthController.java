package com.xuexian.jigsaw.controller.user;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xuexian.jigsaw.dto.CasPageLogin;
import com.xuexian.jigsaw.dto.LoginFormDTO;
import com.xuexian.jigsaw.dto.UserDTO;
import com.xuexian.jigsaw.entity.User;
import com.xuexian.jigsaw.service.IUserService;
import com.xuexian.jigsaw.util.JwtUtil;
import com.xuexian.jigsaw.util.PasswordEncoder;
import com.xuexian.jigsaw.vo.LoginResponse;
import com.xuexian.jigsaw.vo.Result;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.xuexian.jigsaw.util.Code.*;

@RestController
@RequestMapping("/auth")
@Slf4j
@Tag(name = "登录接口", description = "处理统一认证和自定义登录业务")
public class AuthController {

    @Autowired
    private IUserService userService;

    /**
     * 统一认证拿到一次jwt,解析后重定向到默认拼图页面
     */
    @ResponseBody
    @SneakyThrows
    @RequestMapping("/casLogin")
    public String login(
            @RequestParam String token,
            HttpServletResponse response
    ) {
        Claims claims = JwtUtil.parseToken(token);
        String casId = claims.get("casID").toString();
        if (casId == null) {
            return "登入失败，token不合法！";
        } else {
            User user = userService.getOne(Wrappers.<User>lambdaQuery()
                    .eq(User::getUserName, casId));
            if (user == null) {
                user = new User()
                        .setUserName(casId)
                        .setPassword(PasswordEncoder.encode(casId))
                        .setNickName("用户_" + casId)
                        .setCreateTime(LocalDateTime.now())
                        .setUpdateTime(LocalDateTime.now())
                        .setLevel(1L);
                userService.save(user);
                userService.assignRole(user.getId(), "USER");
            }
            response.sendRedirect(CasPageLogin.DEFAULT_FORWARD + "?casId=" + casId);
            return null;
        }
    }

    /**
     * 从拼图首页登陆的自定义登录业务
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录接口", description = "通过用户名和密码登录，返回JWT和用户信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "-10000", description = "用户不存在"),
            @ApiResponse(responseCode = "-10001", description = "密码错误")
    })
    public Result<LoginResponse> login(
            @Parameter(description = "JSON格式接受userName和password")
            @RequestBody LoginFormDTO loginForm
    ) {
        User user = userService.getOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUserName, loginForm.getUserName()));

        if (user == null) {
            return Result.error(USER_NOT_EXIST, "用户不存在");
        }

        if (!PasswordEncoder.matches(user.getPassword(), loginForm.getPassword())) {
            return Result.error(PASSWORD_ERROR, "密码错误");
        }

        List<String> roles = userService.getUserRoles(user.getId());
        if (roles.isEmpty()) {
            roles = List.of("USER");
        }

        UserDTO userDTO = new UserDTO(user.getId(), user.getNickName(), roles);

        String jwt = JwtUtil.generateToken(user.getId(), user.getUserName(), userDTO.getRoles());
        log.info("jwt:{}", jwt);

        LoginResponse data = new LoginResponse();
        data.setUser(userDTO);
        data.setToken(jwt);
        return Result.success(REQUEST_SUCCESS, data);
    }
}
