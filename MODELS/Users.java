package com.release.rsa_20.MODELS;

public class Users {
    @Override
    public String toString() {
        return "Users{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", pub_key='" + pub_key + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    private String id;
    private String name;
    private String pub_key;
    private String status;
    // Constructors;
    public Users() {
    }

    public Users(String id, String name, String pub_key, String status) {
        this.id = id;
        this.name = name;
        this.pub_key = pub_key;
        this.status = status;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPub_key() {
        return pub_key;
    }

    public void setPub_key(String pub_key) {
        this.pub_key = pub_key;
    }
}
