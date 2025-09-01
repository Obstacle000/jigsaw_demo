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
import com.xuexian.jigsaw.service.IUserService;
import com.xuexian.jigsaw.service.impl.CategoryServiceImpl;
import com.xuexian.jigsaw.service.impl.JigsawServiceImpl;
import com.xuexian.jigsaw.util.AliOssUtil;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
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

import static com.xuexian.jigsaw.util.RedisConstants.CURRENT_KEY;

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

    @Autowired
    private JigsawMapper jigsawMapper;
    @Autowired
    private PieceMapper pieceMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryServiceImpl categoryService;
    @Autowired
    private JigsawServiceImpl jigsawService;

    @Autowired
    private RedisTemplate stringRedisTemplate;

    @PostMapping("/userUpload")
    public Result upload(@RequestParam("file") MultipartFile file){
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

    @PostMapping("/uploadJigsaw")
    public Result uploadJigsaw(@RequestParam("categoryId") Integer categoryId,
                               @RequestParam("title") String title,
                               @RequestParam("pieces") Integer pieceCount,
                               @RequestParam("background") String background, // 新增
                               @RequestParam("file") MultipartFile file) {
        if(!isAdmin()) return Result.error("无权限");
        try {
            // 查询分类信息
            Category category = categoryMapper.selectById(categoryId);
            if (category == null) {
                return Result.error("分类不存在");
            }

            // 上传完整图片
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fullFileName = category.getName() + "_" + title + extension;
            String fullUrl = ossUtil.upload(file.getBytes(), fullFileName);

            // 保存 jigsaw 表
            Jigsaw jigsaw = new Jigsaw();
            jigsaw.setCategoryId(categoryId);
            jigsaw.setTitle(title);
            jigsaw.setPieceCount(pieceCount);
            jigsaw.setBackground(background); // 设置背景信息
            jigsaw.setUrl(fullUrl);
            jigsaw.setCreatedAt(LocalDateTime.now());

            jigsawMapper.insert(jigsaw);

            // 读取图片切分
            Image img = ImageIO.read(file.getInputStream());
            BufferedImage image = ImgUtil.toBufferedImage(img);
            int rows = (int) Math.sqrt(pieceCount);
            int cols = (int) Math.ceil((double) pieceCount / rows);
            int pieceWidth = image.getWidth() / cols;
            int pieceHeight = image.getHeight() / rows;

            List<Map<String, Object>> initialPieces = new ArrayList<>();
            int pieceNumber = 1;

            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    if (pieceNumber > pieceCount) break;
                    BufferedImage subImage = image.getSubimage(x * pieceWidth, y * pieceHeight,
                            Math.min(pieceWidth, image.getWidth() - x * pieceWidth),
                            Math.min(pieceHeight, image.getHeight() - y * pieceHeight));
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImgUtil.write(subImage, extension.replace(".", ""), os);

                    String pieceName = category.getName() + "_" + title + "_piece_" + pieceNumber + extension;
                    String pieceUrl = ossUtil.upload(os.toByteArray(), pieceName);

                    // 保存 piece 表
                    Piece piece = new Piece();
                    piece.setJigsawId(jigsaw.getId().intValue());
                    piece.setPieceNumber(pieceNumber);
                    piece.setUrl(pieceUrl);
                    piece.setCreatedAt(LocalDateTime.now());
                    piece.setUpdatedAt(LocalDateTime.now());
                    pieceMapper.insert(piece);

                    // 拼图块位置
                    Map<String, Object> map = new HashMap<>();
                    map.put("pieceNumber", pieceNumber);
                    map.put("x", 10 + (pieceNumber - 1) * 100);
                    map.put("y", 500);
                    map.put("placed", false);
                    map.put("url", pieceUrl);
                    initialPieces.add(map);

                    pieceNumber++;
                }
            }

            // 写入 Redis 当前状态
            String currentKey = String.format(CURRENT_KEY, jigsaw.getId(), "init");
            stringRedisTemplate.opsForValue().set(currentKey, JSONUtil.toJsonStr(initialPieces));

            return Result.success("拼图上传成功");
        } catch (Exception e) {
            log.error("上传拼图失败", e);
            return Result.error("上传拼图失败");
        }
    }

    /**
     * 上传分类封面图片
     * @param file 文件
     * @param categoryId 分类ID
     * @return 上传结果
     */
    @PostMapping("/categoryUpload")
    public Result uploadCategoryCover(@RequestParam("file") MultipartFile file,
                                      @RequestParam("categoryId") Integer categoryId) {
        log.info("上传分类封面图片: {}", file.getOriginalFilename());
        if(!isAdmin()) return Result.error("无权限");
        try {
            // 使用原始文件名，也可以加上前缀 category_ 或者做一定规则
            String originalFilename = file.getOriginalFilename();
            String url = ossUtil.upload(file.getBytes(), "category_" + categoryId + "_" + originalFilename);

            // 更新数据库
            boolean updated = categoryService.lambdaUpdate()
                    .eq(Category::getCategoryId, categoryId)
                    .set(Category::getCover, url)
                    .update();

            if (!updated) {
                log.warn("分类封面更新失败，分类ID：{}", categoryId);
                return Result.error("分类封面更新失败");
            }

            return Result.success(url);
        } catch (Exception e) {
            log.error("分类封面上传失败", e);
            return Result.error("文件上传失败");
        }
    }


    @PostMapping("/admin/jigsaw/uploadBackground")
    public Result uploadJigsawBackground(@RequestParam("file") MultipartFile file,
                                         @RequestParam("jigsawId") Long jigsawId) {
        if(!isAdmin()) return Result.error("无权限");
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // 文件名：jigsaw_{id}_background
            String newFilename = "jigsaw_" + jigsawId + "_background" + extension;

            String filePath = ossUtil.upload(file.getBytes(), newFilename);

            boolean updated = jigsawService.lambdaUpdate()
                    .eq(Jigsaw::getId, jigsawId)
                    .set(Jigsaw::getBackground, filePath)
                    .update();

            if (!updated) {
                return Result.error("背景更新失败");
            }

            return Result.success(filePath);
        } catch (IOException e) {
            log.error("上传拼图背景失败", e);
            return Result.error("文件上传失败");
        }
    }
    /** 判断是否管理员 */
    private boolean isAdmin(){
        UserDTO user = UserHolder.getUser();
        return user != null && Boolean.TRUE.equals(user.isAdmin());
    }
}
