package com.cc.qylgjavaservice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.CommentsDTO;
import com.cc.qylgjavaservice.dto.ArticleDTO;
import com.cc.qylgjavaservice.dto.ArticleDetailDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ArticleComments;
import com.cc.qylgjavaservice.entity.Articles;
import com.cc.qylgjavaservice.mapper.ArticleMapper;
import com.cc.qylgjavaservice.service.ArticleService;
import org.apache.ibatis.javassist.runtime.Inner;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.stream.events.Comment;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.cc.qylgjavaservice.utils.RedisConstants.*;

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

    @Override
    public Result<ArticleDetailDTO> getArticleDetail(int id) {
        RBucket<ArticleDetailDTO> bucket=redissonClient.getBucket(HOT_ARTICLE_KEY_DETAIL+id);
        if (bucket.isExists()){
            return Result.success(bucket.get());
        }

        ArticleDTO articleDTO=articleMapper.selectArticleDetail(id);

        List<CommentsDTO> commentsDTO=articleMapper.selectArticleComments(id);

        if (commentsDTO!=null && !commentsDTO.isEmpty()){
            commentsDTO=buildCommentsTree(commentsDTO);
        }

        ArticleDetailDTO articleDetailDTO=new ArticleDetailDTO();
        articleDetailDTO.setArticleDTO(articleDTO);
        articleDetailDTO.setCommentsDTO(commentsDTO);

        int commentCount=articleDTO.getCommentCount();
        int likeCount= articleDTO.getLikeCount();

        //暂定点赞数和评论数大于10属于热门
        if (likeCount>10 && commentCount>10){
            bucket.set(articleDetailDTO,Duration.ofMinutes(10));
        }

        return Result.success(articleDetailDTO);
    }


    //格式化评论
    private List<CommentsDTO> buildCommentsTree(List<CommentsDTO> commentsDTO) {
        List<CommentsDTO> roots=new ArrayList<>();

        HashMap<Integer,CommentsDTO> map=new HashMap<>();

        for (CommentsDTO comments : commentsDTO){
            comments.setReplies(new ArrayList<>());
            map.put(comments.getId(),comments);
        }

        for (CommentsDTO comments:commentsDTO){
            Integer parentId=comments.getParentId();
            if (parentId==0 || parentId==null){
                roots.add(comments);
            }
            else {
                CommentsDTO parent= map.get(parentId);
                if (parent!=null){
                    parent.getReplies().add(comments);
                }
                else {
                    roots.add(comments);
                }
            }
        }

        return roots;
    }
}
