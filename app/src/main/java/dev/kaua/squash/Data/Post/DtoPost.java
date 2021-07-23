package dev.kaua.squash.Data.Post;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import dev.kaua.squash.Security.EncryptHelper;

public class DtoPost implements Comparable<DtoPost>{

    @SerializedName("Posts_Search")
    private ArrayList<Posts_Search>  posts;

    public ArrayList<Posts_Search> getPosts(){
        return posts;
    }

    private String post_id;

    private String account_id;

    private String account_id_cry;

    private String name_user;

    private String username;

    private String profile_image;

    private String post_date;

    private String post_time;

    private String post_content;

    private List<String> post_images;

    private String post_likes;

    private String post_comments_amount;

    private String post_topic;

    private String verification_level;

    public String getVerification_level() {
        return verification_level;
    }

    public void setVerification_level(String verification_level) {
        this.verification_level = verification_level;
    }

    public String getAccount_id_cry() {
        return account_id_cry;
    }

    public void setAccount_id_cry(String account_id_cry) {
        this.account_id_cry = account_id_cry;
    }

    public String getName_user() {
        return name_user;
    }

    public void setName_user(String name_user) {
        this.name_user = name_user;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getPost_date() {
        return post_date;
    }

    public void setPost_date(String post_date) {
        this.post_date = post_date;
    }

    public String getPost_time() {
        return post_time;
    }

    public void setPost_time(String post_time) {
        this.post_time = post_time;
    }

    public String getPost_content() {
        return post_content;
    }

    public void setPost_content(String post_content) {
        this.post_content = post_content;
    }

    public List<String> getPost_images() {
        return post_images;
    }

    public void setPost_images(List<String> post_images) {
        this.post_images = post_images;
    }

    public String getPost_likes() {
        return post_likes;
    }

    public void setPost_likes(String post_likes) {
        this.post_likes = post_likes;
    }

    public String getPost_comments_amount() {
        return post_comments_amount;
    }

    public void setPost_comments_amount(String post_comments_amount) {
        this.post_comments_amount = post_comments_amount;
    }

    public String getPost_topic() {
        return post_topic;
    }

    public void setPost_topic(String post_topic) {
        this.post_topic = post_topic;
    }

    @Override
    public int compareTo(DtoPost o) {
        return getPost_id().compareTo(o.getPost_id());
    }

    public class Posts_Search {

        @SerializedName("post_id")
        @Expose
        private String post_id;

        @SerializedName("account_id")
        @Expose
        private String account_id;

        @SerializedName("name_user")
        @Expose
        private String name_user;

        @SerializedName("username")
        @Expose
        private String username;

        @SerializedName("profile_image")
        @Expose
        private String profile_image;

        @SerializedName("post_date")
        @Expose
        private String post_date;

        @SerializedName("post_time")
        @Expose
        private String post_time;

        @SerializedName("post_content")
        @Expose
        private String post_content;

        @SerializedName("post_images")
        @Expose
        private List<String> post_images;

        @SerializedName("post_likes")
        @Expose
        private String post_likes;

        @SerializedName("post_comments_amount")
        @Expose
        private String post_comments_amount;

        @SerializedName("post_topic")
        @Expose
        private String post_topic;

        @SerializedName("verification_level")
        @Expose
        private String verification_level;

        public String getVerification_level() {
            return verification_level;
        }

        public void setVerification_level(String verification_level) {
            this.verification_level = verification_level;
        }

        public String getPost_id() {
            return post_id;
        }

        public void setPost_id(String post_id) {
            this.post_id = post_id;
        }

        public String getName_user() {
            return name_user;
        }

        public void setName_user(String name_user) {
            this.name_user = name_user;
        }

        public String getAccount_id() {
            return account_id;
        }

        public void setAccount_id(String account_id) {
            this.account_id = account_id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getProfile_image() {
            return profile_image;
        }

        public void setProfile_image(String profile_image) {
            this.profile_image = profile_image;
        }

        public String getPost_date() {
            return post_date;
        }

        public void setPost_date(String post_date) {
            this.post_date = post_date;
        }

        public String getPost_time() {
            return post_time;
        }

        public void setPost_time(String post_time) {
            this.post_time = post_time;
        }

        public String getPost_content() {
            return post_content;
        }

        public void setPost_content(String post_content) {
            this.post_content = post_content;
        }

        public List<String> getPost_images() {
            return post_images;
        }

        public void setPost_images(List<String> post_images) {
            this.post_images = post_images;
        }

        public String getPost_likes() {
            return post_likes;
        }

        public void setPost_likes(String post_likes) {
            this.post_likes = post_likes;
        }

        public String getPost_comments_amount() {
            return post_comments_amount;
        }

        public void setPost_comments_amount(String post_comments_amount) {
            this.post_comments_amount = post_comments_amount;
        }

        public String getPost_topic() {
            return post_topic;
        }

        public void setPost_topic(String post_topic) {
            this.post_topic = post_topic;
        }
    }

}
