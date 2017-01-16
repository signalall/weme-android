package space.weme.remix.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable
import space.weme.remix.Constants
import space.weme.remix.model.*

/**
 * Created by Joyce on 2017/1/3.
 */
interface ActivityService {

    data class GetActivity(
            @SerializedName("token") val token: String,
            @SerializedName("page") val page: String
    )

    @POST(Constants.GET_PUBLISH_ACTIVITY)
    fun getPublishActivity(
            @Body token: GetActivity
    ): Observable<ResponseWrapper<List<Activity>>>

    @POST(Constants.GET_LIKE_ACTIVITY)
    fun getLikeActivity(
            @Body token: GetActivity
    ): Observable<ResponseWrapper<List<Activity>>>

    @POST(Constants.GET_REGISTER_ACTIVITY)
    fun getRegisterActivity(
            @Body token: GetActivity
    ): Observable<ResponseWrapper<List<Activity>>>

    data class SearchActivity(
            @SerializedName("token") val token: String,
            @SerializedName("text") val text: String
    )

    @POST(Constants.SEARCH_ACTIVITY)
    fun searchActivity(
            @Body token: SearchActivity
    ): Observable<ResponseWrapper<List<Activity>>>


    data class GetActivityInfo(
            @SerializedName("token") val token: String,
            @SerializedName("page") val page: String
    )

    // Todo: Refactor backend interface
    class GetActivityInfoResp {

        @SerializedName("state")
        var state: String? = ""

        @SerializedName("reason")
        var reason: String? = ""

        @SerializedName("pages")
        var pages: Int = 0

        @SerializedName("result")
        var result: List<Activity>? = null
    }

    @POST(Constants.GET_ACTIVITY_INFO_URL)
    fun getActivityInfo(
            @Body token: GetActivityInfo
    ): Observable<GetActivityInfoResp>


    data class GetActivityDetail(
            @SerializedName("token") val token: String,
            @SerializedName("activityid") val activityId: String
    )

    @POST(Constants.GET_ACTIVITY_DETAIL_URL)
    fun getActivityDetail(
            @Body token: GetActivityDetail
    ): Observable<ResponseWrapper<ActivityDetail>>

    data class PublishActivity(
            @SerializedName("token") val token: String,
            @SerializedName("title") val title: String,
            @SerializedName("location") val location: String,
            @SerializedName("number") val number: String,
            @SerializedName("time") val time: String,
            @SerializedName("advertise") val advertise: String,
            @SerializedName("whetherimage") val whetherimage: String,
            @SerializedName("detail") val detail: String,
            @SerializedName("labe") val labe: String
    )

    data class PublishActivityResp(
            @SerializedName("id") val id: Int, // Activity id
            @SerializedName("reason") val reason: String,
            @SerializedName("state") val state: String
    )

    @POST(Constants.PUBLISH_ACTIVITY)
    fun publishActivity(
            @Body token: PublishActivity
    ): Observable<PublishActivityResp>

    // Todo: Refactor backend interface
    data class SignActivity(
            @SerializedName("token") val token: String,
            @SerializedName("activityid") val activityid: String,
            @SerializedName("activity") val activity: String
    )

    @POST(Constants.SIGN_ACTIVITY)
    fun signActivity(
            @Body token: SignActivity
    ): Observable<Any>

    // Todo: Refactor backend interface
    data class DelSignActivity(
            @SerializedName("token") val token: String,
            @SerializedName("activityid") val activityid: String,
            @SerializedName("activity") val activity: String
    )

    @POST(Constants.DEL_SIGN_ACTIVITY)
    fun delSignActivity(
            @Body token: DelSignActivity
    ): Observable<Any>

    // Todo: Refactor backend interface
    data class LikeActivity(
            @SerializedName("token") val token: String,
            @SerializedName("activityid") val activityid: String,
            @SerializedName("activity") val activity: String
    )

    @POST(Constants.LIKE_ACTIVITY)
    fun likeActivity(
            @Body token: LikeActivity
    ): Observable<Any>

    data class UnlikeActivity(
            @SerializedName("token") val token: String,
            @SerializedName("activityid") val activityid: String,
            @SerializedName("activity") val activity: String
    )

    @POST(Constants.UNLIKE_ACTIVITY)
    fun unlikeActivity(@Body body: UnlikeActivity): Observable<Any>

    data class GetTopActivity(
            @SerializedName("token") val token: String
    )

    @POST(Constants.GET_TOP_ACTIVITY_URL)
    fun getTopActivity(@Body body: GetTopActivity): Observable<ResponseWrapper<List<TopActivity>>>

    data class GetActivityCommennt(
            @SerializedName("token") val token: String,
            @SerializedName("activityid") val activityid: String,
            @SerializedName("endid") val pageSize: String
    )

    @POST(Constants.GET_ACTIVITY_COMMENT_URL)
    fun getActivityComment(@Body body: GetActivityCommennt): Observable<ResponseWrapper<List<ActivityComment>>>

    data class CommentToActivity(
            @SerializedName("token") val token: String,
            @SerializedName("activityid") val activityid: String,
            @SerializedName("body") val body: String
    )

    data class CommentToActivityResp(
            @SerializedName("id") val id: Int, // Activity id
            @SerializedName("reason") val reason: String,
            @SerializedName("state") val state: String,
            @SerializedName("commentnumber") val commentNumber: String
    )

    @POST(Constants.COMMENT_TO_ACTIVITY_URL)
    fun commentToActivity(@Body body: CommentToActivity): Observable<CommentToActivityResp>
}