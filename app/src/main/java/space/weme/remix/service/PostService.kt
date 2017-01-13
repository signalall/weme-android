package space.weme.remix.service

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.POST
import rx.Observable
import space.weme.remix.Constants
import space.weme.remix.model.Post
import space.weme.remix.model.PostComment
import space.weme.remix.model.ResponseWrapper

interface PostService {

    data class GetPostDetail(
            @SerializedName("token") val token: String,
            @SerializedName("postid") val postId: String
    )

    @POST(Constants.GET_POST_DETAIL)
    fun getPostDetail(
            @Body body: GetPostDetail
    ): Observable<ResponseWrapper<Post>>

    data class GetPostList(
            @SerializedName("token") val token: String,
            @SerializedName("topicid") val topicId: String,
            @SerializedName("page") val page: String
    )

    @POST(Constants.GET_POST_LIST)
    fun getPostList(
            @Body body: GetPostList
    ): Observable<ResponseWrapper<List<Post>>>

    data class LikePost(
            @SerializedName("token") val token: String,
            @SerializedName("postid") val postId: String
    )

    @POST(Constants.LIKE_POST_URL)
    fun likePost(
            @Body body: LikePost
    ): Observable<ResponseWrapper<Any>>

    data class UnlikePost(
            @SerializedName("token") val token: String,
            @SerializedName("postid") val postId: String
    )

    @POST(Constants.UNLIKE_POST_URL)
    fun unlikePost(
            @Body body: UnlikePost
    ): Observable<ResponseWrapper<Any>>

    data class CommentToPost(
            @SerializedName("token") val token: String,
            @SerializedName("body") val body: String,
            @SerializedName("postid") val postId: String
    )

    @POST(Constants.COMMENT_TO_POST_URL)
    fun commentToPost(
            @Body body: CommentToPost
    ): Observable<Map<String, String>>

    data class PublishPost(
            @SerializedName("token") val token: String,
            @SerializedName("topicid") val topicId: String,
            @SerializedName("title") val title: String,
            @SerializedName("body") val body: String
    )

    @POST(Constants.PUBLISH_POST_URL)
    fun publishPost(
            @Body body: PublishPost
    ): Observable<Map<String, String>>

    data class GetPostComment(
            @SerializedName("token") val token: String,
            @SerializedName("postid") val postId: String,
            @SerializedName("page") val page: String
    )

    @POST(Constants.GET_POST_COMMENT)
    fun getPostComment(
            @Body body: GetPostComment
    ): Observable<ResponseWrapper<List<PostComment>>>

    data class CommentToComment(
            @SerializedName("token") val token: String,
            @SerializedName("body") val body: String,
            @SerializedName("destcommentid") val destCommentId: String
    )

    @POST(Constants.COMMENT_TO_COMMENT_URL)
    fun commentToComment(
            @Body body: CommentToComment
    ): Observable<Any>

    data class DeletePost(
            @SerializedName("token") val token: String,
            @SerializedName("postid") val postId: String
    )

    @POST(Constants.DELETE_POST_URL)
    fun deletePost(
            @Body body: DeletePost
    ): Observable<Any>

    data class LikeComment(
            @SerializedName("token") val token: String,
            @SerializedName("commentid") val commentId: String
    )

    @POST(Constants.LIKE_COMMENT_URL)
    fun likeComment(
            @Body body: LikeComment
    ): Observable<Any>

    data class UnlikeComment(
            @SerializedName("token") val token: String,
            @SerializedName("commentid") val commentId: String
    )

    @POST(Constants.UNLIKE_COMMENT_URL)
    fun unlikeComment(
            @Body body: UnlikeComment
    ): Observable<Any>
}


