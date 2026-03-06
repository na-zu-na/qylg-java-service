package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.ArticleDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.Articles;
import com.cc.qylgjavaservice.mapper.ArticleMapper;
import com.cc.qylgjavaservice.service.ArticleService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cc.qylgjavaservice.utils.RedisConstants.DISCOVER_LIST_KEY_PREFIX;
import static com.cc.qylgjavaservice.utils.RedisConstants.HOT_ARTICLE_KEY;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper,Articles> implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Result<Page<ArticleDTO>> getArticles(int current, int pageSize) {
        Page<ArticleDTO> page=new Page<>(current,pageSize);
        Page<ArticleDTO> articles = articleMapper.selectArticlePage(page);

        return Result.success(articles);
    }

    @Override
    public Result<List<ArticleDTO>> getHotArticle() {
        RBucket<List<ArticleDTO>> bucket=redissonClient.getBucket(HOT_ARTICLE_KEY);
        List<ArticleDTO> articleDTOList;

        //查缓存
        if (bucket.isExists()){
            articleDTOList = bucket.get();
            return Result.success(articleDTOList);
        }

        //查数据库
        articleDTOList = articleMapper.selectHotArticle();
        if (articleDTOList!=null && !articleDTOList.isEmpty())
        {
            bucket.set(articleDTOList, Duration.ofMinutes(10));
            return Result.success(articleDTOList);
        }
        return Result.fail(404,"无热点数据");
    }

    @Override
    public Result<List<ArticleDTO>> getDiscoverList(int type) {
        RBucket<List<ArticleDTO>> bucket=redissonClient.getBucket(DISCOVER_LIST_KEY_PREFIX+type);

        //查缓存
        if (bucket.isExists()){
            List<ArticleDTO> articleDTOList = bucket.get();
            return Result.success(articleDTOList);
        }

        //查数据库
        List<ArticleDTO> articleDTOList = articleMapper.selectDiscoverList(type);
        if (articleDTOList!=null && !articleDTOList.isEmpty()){
            bucket.set(articleDTOList,Duration.ofMinutes(10));
            return Result.success(articleDTOList);
        }
        return Result.fail(404,"无该类数据");
    }
}
