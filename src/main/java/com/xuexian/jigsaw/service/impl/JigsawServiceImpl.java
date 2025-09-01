package com.xuexian.jigsaw.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuexian.jigsaw.entity.Jigsaw;
import com.xuexian.jigsaw.mapper.JigsawMapper;
import com.xuexian.jigsaw.service.IJigsawService;
import com.xuexian.jigsaw.vo.ScrollResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@Service
public class JigsawServiceImpl extends ServiceImpl<JigsawMapper, Jigsaw> implements IJigsawService {
    @Autowired
    private JigsawMapper jigsawMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;




    /**
     * 滚动查询拼图列表
     * @param categoryId
     * @param maxTime
     * @param offset
     * @param limit
     * @param userId
     * @return
     */
    @Override
    public ScrollResult<Jigsaw> scrollJigsawByCategory(Integer categoryId, LocalDateTime maxTime, Integer offset, Integer limit, Long userId) {
        if (maxTime == null) {
            maxTime = LocalDateTime.now();
        }
        if (offset == null) {
            offset = 0;
        }
        List<Jigsaw> jigsaws = jigsawMapper.scrollByCategory(categoryId, maxTime, offset, limit);
        if (jigsaws.isEmpty()) {
            return new ScrollResult<>(Collections.emptyList(), null, 0);
        }

        // 计算新的 minTime 和 offset
        LocalDateTime minTime = jigsaws.get(jigsaws.size() - 1).getCreatedAt();
        int os = 0;
        for (Jigsaw j : jigsaws) {
            if (j.getCreatedAt().isEqual(minTime)) {
                os++;
            }
        }

        // TODO: 如果需要，查询每个拼图用户是否已完成，可通过 record 表 join 查询
        // List<Long> jigsawIds = jigsaws.stream().map(Jigsaw::getId).collect(Collectors.toList());
        // Map<Long, Boolean> userDoneMap = recordMapper.checkUserDone(userId, jigsawIds);

        return new ScrollResult<>(jigsaws, minTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), os);
    }



}
