package com.dascom.lucene.example.manager;

import java.io.File;

import com.cloverworxs.common.Pagination;

public interface FileIndexManager {

    public void addIndex(File dir) ;
    
    public void deleteIndex(String id);
    
    public void updateIndex(File dir);
    
    public Pagination findByIndex(String condition, int currPage, int pageSize);
}
