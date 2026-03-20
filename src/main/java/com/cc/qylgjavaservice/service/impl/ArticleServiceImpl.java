package com.cc.qylgjavaservice.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cc.qylgjavaservice.dto.articleDTO.*;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ArticleLike;
import com.cc.qylgjavaservice.entity.Articles;
import com.cc.qylgjavaservice.mapper.ArticleLikeMapper;
import com.cc.qylgjavaservice.mapper.ArticleMapper;
import com.cc.qylgjavaservice.service.ArticleService;
import com.cc.qylgjavaservice.service.ArticleSyncService;
import com.cc.qylgjavaservice.utils.UserContext;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cc.qylgjavaservice.utils.RedisConstants.*;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper,Articles> implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ArticleLikeMapper articleLikeMapper;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ArticleSyncService articleSyncService;

    @Override
    public Result<Page<ArticleDTO>> getArticles(int current, int pageSize) {
        Page<ArticleDTO> page=new Page<>(current,pageSize);
        Page<ArticleDTO> articles = articleMapper.selectArticlePage(page);

        return Result.success(articles);
    }

    @Override
    public Result<List<ArticleDTO>> getHotArticle() {
        RBucket<ArticleCacheDTO> bucket=redissonClient.getBucket(HOT_ARTICLE_KEY,new JsonJacksonCodec());
        List<ArticleDTO> articleDTOList;

        //查缓存
        if (bucket.isExists()){
            articleDTOList = bucket.get().getArticleDTOList();
            return Result.success(articleDTOList);
        }

        //查数据库
        articleDTOList = articleMapper.selectHotArticle();
        if (articleDTOList!=null && !articleDTOList.isEmpty())
        {
            ArticleCacheDTO articleCacheDTO=new ArticleCacheDTO();
            articleCacheDTO.setArticleDTOList(articleDTOList);
            bucket.set(articleCacheDTO, Duration.ofMinutes(10));
            return Result.success(articleDTOList);
        }
        return Result.fail(404,"无热点数据");
    }

    @Override
    public Result<List<ArticleDTO>> getDiscoverList(int type) {
        RBucket<List<ArticleDTO>> bucket=redissonClient.getBucket(DISCOVER_LIST_KEY_PREFIX+type,new JsonJacksonCodec());

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

        //查文章信息
        ArticleDTO articleDTO=articleMapper.selectArticleDetail(id);

        //查评论信息
        List<CommentsDTO> commentsDTO=articleMapper.selectArticleComments(id);

        //查找点赞信息
        QueryWrapper<ArticleLike> wrapper = new QueryWrapper<>();
        wrapper.eq("article_id", articleDTO.getId())
                .eq("user_id", UserContext.getCurrentUserId());

        ArticleLike like = articleLikeMapper.selectOne(wrapper);
        boolean isLiked= like != null;
        articleDTO.setLiked(isLiked);

        //格式化评论
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

    @Override
    public Result<List<ArticleDTO>> getMyArticle(String sortKey, String timeOrder) {
        Long id=UserContext.getCurrentUserId();

        List<ArticleDTO> articleDTOList=articleMapper.selectMyArticle(id,sortKey,timeOrder);

        if (!articleDTOList.isEmpty()){
            return Result.success(articleDTOList);
        }
        else {
            return Result.fail(404,"无数据");
        }
    }

    @Override
    @Transactional
    public Result<Boolean> delMyArticle(Long id) {
        Integer categoryId = this.getById(id).getCategoryId();
        int i = articleMapper.deleteById(id);

        if (i!=0){
            //删除缓存
            RBucket<ArticleDTO> bucketHot=redissonClient.getBucket(HOT_ARTICLE_KEY);
            RBucket<ArticleDTO> bucketDiscover=redissonClient.getBucket(DISCOVER_LIST_KEY_PREFIX+categoryId);
            if (bucketHot.isExists()){
                bucketHot.delete();
            }
            if (bucketDiscover.isExists()){
                bucketDiscover.delete();
            }

            //删除es
            articleSyncService.delete(id);

            return Result.success(true);
        }
        else {
            return Result.fail(404,"删除失败");
        }
    }

    @Override
    @Transactional
    public Result<Long> postArticle(Articles articles) {
        boolean b = this.saveOrUpdate(articles);
        Long id = articles.getId();
        Integer categoryId = articles.getCategoryId();

        if (b){
            //删除缓存
            RBucket<ArticleDTO> bucketHot=redissonClient.getBucket(HOT_ARTICLE_KEY);
            RBucket<ArticleDTO> bucketDiscover=redissonClient.getBucket(DISCOVER_LIST_KEY_PREFIX+categoryId);
            if (bucketHot.isExists()){
                bucketHot.delete();
            }
            if (bucketDiscover.isExists()){
                bucketDiscover.delete();
            }
            //更新es
            articleSyncService.syncOne(articles);

            return Result.success(id);
        }
        else {
            return Result.fail(500,"新增/修改文章失败");
        }
    }

    @Override
    public List<ArticleDTO> searchArticle(String keyword) {
        List<ArticleDocument> articleDocuments = searchFromEs(keyword);

        if (articleDocuments.isEmpty()){
            return searchFromDb(keyword);
        }

        List<Long> list = articleDocuments.stream().map(ArticleDocument::getId).toList();
        ArrayList<Long> ids=new ArrayList<>(list);
        List<ArticleDTO> articleDTOList = articleMapper.searchArticleList(ids);

        //创建一个 Map: ID -> ArticleDTO，方便快速查找
        Map<Long,ArticleDTO> map=articleDTOList.stream().collect(Collectors.toMap(ArticleDTO::getId, Function.identity()));

        List<ArticleDTO> resDto=new ArrayList<>();
        ids.forEach(i->{
            ArticleDTO articleDTO = map.get(i);
            if (articleDTO!=null){
                resDto.add(articleDTO);
            }
        });

        return resDto;

    }


    public List<ArticleDocument> searchFromEs(String keyword){
        try{
            SearchResponse<ArticleDocument> response=elasticsearchClient.search(s->
                s.index("articles_index").query(q->q.bool(b->{
                    b.should(sh->sh.matchPhrase(mp->mp
                            .field("title")
                            .query(keyword)
                            .boost(10.0f)));
                    b.should(sh->sh.multiMatch(mm->mm
                            .query(keyword)
                            .fields(
                                    "title^5",
                                    "title.pinyin^3",
                                    "content"
                            ).minimumShouldMatch("70%")
                    ));
                    b.minimumShouldMatch("1");

                    return b;
                }))
                        .sort(so->so.score(
                                sc->sc.order(SortOrder.Desc)
                        ))
            ,ArticleDocument.class);

            return response.hits().hits().stream().map(Hit::source).filter(Objects::nonNull).toList();

        } catch (Exception e){
            e.printStackTrace();
            return List.of();
        }

    }

    public List<ArticleDTO> searchFromDb(String keyword){
        return articleMapper.searchArticle(keyword);
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
