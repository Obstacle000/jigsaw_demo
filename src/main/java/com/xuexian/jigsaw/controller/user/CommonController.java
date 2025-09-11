package com.xuexian.jigsaw.controller.user;


import cn.hutool.core.img.ImgUtil;
import cn.hutool.json.JSONUtil;
import com.xuexian.jigsaw.dto.UserDTO;
import com.xuexian.jigsaw.entity.Category;
import com.xuexian.jigsaw.entity.Jigsaw;
import com.xuexian.jigsaw.entity.Piece;
import com.xuexian.jigsaw.mapper.CategoryMapper;
import com.xuexian.jigsaw.mapper.JigsawMapper;
import com.xuexian.jigsaw.mapper.PieceMapper;
import com.xuexian.jigsaw.service.CommonService;
import com.xuexian.jigsaw.service.IUserService;
import com.xuexian.jigsaw.service.impl.CategoryServiceImpl;
import com.xuexian.jigsaw.service.impl.JigsawServiceImpl;
import com.xuexian.jigsaw.util.AliOssUtil;
import com.xuexian.jigsaw.util.Code;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;


import static com.xuexian.jigsaw.util.Code.*;
import static com.xuexian.jigsaw.util.RedisConstants.CURRENT_KEY;
import static com.xuexian.jigsaw.util.RedisConstants.INITIAL_KEY;


/**
 * 通用接口
 */
@RestController
@RequestMapping("/common")
@Tag(name = "通用接口",description="管理员上传,以及用户上传头像")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil ossUtil;

    @Autowired
    private IUserService userService;

    @Autowired
    private CommonService commonService;
    @PostMapping("/userUpload")
    @Operation(summary="上传用户头像",description = "上传头像")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功"),
            @ApiResponse(responseCode = "-20002", description = "头像更新失败"),
            @ApiResponse(responseCode = "-20003", description = "文件上传失败"),
    })
    public Result upload( @RequestParam("file") MultipartFile file){
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
                return Result.error(AVATAR_UPDATE_FAIL,"头像更新失败");
            }


            return Result.success(REQUEST_SUCCESS,filePath);
        } catch (IOException e) {
            log.info("文件上传失败: {}",e.getMessage());
        }
        return Result.error(FILE_UPLOAD_FAIL,"文件上传失败");
    }




    /** 新增或更新拼图 */
    @PostMapping("/uploadJigsaw")
    @Operation(summary = "上传或更新完整拼图图片及基本信息（拼图块自动分割）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功" ),
            @ApiResponse(responseCode = "-10", description = "无权限"),
            @ApiResponse(responseCode = "-10002", description = "分类不存在"),
            @ApiResponse(responseCode = "-10003", description = "拼图不存在"),
            @ApiResponse(responseCode = "-20003", description = "文件上传失败"),
            @ApiResponse(responseCode = "-1", description = "系统未知错误"),
    })
    public Result uploadJigsaw(
             @RequestParam(value="jigsawId", required=false) Long jigsawId,
             @RequestParam(value = "categoryId") Integer categoryId,
            @RequestParam("title") String title,
            @RequestParam("pieces") Integer pieceCount,
            @RequestParam("background") String background,
            @RequestParam("file") MultipartFile file) {

        return commonService.uploadJigsaw(jigsawId,categoryId,title,pieceCount,background,file);

    }





    /** 上传或更新分类封面 */
    @PostMapping("/categoryUpload")
    @Operation(summary = "上传或更新分类封面图片，如果分类不存在则新增分类")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功" ),
            @ApiResponse(responseCode = "-10", description = "无权限"),
            @ApiResponse(responseCode = "-20000", description = "封面更新失败"),
            @ApiResponse(responseCode = "-20003", description = "文件上传失败"),
            @ApiResponse(responseCode = "-1", description = "系统未知错误"),
    })
    public Result uploadCategoryCover(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) {

        return commonService.uploadCategoryCover(categoryId,name,file);

    }

    /** 删除拼图 */
    @PostMapping("/deleteJigsaw")
    @Operation(summary = "删除拼图（及其所有拼图块）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功" ),
            @ApiResponse(responseCode = "-10", description = "无权限"),
            @ApiResponse(responseCode = "-10003", description = "拼图不存在"),
            @ApiResponse(responseCode = "-1", description = "系统未知错误"),
    })
    public Result deleteJigsaw(@RequestParam("jigsawId") Long jigsawId) {
        return commonService.deleteJigsaw(jigsawId);
    }

    /** 删除分类 */
    @PostMapping("/deleteCategory")
    @Operation(summary = "删除分类（及其所有拼图和拼图块）")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "成功" ),
            @ApiResponse(responseCode = "-10", description = "无权限"),
            @ApiResponse(responseCode = "-10002", description = "分类不存在"),
            @ApiResponse(responseCode = "-1", description = "系统未知错误"),
    })
    public Result deleteCategory(@RequestParam("categoryId") Integer categoryId) {
        return commonService.deleteCategory(categoryId);
    }



//    /** 上传或更新拼图背景 */
//    @PostMapping("/uploadBackground")
//    @Operation(summary = "上传或更新拼图背景图片")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "成功"),
//            @ApiResponse(responseCode = "-10", description = "无权限"),
//            @ApiResponse(responseCode = "-10003", description = "拼图不存在"),
//            @ApiResponse(responseCode = "-20001", description = "背景更新失败"),
//            @ApiResponse(responseCode = "-20003", description = "文件上传失败"),
//            @ApiResponse(responseCode = "-1", description = "系统未知错误"),
//    })
//    public Result uploadJigsawBackground(
//            @RequestParam("jigsawId") Long jigsawId,
//             @RequestParam("file") MultipartFile file) {
//
//        return commonService.uploadJigsawBackground(jigsawId,file);
//
//    }

}
