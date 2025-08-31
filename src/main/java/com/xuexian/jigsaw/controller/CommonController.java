package com.xuexian.jigsaw.controller;


import com.xuexian.jigsaw.dto.UserDTO;
import com.xuexian.jigsaw.service.IUserService;
import com.xuexian.jigsaw.util.AliOssUtil;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/common")

@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil ossUtil;

    @Autowired
    private IUserService userService;

    @PostMapping("/userUpload")
    public Result upload(MultipartFile file){
        log.info("用户头像上传: {}",file);
        UserDTO userDTO = UserHolder.getUser();

        try {
            //原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取扩展名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString()+ extension;
            //获取路径
            String filePath = ossUtil.upload(file.getBytes(),newFilename);

            // 更新数据库头像字段
            boolean updated = userService.lambdaUpdate()
                    .eq(com.xuexian.jigsaw.entity.User::getId, userDTO.getId())
                    .set(com.xuexian.jigsaw.entity.User::getIcon, filePath)
                    .update();

            if (!updated) {
                log.warn("头像更新失败，用户ID：{}", userDTO.getId());
                return Result.error("头像更新失败");
            }


            return Result.success(filePath);
        } catch (IOException e) {
            log.info("文件上传失败: {}",e.getMessage());
        }
        return Result.error("文件上传失败");
    }
}
