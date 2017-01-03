package space.weme.remix.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import rx.Observable
import space.weme.remix.Constants
import space.weme.remix.model.Food

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

    @POST(Constants.GET_RECOMMEND_FOOD)
    fun likeFood(
            @Body token: LikeFood
    ): Observable<ResponseWrapper<Any>>
}