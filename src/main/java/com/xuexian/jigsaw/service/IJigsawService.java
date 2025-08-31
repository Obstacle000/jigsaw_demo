package com.xuexian.jigsaw.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuexian.jigsaw.entity.Jigsaw;
import com.xuexian.jigsaw.vo.Result;
import com.xuexian.jigsaw.vo.ScrollResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

public interface IJigsawService extends IService<Jigsaw> {

    ScrollResult<Jigsaw> scrollJigsawByCategory(Integer categoryId, LocalDateTime maxTime, Integer offset, Integer limit, Long userId);


}
