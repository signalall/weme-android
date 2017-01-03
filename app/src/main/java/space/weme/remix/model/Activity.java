package space.weme.remix.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Liujilong on 16/1/27.
 * liujilong.me@gmail.com
 */
public class Activity {

    @SerializedName("id")
    private int id;

    @SerializedName("time")
    private String time;

    @SerializedName("location")
    private String location;

    @SerializedName("title")
    private String title;

    @SerializedName("number")
    private String capacity;

    @SerializedName("state")
    private String state; // yes

    @SerializedName("signnumber")
    private String signNumber;

    @SerializedName("remark")
    private String remark;

    @SerializedName("author")
    private String author;

    @SerializedName("detail")
    private String detail;

    @SerializedName("advertise")
    private String advertise;

    @SerializedName("needsImage")
    private boolean needsImage;

    @SerializedName("likeFlag")
    private boolean likeFlag;

    @SerializedName("authorid")
    private int authorID;

    @SerializedName("school")
    private String school;

    @SerializedName("poster")
    private String poster;

    @SerializedName("status")
    private String status;

    @SerializedName("timestate")
    private String timeState;

    @SerializedName("sponsor")
    private String sponsor;

    @SerializedName("top")
    private String top;

    @SerializedName("gender")
    private String gender;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSignNumber() {
        return signNumber;
    }

    public void setSignNumber(String signNumber) {
        this.signNumber = signNumber;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public boolean isNeedsImage() {
        return needsImage;
    }

    public void setNeedsImage(boolean needsImage) {
        this.needsImage = needsImage;
    }

    public boolean isLikeFlag() {
        return likeFlag;
    }

    public void setLikeFlag(boolean likeFlag) {
        this.likeFlag = likeFlag;
    }

    public int getAuthorID() {
        return authorID;
    }

    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeState() {
        return timeState;
    }

    public void setTimeState(String timeState) {
        this.timeState = timeState;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Activity{");
        sb.append("id=").append(id);
        sb.append(", time='").append(time).append('\'');
        sb.append(", location='").append(location).append('\'');
        sb.append(", title='").append(title).append('\'');
        sb.append(", capacity='").append(capacity).append('\'');
        sb.append(", state='").append(state).append('\'');
        sb.append(", signNumber='").append(signNumber).append('\'');
        sb.append(", remark='").append(remark).append('\'');
        sb.append(", author='").append(author).append('\'');
        sb.append(", detail='").append(detail).append('\'');
        sb.append(", advertise='").append(advertise).append('\'');
        sb.append(", needsImage=").append(needsImage);
        sb.append(", likeFlag=").append(likeFlag);
        sb.append(", authorID=").append(authorID);
        sb.append(", school='").append(school).append('\'');
        sb.append(", poster='").append(poster).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", timeState='").append(timeState).append('\'');
        sb.append(", sponsor='").append(sponsor).append('\'');
        sb.append(", top='").append(top).append('\'');
        sb.append(", gender='").append(gender).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
