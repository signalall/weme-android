package space.weme.remix.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Liujilong on 16/2/15.
 * liujilong.me@gmail.com
 */
public class Food {

    @SerializedName("id")
    private String ID;

    @SerializedName("title")
    private String title;

    @SerializedName("authorid")
    private String authorId;

    @SerializedName("author")
    private String author;

    @SerializedName("location")
    private String location;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("price")
    private String price;

    @SerializedName("comment")
    private String comment;

    @SerializedName("likeNumber")
    private int likeNumber;

    @SerializedName("imageurl")
    private String url;

    @SerializedName("likeflag")
    private int likeFlag;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getLikeNumber() {
        return likeNumber;
    }

    public void setLikeNumber(int likeNumber) {
        this.likeNumber = likeNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLikeFlag() {
        return likeFlag;
    }

    public void setLikeFlag(int likeFlag) {
        this.likeFlag = likeFlag;
    }
}
