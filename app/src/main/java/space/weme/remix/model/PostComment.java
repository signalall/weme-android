package space.weme.remix.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Liujilong on 2016/1/30.
 * liujilong.me@gmail.com
 */
public class PostComment {

    @SerializedName("body")
    private String content;

    @SerializedName("flag")
    private String flag; // whether you liked this reply

    @SerializedName("gender")
    private String gender;

    @SerializedName("id")
    private String id;

    @SerializedName("image")
    private List<String> image;

    @SerializedName("commentNumber")
    private int commentCount;

    @SerializedName("likeNumber")
    private int likeCount;

    @SerializedName("name")
    private String name;

    @SerializedName("reply")
    private List<Comment> comments;

    @SerializedName("school")
    private String school;

    @SerializedName("thumbnail")
    private List<String> thumbnail;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("userid")
    private String userId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getImage() {
        return image;
    }

    public void setImage(List<String> image) {
        this.image = image;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public List<String> getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(List<String> thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PostComment{");
        sb.append("content='").append(content).append('\'');
        sb.append(", flag='").append(flag).append('\'');
        sb.append(", gender='").append(gender).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append(", image=").append(image);
        sb.append(", commentCount=").append(commentCount);
        sb.append(", likeCount=").append(likeCount);
        sb.append(", name='").append(name).append('\'');
        sb.append(", comments=").append(comments);
        sb.append(", school='").append(school).append('\'');
        sb.append(", thumbnail=").append(thumbnail);
        sb.append(", timestamp='").append(timestamp).append('\'');
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
