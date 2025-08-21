package com.xuexian.jigsaw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String nickName;
    private List<String> roles;



}
