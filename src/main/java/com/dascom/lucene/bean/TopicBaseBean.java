/*
 *
 * Copyright CloverWorxs Inc.
 */
package com.dascom.lucene.bean;

import java.util.Date;

public class TopicBaseBean {
    
    private String key;

    private String creator;

    private int topicSize;

    private String lastReplyer;

    private Date lastReplyDate;

    private String forumKey;

    private String parentKey;

    private String parentTitle;

    private String content;

    private boolean audit;

    private int count;

    private TopicBaseBean topTopic;

    private float grade;

    private String userLoginName;

    private String forumName;

    private String privateKey;

    private boolean shared;

    private String questionAcademyTitle;// 用户的机构

    private String answerAcademyTitle;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getContent() {
        return this.content;
    }

    /**
     * 
     * @param content
     */
    public void setContent(String content) {
        this.content = content;
    }

    public void setAudit(boolean audit) {
        this.audit = audit;
    }

    public String getParentKey() {
        return this.parentKey;
    }

    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }

    public String getForumKey() {
        return this.forumKey;
    }

    public void setForumKey(String forumKey) {
        this.forumKey = forumKey;
    }

    public String getLastReplyer() {
        return this.lastReplyer;
    }

    public void setLastReplyer(String lastReplyer) {
        this.lastReplyer = lastReplyer;
    }

    public String getLastReplyTime() {
        return lastReplyDate.toString();
    }

    public int getTopicSize() {
        return this.topicSize;
    }

    public void setTopicSize(int size) {
        this.topicSize = size;
    }

    /**
     * default constructor
     */
    public TopicBaseBean() {
        super();
    }


    /**
     * 
     * @param key
     * @param creator
     * @param title
     */
    public TopicBaseBean(String key, String creator, String title) {
        this.creator = creator;
    }

    public TopicBaseBean(String string, String string2, String string3, String string4, Date date) {
        this.key = string;
        this.creator = string2;
        this.parentTitle = string3;
        this.content = string4;
        this.lastReplyDate = date;
        this.topTopic = this;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public boolean isAudit() {
        return audit;
    }

    public TopicBaseBean getTopTopic() {
        return topTopic;
    }

    public void setTopTopic(TopicBaseBean topTopic) {
        this.topTopic = topTopic;
    }

    public Date getLastReplyDate() {
        return lastReplyDate;
    }

    public void setLastReplyDate(Date lastReplyDate) {
        this.lastReplyDate = lastReplyDate;
    }

    public float getGrade() {
        return grade;
    }

    public void setGrade(float grade) {
        this.grade = grade;
    }

    public String getUserLoginName() {
        return userLoginName;
    }

    public void setUserLoginName(String userLoginName) {
        this.userLoginName = userLoginName;
    }

    /**
     * @return the parentTitle
     */
    public String getParentTitle() {
        return parentTitle;
    }

    /**
     * @param parentTitle the parentTitle to set
     */
    public void setParentTitle(String parentTitle) {
        this.parentTitle = parentTitle;
    }

    /**
     * @return the forumName
     */
    public String getForumName() {
        return forumName;
    }

    /**
     * @param forumName the forumName to set
     */
    public void setForumName(String forumName) {
        this.forumName = forumName;
    }

    public String getQuestionAcademyTitle() {
        return questionAcademyTitle;
    }

    public void setQuestionAcademyTitle(String questionAcademyTitle) {
        this.questionAcademyTitle = questionAcademyTitle;
    }

    public String getAnswerAcademyTitle() {
        return answerAcademyTitle;
    }

    public void setAnswerAcademyTitle(String answerAcademyTitle) {
        this.answerAcademyTitle = answerAcademyTitle;
    }

}