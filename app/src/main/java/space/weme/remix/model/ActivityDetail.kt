package space.weme.remix.model

import com.google.gson.annotations.SerializedName

/**
 * Created by just on 2016/2/12.
 */
class ActivityDetail {

    @SerializedName("id")
    var id: String? = ""

    @SerializedName("authorid")
    var authorid: String? = ""

    @SerializedName("school")
    var school: String? = ""

    @SerializedName("gender")
    var gender: String? = "男"

    @SerializedName("title")
    var title: String? = ""

    @SerializedName("time")
    var time: String? = ""

    @SerializedName("location")
    var location: String? = ""

    @SerializedName("number")
    var number: String? = ""

    @SerializedName("author")
    var author: String? = ""

    @SerializedName("signnumber")
    var signnumber: String? = ""

    @SerializedName("remark")
    var remark: String? = ""

    @SerializedName("state")
    var state: String? = ""

    @SerializedName("detail")
    var detail: String? = ""

    @SerializedName("advertise")
    var advertise: String? = ""

    @SerializedName("whetherimage")
    var whetherimage: String? = ""

    @SerializedName("likeflag")
    var flag: String? = ""

    @SerializedName("imageurl")
    var imageurl: String? = ""
}
