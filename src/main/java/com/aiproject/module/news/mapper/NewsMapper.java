package com.aiproject.module.news.mapper;

import com.aiproject.module.news.model.News;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * News Mapper
 */
@Mapper
public interface NewsMapper extends BaseMapper<News> {
}
