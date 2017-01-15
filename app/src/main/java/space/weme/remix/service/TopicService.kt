package space.weme.remix.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable
import space.weme.remix.Constants
import space.weme.remix.model.ResponseWrapper
import space.weme.remix.model.TopTopic
import space.weme.remix.model.Topic

interface TopicService {

    data class GetTopicInfo(@SerializedName("token") val token: String, @SerializedName("topicid") val topicId: String
    )

    @POST(Constants.GET_TOPIC_INFO)
    fun getTopicInfo(@Body body: GetTopicInfo): Observable<ResponseWrapper<Topic>>

    data class GetTopicList(
            @SerializedName("token") val token: String
    )

    @POST(Constants.GET_TOPIC_LIST)
    fun getTopicList(@Body body: GetTopicList): Observable<ResponseWrapper<List<Topic>>>

    data class GetTopTopicList(
            @SerializedName("token") val token: String
    )

    @POST(Constants.TOP_BROAD_URL)
    fun getTopTopicList(@Body body: GetTopTopicList): Observable<ResponseWrapper<List<TopTopic>>>
}