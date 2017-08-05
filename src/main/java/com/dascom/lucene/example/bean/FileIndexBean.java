package com.dascom.lucene.example.bean;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 文件索引bean
 */
public class FileIndexBean {
    
    public static final String CONTENTS = "content";
    public static final String SUMMARY = "summary";//摘要
    public static final String TITLE = "title";
    public static final String MODIFYED = "modified";
    public static final String PATH = "path";
    
    private String path;
    
    private String title;
    
    private long lastModified; //最后修改时间
    
    private Date lastModifiedDate;
    
    private Date createDate;//创建索引日期
    
    private String fileDir; //要索引的文件目录
    
    private String summary;
    
    private String content;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getFileDir() {
        return fileDir;
    }

    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
