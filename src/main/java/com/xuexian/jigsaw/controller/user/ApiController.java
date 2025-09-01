package com.xuexian.jigsaw.controller.user;

import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/me")
    public Result me() {
        // 拦截器里已经放好了
        return Result.success(UserHolder.getUser());
    }
}
