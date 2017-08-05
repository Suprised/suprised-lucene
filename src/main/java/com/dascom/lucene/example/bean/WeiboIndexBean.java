package com.dascom.lucene.example.bean;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 微博的索引bean
 */
public class WeiboIndexBean {
    
    public static final String WB_ID = "wb_id";
    public static final String WB_CONTENT = "wb_content";
    public static final String WB_SUMMARY = "wb_summary";
    public static final String WB_PUBLISH_DATE = "wb_publishDate";

    private String id;
    
    private String content;

    private String summary;

    private Date publishDate;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
