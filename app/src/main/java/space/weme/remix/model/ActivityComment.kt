package space.weme.remix.model

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Created by Joyce on 2017/1/16.
 */

class ActivityComment {

    /**
     * 评论的内容
     */
    @SerializedName("body")
    var body: String? = ""

    /**
     * 回复该评论的评论个数
     */
    @SerializedName("commentnumber")
    var commentNumber: Int = 0

    /**
     * 评论者的星座
     */
    @SerializedName("constelleation")
    var constelleation: String = ""

    /**
     *  用户时候对该评论进行了点赞： 0 没有， 1 有
     */
    @SerializedName("flag")
    var flag: String? = ""

    /**
     * 评论者的星座
     */
    @SerializedName("gender")
    var gender: String? = ""

    /**
     * '该评论在数据库中的 id
     */
    @SerializedName("id")
    var id: Int = 0

    /**
     * 该评论附带的图片 URL
     */
    @SerializedName("image")
    var image: List<String> = ArrayList()

    /**
     * 该评论所获得的点赞的个数
     */
    @SerializedName("likenumber")
    var likeNumber: String? = ""

    /**
     * 评论者在 WEME 中的昵称
     */
    @SerializedName("name")
    var name: String? = ""

    /**
     * 回复该评论的评论 id 集合
     */
    @SerializedName("reply")
    var reply: List<String> = ArrayList()

    /**
     * 评论者在 WEME 中所填写学校信息
     */
    @SerializedName("school")
    var school: String? = ""

    /**
     * 该评论附带的图片的小图标 URL
     */
    @SerializedName("thumbnail")
    var thumbnail: List<String> = ArrayList()

    /**
     * 该评论发布的时间
     */
    @SerializedName("timestamp")
    var timestamp: String = ""

    /**
     * 评论者在 WEME 中的 WeME 中的 id
     */
    @SerializedName("userid")
    var userId: Int = 0
}