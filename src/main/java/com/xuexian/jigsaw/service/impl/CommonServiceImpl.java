package com.xuexian.jigsaw.service.impl;

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
import com.xuexian.jigsaw.util.AliOssUtil;
import com.xuexian.jigsaw.util.Code;
import com.xuexian.jigsaw.util.UserHolder;
import com.xuexian.jigsaw.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static com.xuexian.jigsaw.util.RedisConstants.INITIAL_KEY;

@Service
@Slf4j
public class CommonServiceImpl implements CommonService {
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

    @Override
    public Result uploadJigsawBackground(Long jigsawId, MultipartFile file) {
        if (!isAdmin()) return Result.error(Code.NO_GRANTED, "无权限");

        Jigsaw jigsaw = jigsawMapper.selectById(jigsawId);
        if (jigsaw == null) return Result.error(Code.JIGSAW_NOT_EXIST, "拼图不存在");

        try {
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            String newFilename = "jigsaw_" + jigsawId + "_background" + extension;
            String url = ossUtil.upload(file.getBytes(), newFilename);

            boolean updated = jigsawService.lambdaUpdate()
                    .eq(Jigsaw::getId, jigsawId)
                    .set(Jigsaw::getBackground, url)
                    .update();

            if (!updated) return Result.error(Code.BACKGROUND_COVER_UPDATE_FAIL, "背景更新失败");

            return Result.success(Code.REQUEST_SUCCESS, url);
        } catch (IOException e) {
            log.error("上传拼图背景失败", e);
            return Result.error(Code.FILE_UPLOAD_FAIL, "文件上传失败");
        } catch (Exception e) {
            log.error("上传拼图背景失败", e);
            return Result.error(Code.SYSTEM_UNKNOWN_ERR, "系统未知错误");
        }
    }

    @Override
    public Result uploadCategoryCover(Integer categoryId, String name, MultipartFile file) {
        if (!isAdmin()) return Result.error(Code.NO_GRANTED, "无权限");

        try {
            Category category = null;

            // 判断分类是否存在
            if (categoryId != null) {
                category = categoryMapper.selectById(categoryId);
            }

            // 不存在则新增
            if (category == null) {
                category = new Category();
                category.setName(name);
                categoryMapper.insert(category);
                categoryId = category.getCategoryId(); // 获取数据库生成的ID
            }

            // 上传封面图片
            String url = ossUtil.upload(file.getBytes(), "category_" + categoryId + "_" + file.getOriginalFilename());

            // 更新封面字段
            boolean updated = categoryService.lambdaUpdate()
                    .eq(Category::getCategoryId, categoryId)
                    .set(Category::getCover, url)
                    .update();

            if (!updated) return Result.error(Code.CLASSIFICATION_COVER_UPDATE_FAIL, "封面更新失败");

            return Result.success(Code.REQUEST_SUCCESS, url);
        } catch (IOException e) {
            log.error("上传分类封面失败", e);
            return Result.error(Code.FILE_UPLOAD_FAIL, "文件上传失败");
        } catch (Exception e) {
            log.error("上传分类封面失败", e);
            return Result.error(Code.SYSTEM_UNKNOWN_ERR, "系统未知错误");
        }
    }

