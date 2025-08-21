package com.xuexian.jigsaw.controller;

import com.xuexian.jigsaw.dto.LoginFormDTO;
import com.xuexian.jigsaw.util.JwtUtil;
import com.xuexian.jigsaw.vo.Result;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public Result login(LoginFormDTO loginForm) {
       return null;
    }
}
