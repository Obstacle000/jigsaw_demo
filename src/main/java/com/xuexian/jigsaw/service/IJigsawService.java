package com.xuexian.jigsaw.service;

import com.xuexian.jigsaw.entity.Jigsaw;
import com.xuexian.jigsaw.vo.Result;
import com.xuexian.jigsaw.vo.ScrollResult;

import java.time.LocalDateTime;

public interface IJigsawService {

    ScrollResult<Jigsaw> scrollJigsawByCategory(Integer categoryId, LocalDateTime maxTime, Integer offset, Integer limit, Long userId);


}
