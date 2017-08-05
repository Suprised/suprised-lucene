package com.dascom.lucene.example.manager;

import com.cloverworxs.common.Pagination;
import com.dascom.lucene.example.bean.WeiboIndexBean;

public interface WeiboIndexManager {

    public void addIndex(WeiboIndexBean indexBean) ;
    
    public void deleteIndex(String id);
    
    public void updateIndex(WeiboIndexBean indexBean);
    
    public Pagination findByIndex(String condition, int currPage, int pageSize);
}
