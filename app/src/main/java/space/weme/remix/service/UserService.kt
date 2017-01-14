package space.weme.remix.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable
import space.weme.remix.Constants
import space.weme.remix.model.ResponseWrapper
import space.weme.remix.model.User

/**
 * Created by Joyce on 2017/1/5.
 */

interface UserService {
    data class SendSmsCode(
            @SerializedName("phone") val phone: String,
            @SerializedName("type") val type: String
    )

    @POST(Constants.SEND_SMS_CODE)
    fun sendSmsCode(@Body sendSmsCode: SendSmsCode): Observable<ResponseWrapper<Any>>

    data class RegisterPhone(
            @SerializedName("phone") val phone: String,
            @SerializedName("password") val password: String,
            @SerializedName("code") val code: String
    )

    data class RegisterPhoneResp(
            @SerializedName("state") val state: String,
            @SerializedName("reason") val reason: String,
            @SerializedName("id") val userId: String,
            @SerializedName("token") val token: String
    )

    @POST(Constants.REGISTER_PHONE)
    fun registerPhone(@Body registerPhone: RegisterPhone): Observable<RegisterPhoneResp>

    data class GetProfileByUserId(
            @SerializedName("token") val token: String,
            @SerializedName("id") val userId: String
    )

    data class GetProfile(
            @SerializedName("token") val token: String
    )

    class GetProfileByUserIdResp {
        @SerializedName("birthday")
        var birthday: String? = null

        @SerializedName("degree")
        var degree: String? = null

        @SerializedName("enrollment")
        var enrollment: String? = null

        @SerializedName("hobby")
        var hobby: String? = null

        @SerializedName("id")
        var id: Int = 0

        @SerializedName("phone")
        var phone: String? = null

        @SerializedName("preference")
        var preference: String? = null

        @SerializedName("qq")
        var qq: String? = null

        @SerializedName("wechat")
        var wechat: String? = null

        @SerializedName("username")
        var username: String? = null

        @SerializedName("name")
        var name: String? = null

        @SerializedName("school")
        var school: String? = null

        @SerializedName("department")
        var department: String? = null

        @SerializedName("gender")
        var gender: String? = null

        @SerializedName("hometown")
        var hometown: String? = null

        @SerializedName("lookcount")
        var lookcount: String? = null

        @SerializedName("weme")
        var weme: String? = null

        @SerializedName("constellation")
        var constellation: String? = null

        @SerializedName("voice")
        var voiceUrl: String? = null

        @SerializedName("avatar")
        var avatar: String? = null

        @SerializedName("match")
        var match: String? = null // "0" "1"

        @SerializedName("birthflag")
        var birthFlag: Int = 0

        @SerializedName("followflag")
        var followFlag: Int = 0

        @SerializedName("state")
        var state: String = ""

        @SerializedName("reason")
        var reason: String = ""
    }

    @POST(Constants.GET_PROFILE_BY_ID)
    fun getProfileByUserId(@Body body: GetProfileByUserId): Observable<GetProfileByUserIdResp> // Todo:

    @POST(Constants.GET_PROFILE)
    fun getProfile(@Body body: GetProfile): Observable<User> // Todo:

    data class EditProfile(
            @SerializedName("token") val token: String,
            @SerializedName("name") val nickName: String,
            @SerializedName("birthday") val birthday: String,
            @SerializedName("degree") val degree: String,
            @SerializedName("department") val department: String,
            @SerializedName("gender") val gender: String,
            @SerializedName("hometown") val hometown: String,
            @SerializedName("phone") val phone: String,
            @SerializedName("qq") val qq: String,
            @SerializedName("school") val school: String,
            @SerializedName("wechat") val wechat: String
    )

    @POST(Constants.EDIT_PROFILE_URL)
    fun editProfile(@Body body: EditProfile): Observable<ResponseWrapper<Any>>

    data class Login(
            @SerializedName("username") val username: String,
            @SerializedName("password") val password: String
    )

    data class LoginResp(
            @SerializedName("state") val state: String,
            @SerializedName("reason") val reason: String,
            @SerializedName("token") val token: String,
            @SerializedName("id") val id: String,
            @SerializedName("gender") val gender: String
    )

    @POST(Constants.LOGIN_URL)
    fun login(@Body body: Login): Observable<LoginResp>

    data class GetRecommendUser(
            @SerializedName("token") val token: String
    )

    @POST(Constants.GET_RECOMMEND_USER)
    fun getRecommendUser(@Body body: GetRecommendUser): Observable<ResponseWrapper<List<User>>>

    data class ResetPassword(
            @SerializedName("phone") val phone: String,
            @SerializedName("password") val password: String,
            @SerializedName("code") val code: String
    )

    data class ResetPasswordResp(
            @SerializedName("state") val state: String,
            @SerializedName("reason") val reason: String,
            @SerializedName("token") val token: String,
            @SerializedName("id") val id: String
    )

    @POST(Constants.RESET_PASSWORD)
    fun resetPassword(@Body body: ResetPassword): Observable<ResetPasswordResp>
}