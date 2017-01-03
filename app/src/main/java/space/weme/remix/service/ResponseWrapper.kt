package space.weme.remix.service

/**
 * Created by Joyce on 2017/1/2.
 */
class ResponseWrapper<T> {
    var result: T? = null
    override fun toString(): String {
        return "ResponseWrapper(result=$result)"
    }
}