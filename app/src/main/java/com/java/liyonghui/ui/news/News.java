package com.java.liyonghui.ui.news;

import com.orm.SugarRecord;
import com.orm.dsl.Unique;

public class News extends SugarRecord {
    @Unique
    private String newsID;
    private String title;
    private String content;
    private String time;
    private String source;
    private boolean isRead;
    public News(){

    }
    public News(String newsID, String title, String content, String time, String source){
        this.newsID = newsID;
        this.title = title;
        this.content = content;
        this.time = time;
        this.source = source;
        this.isRead = false;
    }
    public  boolean getIsRead(){
        return isRead;
    }
    public  void setIsRead(boolean isRead){
        this.isRead = isRead;
    }
    public  String getNewsID(){
        return newsID;
    }
    public  void setNewsID(String newsID){
        this.newsID = newsID;
    }
    public  String getTime(){
        return time;
    }
    public  void setTime(String time){
        this.time = time;
    }
    public  String getTitle(){
        return title;
    }
    public  void setTitle(String title){
        this.title = title;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content = content;
    }
    public String getSource(){
        return source;
    }
    public void setSource(String content){
        this.source = source;
    }
}
