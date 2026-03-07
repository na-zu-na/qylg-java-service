package com.cc.qylgjavaservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cc.qylgjavaservice.dto.ArticleDTO;
import com.cc.qylgjavaservice.dto.ArticleDetailDTO;
import com.cc.qylgjavaservice.dto.Result;
import com.cc.qylgjavaservice.entity.ArticleComments;
import com.cc.qylgjavaservice.entity.Articles;
import com.cc.qylgjavaservice.service.ArticleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.xml.stream.events.Comment;
import java.util.List;


@RestController
public class ArticleController {
    @Resource
    private ArticleService articleService;

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

    @GetMapping("/articles-detail")
    public Result<ArticleDetailDTO> getArticleDetail(@RequestParam int id){
        return articleService.getArticleDetail(id);
    }
}
