package com.example.dsm2018.firebasechatexam.model;

public class ChatMessage {
    private String id;
    private String text;
    private String name;
    private String photoUrl;
    private String uid;


    public ChatMessage(){}

    public ChatMessage(String text, String name, String photoUrl, String uid){
        this.text = text;
        this.name = name;
        this.photoUrl = photoUrl;
        this.uid=uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
