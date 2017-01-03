package space.weme.remix.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Liujilong on 2016/1/30.
 * liujilong.me@gmail.com
 */
public class Comment {
    @SerializedName("authorid")
    private String authorid;

    @SerializedName("body")
    private String content; // content of this commitRely

    @SerializedName("destcommentid")
    private String toCommentId;

    @SerializedName("destname")
    private String toUsername;

    @SerializedName("destuserid")
    private String toUserId;

    @SerializedName("id")
    private String fromUserId; // author's id of this commitReply

    @SerializedName("name")
    private String fromUsername; // author's name of this commitReply

    @SerializedName("timestamp")
    private String timestamp;

    public String getAuthorid() {
        return authorid;
    }

    public void setAuthorid(String authorid) {
        this.authorid = authorid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getToCommentId() {
        return toCommentId;
    }

    public void setToCommentId(String toCommentId) {
        this.toCommentId = toCommentId;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
