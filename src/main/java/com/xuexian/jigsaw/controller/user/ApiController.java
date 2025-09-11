package com.xuexian.jigsaw.controller.user;

import com.xuexian.jigsaw.entity.User;
import com.xuexian.jigsaw.service.IUserService;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.xuexian.jigsaw.util.Code.REQUEST_SUCCESS;

@RestController
@RequestMapping("/api")
@Tag(name = "用户信息相关接口", description = "获取当前登录用户的信息接口")
public class ApiController {

    @Autowired
    private IUserService userService;

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的详细信息")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功")
    })
    public Result<User> me() {
        // 拦截器里已经放好了
        Long id = UserHolder.getUser().getId();
        User user = userService.getById(id);
        user.setPassword(null);
        return Result.success(REQUEST_SUCCESS, user);
    }
}
