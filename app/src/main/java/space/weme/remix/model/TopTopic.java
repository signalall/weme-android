package space.weme.remix.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Joyce on 2017/1/15.
 */

public class TopTopic {
    @SerializedName("postid")
    private int id;

    @SerializedName("imageurl")
    private String url;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
