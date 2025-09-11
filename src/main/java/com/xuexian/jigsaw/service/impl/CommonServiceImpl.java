package com.xuexian.jigsaw.service.impl;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
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
    @Transactional
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
                categoryId = category.getId(); // 获取数据库生成的ID
            }

            // 上传封面图片
            String url = ossUtil.upload(file.getBytes(), "category_" + categoryId + "_" + file.getOriginalFilename());

            // 更新封面字段
            boolean updated = categoryService.lambdaUpdate()
                    .eq(Category::getId, categoryId)
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
    @Transactional
    public Result uploadJigsaw(Long jigsawId, Integer categoryId, String title, Integer pieceCount, String background, MultipartFile file) {
        if (!isAdmin()) return Result.error(Code.NO_GRANTED, "无权限");

        // 校验分类
        Category category = categoryMapper.selectById(categoryId);
        category.setTotal(category.getTotal() + 1);
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
                jigsaw.setUpdatedAt(LocalDateTime.now());
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

    /**
     * 判断是否管理员
     */
    private boolean isAdmin() {
        UserDTO user = UserHolder.getUser();
        return user != null && Boolean.TRUE.equals(user.isAdmin());
    }

    /** 切分拼图块方法 */
    /**
     * 切分拼图块方法，同时生成初始状态存 Redis
     */
    private void splitAndSavePieces(Jigsaw jigsaw, MultipartFile file, int pieceCount, Category category) throws IOException {
        BufferedImage image = ImageIO.read(file.getInputStream());
        int rows = (int) Math.sqrt(pieceCount);
        int cols = (int) Math.ceil((double) pieceCount / rows);
        int pieceWidth = image.getWidth() / cols;
        int pieceHeight = image.getHeight() / rows;

        List<Map<String, Object>> initialPieces = new ArrayList<>();

        int pieceNumber = 1;
        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= cols; col++) {
                if (pieceNumber > pieceCount) break;

                BufferedImage subImage = image.getSubimage(
                        (col - 1) * pieceWidth,
                        (row - 1) * pieceHeight,
                        Math.min(pieceWidth, image.getWidth() - (col - 1) * pieceWidth),
                        Math.min(pieceHeight, image.getHeight() - (row - 1) * pieceHeight)
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

                // 构建初始状态 JSON
                Map<String, Object> map = new HashMap<>();
                map.put("pieceNumber", pieceNumber);
                map.put("x", 0); // 初始未拼上
                map.put("y", 0); // 初始未拼上
                map.put("placed", false);
                map.put("url", pieceUrl);
                // 可选：记录正确位置方便判断
                map.put("correctX", col); // 表格列坐标
                map.put("correctY", row); // 表格行坐标

                initialPieces.add(map);
                pieceNumber++;
            }
        }

        // 打乱顺序
        Collections.shuffle(initialPieces);

        // 存 Redis 模板
        String templateKey = String.format(INITIAL_KEY, jigsaw.getId(), 0L);
        stringRedisTemplate.opsForValue().set(templateKey, JSONUtil.toJsonStr(initialPieces));
    }

    @Override
    @Transactional
    public Result deleteJigsaw(Long jigsawId) {
        Jigsaw jigsaw = jigsawMapper.selectById(jigsawId);
        if (jigsaw == null || jigsaw.getDeletedAt() != null) {
            return Result.error(Code.JIGSAW_NOT_EXIST, "拼图不存在或已删除");
        }

        // 校验分类
        Category category = categoryMapper.selectById(jigsaw.getCategoryId());
        category.setTotal(category.getTotal() - 1);

        LocalDateTime now = LocalDateTime.now();

        // 标记拼图为已删除
        jigsaw.setDeletedAt(now);
        jigsaw.setUpdatedAt(now);
        jigsawMapper.updateById(jigsaw);

        // 标记拼图下的拼图块为已删除
        List<Piece> pieces = pieceMapper.selectList(new QueryWrapper<Piece>().eq("jigsaw_id", jigsawId));
        for (Piece piece : pieces) {
            piece.setDeletedAt(now);
            piece.setUpdatedAt(now);
            pieceMapper.updateById(piece);
        }

        return Result.success(Code.REQUEST_SUCCESS, "拼图删除成功");
    }

    @Override
    @Transactional
    public Result deleteCategory(Integer categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeletedAt() != null) {
            return Result.error(Code.CLASSIFICATION_NOT_EXIST, "分类不存在或已删除");
        }

        LocalDateTime now = LocalDateTime.now();

        // 标记分类为已删除
        category.setDeletedAt(now);
        category.setUpdatedAt(now);
        categoryMapper.updateById(category);

        // 找到该分类下的所有拼图
        List<Jigsaw> jigsaws = jigsawMapper.selectList(new QueryWrapper<Jigsaw>().eq("category_id", categoryId));
        for (Jigsaw jigsaw : jigsaws) {
            jigsaw.setDeletedAt(now);
            jigsaw.setUpdatedAt(now);
            jigsawMapper.updateById(jigsaw);

            // 删除拼图下的拼图块
            List<Piece> pieces = pieceMapper.selectList(new QueryWrapper<Piece>().eq("jigsaw_id", jigsaw.getId()));
            for (Piece piece : pieces) {
                piece.setDeletedAt(now);
                piece.setUpdatedAt(now);
                pieceMapper.updateById(piece);
            }
        }

        return Result.success(Code.REQUEST_SUCCESS, "分类及其拼图已删除");
    }
}

