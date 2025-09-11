package com.xuexian.jigsaw.service;

import com.xuexian.jigsaw.vo.Result;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {
    Result uploadJigsawBackground(Long jigsawId,MultipartFile file);
    Result uploadCategoryCover(Integer categoryId,String name,MultipartFile file);
    Result uploadJigsaw(Long jigsawId, Integer categoryId, String title, Integer pieceCount, String background, MultipartFile file);


    /** 删除拼图及其所有拼图块 */
    Result deleteJigsaw(Long jigsawId);

    /** 删除分类及其下所有拼图和拼图块 */
    Result deleteCategory(Integer categoryId);
}
