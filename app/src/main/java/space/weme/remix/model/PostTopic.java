package space.weme.remix.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Liujilong on 2016/1/27.
 * liujilong.me@gmail.com
 */
public class PostTopic implements Parcelable {

    @SerializedName("id")
    public String id;

    @SerializedName("imageurl")
    public String imageurl;

    @SerializedName("note")
    public String note;

    @SerializedName("number")
    public int number;

    @SerializedName("theme")
    public String theme;

    @SerializedName("slogan")
    public String slogan;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.imageurl);
        dest.writeString(this.note);
        dest.writeInt(this.number);
        dest.writeString(this.theme);
        dest.writeString(this.slogan);
    }

    public PostTopic() {
    }

    protected PostTopic(Parcel in) {
        this.id = in.readString();
        this.imageurl = in.readString();
        this.note = in.readString();
        this.number = in.readInt();
        this.theme = in.readString();
        this.slogan = in.readString();
    }

    public static final Creator<PostTopic> CREATOR = new Creator<PostTopic>() {
        @Override
        public PostTopic createFromParcel(Parcel source) {
            return new PostTopic(source);
        }

        @Override
        public PostTopic[] newArray(int size) {
            return new PostTopic[size];
        }
    };
}
