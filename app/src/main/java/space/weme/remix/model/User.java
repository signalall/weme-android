package space.weme.remix.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Liujilong on 2016/1/28.
 * liujilong.me@gmail.com
 */
public class User {
    @SerializedName("birthday")
    private String birthday;

    @SerializedName("degree")
    private String degree;

    @SerializedName("enrollment")
    private String enrollment;

    @SerializedName("hobby")
    private String hobby;

    @SerializedName("id")
    private int id;

    @SerializedName("phone")
    private String phone;

    @SerializedName("preference")
    private String preference;

    @SerializedName("qq")
    private String qq;

    @SerializedName("wechat")
    private String wechat;

    @SerializedName("username")
    private String username;

    @SerializedName("name")
    private String name;

    @SerializedName("school")
    private String school;

    @SerializedName("department")
    private String department;

    @SerializedName("gender")
    private String gender;

    @SerializedName("hometown")
    private String hometown;

    @SerializedName("lookcount")
    private String lookcount;

    @SerializedName("weme")
    private String weme;

    @SerializedName("constellation")
    private String constellation;

    @SerializedName("voice")
    private String voiceUrl;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("match")
    private String match; // "0" "1"

    public String getBirthday() {
        return birthday;
    }

    public String getDegree() {
        return degree;
    }

    public String getEnrollment() {
        return enrollment;
    }

    public String getHobby() {
        return hobby;
    }

    public int getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getPreference() {
        return preference;
    }

    public String getQq() {
        return qq;
    }

    public String getWechat() {
        return wechat;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getSchool() {
        return school;
    }

    public String getDepartment() {
        return department;
    }

    public String getGender() {
        return gender;
    }

    public String getHometown() {
        return hometown;
    }

    public String getLookcount() {
        return lookcount;
    }

    public String getWeme() {
        return weme;
    }

    public String getConstellation() {
        return constellation;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getMatch() {
        return match;
    }
}
