package com.xuexian.jigsaw.vo;

import com.xuexian.jigsaw.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "登录响应数据")
public class LoginResponse {

    @Schema(description = "用户信息")
    private UserDTO user;

    @Schema(description = "JWT令牌")
    private String token;
}