    @Override
    public Result uploadJigsaw(Long jigsawId, Integer categoryId, String title, Integer pieceCount, String background, MultipartFile file) {
        if (!isAdmin()) return Result.error(Code.NO_GRANTED, "无权限");

        // 校验分类
        Category category = categoryMapper.selectById(categoryId);
        if (category == null) return Result.error(Code.CLASSIFICATION_NOT_EXIST, "分类不存在");

        try {
            if (jigsawId != null) {
                // 更新逻辑
                Jigsaw jigsaw = jigsawMapper.selectById(jigsawId);
                if (jigsaw == null) return Result.error(Code.JIGSAW_NOT_EXIST, "拼图不存在");

                String fullUrl = ossUtil.upload(file.getBytes(), category.getName() + "_" + title + "_" + file.getOriginalFilename());
                jigsaw.setTitle(title);
                jigsaw.setPieceCount(pieceCount);
                jigsaw.setBackground(background);
                jigsaw.setUrl(fullUrl);
                jigsawMapper.updateById(jigsaw);

                splitAndSavePieces(jigsaw, file, pieceCount, category);

                return Result.success(Code.REQUEST_SUCCESS, "拼图更新成功");
            } else {
                // 新增逻辑
                Jigsaw jigsaw = new Jigsaw();
                String fullUrl = ossUtil.upload(file.getBytes(), category.getName() + "_" + title + "_" + file.getOriginalFilename());
                jigsaw.setCategoryId(categoryId);
                jigsaw.setTitle(title);
                jigsaw.setPieceCount(pieceCount);
                jigsaw.setBackground(background);
                jigsaw.setUrl(fullUrl);
                jigsaw.setCreatedAt(LocalDateTime.now());
                jigsawMapper.insert(jigsaw);

                // 切分拼图块逻辑
                splitAndSavePieces(jigsaw, file, pieceCount, category);

                return Result.success(Code.REQUEST_SUCCESS, "拼图新增成功");
            }
        } catch (IOException e) {
            log.error("上传拼图失败", e);
            return Result.error(Code.FILE_UPLOAD_FAIL, "文件上传失败");
        } catch (Exception e) {
            log.error("上传拼图失败", e);
            return Result.error(Code.SYSTEM_UNKNOWN_ERR, "系统未知错误");
        }
    }

    /** 判断是否管理员 */
    private boolean isAdmin() {
        UserDTO user = UserHolder.getUser();
        return user != null && Boolean.TRUE.equals(user.isAdmin());
    }

    /** 切分拼图块方法 */
    /** 切分拼图块方法，同时生成初始状态存 Redis */
    private void splitAndSavePieces(Jigsaw jigsaw, MultipartFile file, int pieceCount, Category category) throws IOException {
        Long id = UserHolder.getUser().getId();
        BufferedImage image = ImageIO.read(file.getInputStream());
        int rows = (int) Math.sqrt(pieceCount);
        int cols = (int) Math.ceil((double) pieceCount / rows);
        int pieceWidth = image.getWidth() / cols;
        int pieceHeight = image.getHeight() / rows;

        List<Map<String, Object>> initialPieces = new ArrayList<>();

        // 随机位置范围（根据你的前端画布大小自定义）
        int minX = 50;
        int maxX = 800;
        int minY = 400;
        int maxY = 800;
        Random random = new Random();

        int pieceNumber = 1;
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                if (pieceNumber > pieceCount) break;

                BufferedImage subImage = image.getSubimage(
                        x * pieceWidth, y * pieceHeight,
                        Math.min(pieceWidth, image.getWidth() - x * pieceWidth),
                        Math.min(pieceHeight, image.getHeight() - y * pieceHeight)
                );

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                ImgUtil.write(subImage, extension, os);

                String pieceName = category.getName() + "_" + jigsaw.getTitle() + "_piece_" + pieceNumber + "." + extension;
                String pieceUrl = ossUtil.upload(os.toByteArray(), pieceName);

                // 保存数据库
                Piece piece = new Piece();
                piece.setJigsawId(jigsaw.getId().intValue());
                piece.setPieceNumber(pieceNumber);
                piece.setUrl(pieceUrl);
                piece.setCreatedAt(LocalDateTime.now());
                piece.setUpdatedAt(LocalDateTime.now());
                pieceMapper.insert(piece);

                // 生成随机初始位置
                int randomX = minX + random.nextInt(maxX - minX + 1);
                int randomY = minY + random.nextInt(maxY - minY + 1);

                // 构建初始状态 JSON
                Map<String, Object> map = new HashMap<>();
                map.put("pieceNumber", pieceNumber);
                map.put("x", randomX);
                map.put("y", randomY);
                map.put("placed", false);
                map.put("url", pieceUrl);
                initialPieces.add(map);

                pieceNumber++;
            }
        }

        // 存 Redis 模板
        String templateKey = String.format(INITIAL_KEY, jigsaw.getId(), 0L);
        stringRedisTemplate.opsForValue().set(templateKey, JSONUtil.toJsonStr(initialPieces));
    }

}
