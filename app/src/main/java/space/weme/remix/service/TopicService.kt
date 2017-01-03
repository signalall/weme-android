package space.weme.remix.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable
import space.weme.remix.Constants
import space.weme.remix.model.PostTopic

interface TopicService {

    data class GetTopicInfo(
            @SerializedName("token") val token: String,
            @SerializedName("topicid") val topicId: String
    )

    @POST(Constants.GET_TOPIC_INFO)
    fun getTopicInfo(
            @Body body: GetTopicInfo
    ): Observable<ResponseWrapper<PostTopic>>

    data class GetTopicList(
            @SerializedName("token") val token: String
    )

    @POST(Constants.GET_TOPIC_LIST)
    fun getTopList(
            @Body body: GetTopicList
    ): Observable<ResponseWrapper<List<PostTopic>>>
}