import org.junit.Before
import org.junit.Test
import space.weme.remix.service.PostService
import space.weme.remix.service.Services

/**
 * Created by Joyce on 2017/1/3.
 */

class PostServiceTest {

    private var mPostService: PostService? = null

    @Before
    fun setUp() {
        mPostService = Services.postService()
    }

    @Test
    fun test() {
    }
}