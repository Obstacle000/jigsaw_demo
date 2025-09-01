package com.xuexian.jigsaw.controller.user;

import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Api(tags = "用户信息相关接口")
public class ApiController {

    @GetMapping("/me")
    @ApiOperation("新获取用户信息")
    public Result me() {
        // 拦截器里已经放好了
        return Result.success(UserHolder.getUser());
    }
}
