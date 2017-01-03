package space.weme.remix.model

/**
 * Created by Joyce on 2017/1/2.
 */
class ResponseWrapper<T> {
    var state: String? = null
    var result: T? = null
    override fun toString(): String {
        return "ResponseWrapper(state=$state, result=$result)"
    }
}