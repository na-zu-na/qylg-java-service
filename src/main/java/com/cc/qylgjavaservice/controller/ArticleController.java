package com.cc.qylgjavaservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.*;
import com.cc.qylgjavaservice.dto.articleDTO.*;
import com.cc.qylgjavaservice.entity.Articles;
import com.cc.qylgjavaservice.service.ArticleService;
import com.cc.qylgjavaservice.service.ArticleSyncService;
import com.cc.qylgjavaservice.service.impl.ArticleLikeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class ArticleController {
    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleLikeService articleLikeService;

    @Resource
    private ArticleSyncService articleSyncService;


    @GetMapping("/articles")
    public Result<Page<ArticleDTO>> getArticles(@RequestParam(value = "page",defaultValue = "1") int page ,
                                                @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        return articleService.getArticles(page,pageSize);
    }

    @GetMapping("/home/hot-article")
    public Result<List<ArticleDTO>> getHotArticle(){
        return articleService.getHotArticle();
    }

    @GetMapping("/discover/list")
    public Result<List<ArticleDTO>>  getDiscoverList(@RequestParam int type){
        return articleService.getDiscoverList(type);
    }

    @GetMapping("/api/articles-detail")
    public Result<ArticleDetailDTO> getArticleDetail(@RequestParam int id){
        return articleService.getArticleDetail(id);
    }

    @PostMapping("/api/articles/{id}/like")
    public Result<ArticleLikeDTO> likeArticle(
            @PathVariable Long id,
            @RequestBody LikeActionDTO likeActionDTO) {

        return articleLikeService.toggleLike(id, likeActionDTO.getUserId(), likeActionDTO.getAction());
    }

    @GetMapping("/api/my/articles")
    public Result<List<ArticleDTO>> getMyArticle(@RequestParam("sortKey") String sortKey,
                                                 @RequestParam(value = "timeOrder",defaultValue = "desc") String timeOrder){
        return articleService.getMyArticle(sortKey,timeOrder);
    }

    @DeleteMapping("/api/my/del")
    public Result<Boolean> delMyArticle(@RequestParam("id") Long id){
        return articleService.delMyArticle(id);
    }

    @PostMapping("/api/article/post")
    public Result<Long> postArticle(@RequestBody Articles articles){
        return articleService.postArticle(articles);
    }

    @GetMapping("/article/search/init")
    public void articleSearchInit(){
        articleSyncService.syncAll();
    }

    @GetMapping("/article/search")
    public List<ArticleDTO> searchArticle(@RequestParam(value = "keyword") String keyword){
        return articleService.searchArticle(keyword);
    }

    @GetMapping("/admin/articles")
    public Result<ArticleAdminDTO> getAdminArticles(@RequestParam(value = "page",defaultValue = "1") int page ,
                                                    @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                                    @RequestParam(required = false) Integer status,
                                                    @RequestParam(required = false) Integer category_id){
        return articleService.getAdminArticles(page,pageSize,status,category_id);
    }

    @PutMapping("/admin/articles/{articleId}/status")
    public Result<Void> updateArticleStatus(@PathVariable Long articleId,
                                            @RequestParam Integer status) {
        return articleService.updateArticleStatus(articleId, status);
    }
}
