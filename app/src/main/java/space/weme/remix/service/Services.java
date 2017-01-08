package space.weme.remix.service;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import space.weme.remix.util.StrUtils;

/**
 * Created by Joyce on 2017/1/2.
 */

public class Services {

    private static PostService mPostService;
    private static TopicService mTopicService;
    private static FoodService mFoodService;
    private static ActivityService mActivityService;
    private static UserService mUserSerivce;
    private static Retrofit restAdapter;

    static {
        OkHttpClient client = new OkHttpClient().newBuilder()
                // .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        restAdapter = new Retrofit.Builder()
                .baseUrl(StrUtils.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    private static <T> T create(Class clazz) {
        return (T) restAdapter.create(clazz);
    }

    public static PostService postService() {
        if (mPostService == null)
            mPostService = Services.create(PostService.class);
        return mPostService;
    }

    public static TopicService topicService() {
        if (mTopicService == null)
            mTopicService = Services.create(TopicService.class);
        return mTopicService;
    }

    public static FoodService foodService() {
        if (mFoodService == null)
            mFoodService = Services.create(FoodService.class);
        return mFoodService;
    }

    public static ActivityService activityService() {
        if (mActivityService == null)
            mActivityService = Services.create(ActivityService.class);
        return mActivityService;
    }

    public static UserService userService() {
        if (mUserSerivce == null)
            mUserSerivce = Services.create(UserService.class);
        return mUserSerivce;
    }
}
