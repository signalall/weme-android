package space.weme.remix.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by just on 2016/2/12.
 */
public class AtyDetail {

    @SerializedName("id")
    private String id;

    @SerializedName("authorid")
    private String authorid;

    @SerializedName("school")
    private String school;

    @SerializedName("gender")
    public String gender;

    @SerializedName("title")
    private String title;

    @SerializedName("time")
    private String time;

    @SerializedName("location")
    private String location;

    @SerializedName("number")
    private String number;

    @SerializedName("author")
    private String author;

    @SerializedName("signnumber")
    private String signnumber;

    @SerializedName("remark")
    private String remark;

    @SerializedName("state")
    private String state;

    @SerializedName("detail")
    private String detail;

    @SerializedName("advertise")
    private String advertise;

    @SerializedName("whetherimage")
    private String whetherimage;

    @SerializedName("likeflag")
    private String likeflag;

    @SerializedName("imageurl")
    private String imageurl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorid() {
        return authorid;
    }

    public void setAuthorid(String authorid) {
        this.authorid = authorid;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSignnumber() {
        return signnumber;
    }

    public void setSignnumber(String signnumber) {
        this.signnumber = signnumber;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getAdvertise() {
        return advertise;
    }

    public void setAdvertise(String advertise) {
        this.advertise = advertise;
    }

    public String getWhetherimage() {
        return whetherimage;
    }

    public void setWhetherimage(String whetherimage) {
        this.whetherimage = whetherimage;
    }

    public String getLikeflag() {
        return likeflag;
    }

    public void setLikeflag(String likeflag) {
        this.likeflag = likeflag;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
