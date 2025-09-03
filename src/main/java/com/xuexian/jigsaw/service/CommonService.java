package com.xuexian.jigsaw.service;

import com.xuexian.jigsaw.vo.Result;
import org.springframework.web.multipart.MultipartFile;

public interface CommonService {
    Result uploadJigsawBackground(Long jigsawId,MultipartFile file);
    Result uploadCategoryCover(Integer categoryId,String name,MultipartFile file);
    Result uploadJigsaw(Long jigsawId, Integer categoryId, String title, Integer pieceCount, String background, MultipartFile file);
}
