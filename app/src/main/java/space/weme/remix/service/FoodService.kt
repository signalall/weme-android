package space.weme.remix.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable
import space.weme.remix.Constants
import space.weme.remix.model.Food
import space.weme.remix.model.ResponseWrapper

/**
 * Created by Joyce on 2017/1/3.
 */

interface FoodService {

    data class GetRecommendFood(
            @SerializedName("token") val token: String
    )

    @POST(Constants.GET_RECOMMEND_FOOD)
    fun getRecommendFood(
            @Body token: GetRecommendFood
    ): Observable<ResponseWrapper<List<Food>>>

    data class LikeFood(
            @SerializedName("token") val token: String,
            @SerializedName("foodcardid") val foodCardId: String
    )

    data class LikeFoodResp(
            @SerializedName("state") val state: String,
            @SerializedName("reason") val reason: String,
            @SerializedName("likenumber") val likeNumber: Int
    )

    @POST(Constants.LIKE_FOOD_URL)
    fun likeFood(@Body token: LikeFood): Observable<LikeFoodResp>

    data class PublishCard(
            @SerializedName("token") val token: String,
            @SerializedName("title") val title: String,
            @SerializedName("comment") val comment: String,
            @SerializedName("location") val location: String,
            @SerializedName("latitude") val latitude: String,
            @SerializedName("longitude") val longitude: String,
            @SerializedName("price") val price: String
    )

    data class PublicCardResp(
            @SerializedName("state") val state: String,
            @SerializedName("reason") val reason: String,
            @SerializedName("id") val id: String
    )

    @POST(Constants.PUBLISH_CARD)
    fun publishCard(@Body body: PublishCard): Observable<PublicCardResp>
}