package com.cc.qylgjavaservice.service.impl;

import com.cc.qylgjavaservice.dto.articleDTO.ArticleDocument;
import com.cc.qylgjavaservice.entity.Articles;
import com.cc.qylgjavaservice.mapper.ArticleMapper;
import com.cc.qylgjavaservice.search.ArticleEsRepository;
import com.cc.qylgjavaservice.service.ArticleSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleSyncServiceImpl implements ArticleSyncService {
    private final ArticleMapper articleMapper;
    private final ArticleEsRepository articleEsRepository;

    @Override
    public void syncAll() {
        List<Articles> articles = articleMapper.selectList(null);
        List<ArticleDocument> list = articles.stream().map(this::toDoc).toList();
        articleEsRepository.saveAll(list);
    }

    @Override
    public void syncOne(Articles articles) {
        articleEsRepository.save(toDoc(articles));
    }

    @Override
    public void delete(Long id) {
        articleEsRepository.deleteById(id);
    }

    @Override
    public ArticleDocument toDoc(Articles articles) {
        ArticleDocument articleDocument=new ArticleDocument();

        BeanUtils.copyProperties(articles,articleDocument);

        articleDocument.setSuggest(new Completion(new String[]{articles.getTitle()}));

        return articleDocument;
    }
}
