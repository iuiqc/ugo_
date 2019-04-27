package com.ugo.search.controller;

import com.ugo.common.pojo.PageResult;
import com.ugo.search.pojo.Goods;
import com.ugo.search.pojo.SearchRequest;
import com.ugo.search.pojo.SearchResult;
import com.ugo.search.service.IndexService;
import com.ugo.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: qc
 * @Date: 2019-4-26 18:23
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;
    
    @PostMapping("page")
    public ResponseEntity<SearchResult> queryGoodsByPage(@RequestBody SearchRequest searchRequest){
        SearchResult goodsPageResult = searchService.search(searchRequest);
        if (goodsPageResult == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(goodsPageResult);
    }
}
