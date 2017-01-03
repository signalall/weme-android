package space.weme.remix.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Liujilong on 2016/1/28.
 * liujilong.me@gmail.com
 */
public class Post {

    @SerializedName("postid")
    private String postId;

    @SerializedName("userid")
    private String userId;

    @SerializedName("name")
    private String name;

    @SerializedName("school")
    private String school;

    @SerializedName("gender")
    private String gender;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("likenumber")
    public String likeNumber;

    @SerializedName("commentnumber")
    public String commentNumber;

    @SerializedName("imageurl")
    public List<String> imageUrl;

    @SerializedName("thumbnail")
    public List<String> thumbnailUrl;

    @SerializedName("likeusers")
    public List<Integer> likeUserIds;

    @SerializedName("flag")
    public String flag; // whether current user has liked this post

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLikeNumber() {
        return likeNumber;
    }

    public void setLikeNumber(String likeNumber) {
        this.likeNumber = likeNumber;
    }

    public String getCommentNumber() {
        return commentNumber;
    }

    public void setCommentNumber(String commentNumber) {
        this.commentNumber = commentNumber;
    }

    public List<String> getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(List<String> imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(List<String> thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public List<Integer> getLikeUserIds() {
        return likeUserIds;
    }

    public void setLikeUserIds(List<Integer> likeUserIds) {
        this.likeUserIds = likeUserIds;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
