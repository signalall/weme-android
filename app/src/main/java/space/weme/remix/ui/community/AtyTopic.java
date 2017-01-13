package space.weme.remix.ui.community;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.Post;
import space.weme.remix.model.PostTopic;
import space.weme.remix.service.PostService;
import space.weme.remix.service.Services;
import space.weme.remix.service.TopicService;
import space.weme.remix.ui.base.SwipeActivity;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 2016/1/28.
 * liujilong.me@gmail.com
 * 每个Topic下的Post列表
 */
public class AtyTopic extends SwipeActivity {
    private static final String TAG = "AtyTopic";
    public static final String TOPIC_ID = "topic_id";

    private static final int REQUEST_NEW_POST = 0xef;

    private String mTopicId;
    private PostTopic mPostTopic;
    private ArrayList<Post> mPostList;
    private boolean isRefreshing = false;
    private boolean isLoading = false;
    private int pageNumber = 0;
    private boolean morePages = true;

    private TopicAdapter mPostListAdapter;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.aty_topic_title_image)
    SimpleDraweeView mImage;

    @BindView(R.id.aty_topic_title_slogan)
    TextView mTvSlogan;

    @BindView(R.id.aty_topic_theme)
    TextView mTvTheme;

    @BindView(R.id.aty_topic_recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_topic);
        ButterKnife.bind(this);
        mTopicId = getIntent().getStringExtra(TOPIC_ID);

        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeSize(DimensionUtils.getDisplay().widthPixels / 2);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        AppBarLayout mBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        int width = DimensionUtils.getDisplay().widthPixels;
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(width, width / 2);
        mBarLayout.setLayoutParams(params);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(AtyTopic.this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (!isLoading &&
                        (totalItemCount - visibleItemCount) <= (firstVisibleItem + 1) &&
                        morePages) {
                    Log.i(TAG, "scroll to end  load page " + (pageNumber + 1));

                    rx.Observable.timer(0, TimeUnit.SECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(resp -> {
                                loadPage();
                            });
                }
            }
        });
        mPostListAdapter = new TopicAdapter(this);
        mRecyclerView.setAdapter(mPostListAdapter);
        mPostList = new ArrayList<>();
        mPostListAdapter.setPostList(mPostList);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isRefreshing) {
                    Log.d(TAG, "ignore manually update!");
                } else {
                    replacePage();
                }
            }
        });
        loadAll();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NEW_POST && resultCode == RESULT_OK) {
            loadAll();
        }
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        Intent i = new Intent(AtyTopic.this, AtyPostNew.class);
        i.putExtra(AtyPostNew.INTENT_ID, mTopicId);
        startActivityForResult(i, REQUEST_NEW_POST);
    }

    private void beforeReplacePage() {
        isRefreshing = true;
    }

    private void afterReplacePage() {
        isRefreshing = false;
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void beforeLoadPage() {
        if (!isLoading) {
            isLoading = true;
            mPostList.add(null);
            mPostListAdapter.notifyItemInserted(mPostList.size());
        }
    }

    private void afterLoadPage() {
        if (isLoading && mPostList.get(mPostList.size() - 1) == null) {
            isLoading = false;
            mPostList.remove(mPostList.size() - 1);
            mPostListAdapter.notifyItemRemoved(mPostList.size());
        }
    }

    private void loadAll() {
        Services.topicService()
                .getTopicInfo(new TopicService.GetTopicInfo(StrUtils.token(), mTopicId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getTopicInfo: " + resp.toString());
                    if (resp.getResult() == null) {
                        return;
                    }
                    mPostTopic = resp.getResult();
                    mImage.setImageURI(Uri.parse(mPostTopic.imageUrl));
                    mTvSlogan.setText(mPostTopic.slogan);
                    mTvTheme.setText(mPostTopic.theme);
                    mPostListAdapter.setTopic(mPostTopic);
                    mPostListAdapter.notifyDataSetChanged();
                }, ex -> {
                    Log.e(TAG, "getTopicInfo: " + ex.getMessage());
                });
        replacePage();
    }

    private void replacePage() {
        int pageSize = 10;
        beforeReplacePage();
        Services.postService()
                .getPostList(new PostService.GetPostList(
                        StrUtils.token(),
                        mTopicId,
                        String.valueOf(1),
                        String.valueOf(pageSize)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getPostList: " + resp.toString());
                    afterReplacePage();
                    pageNumber = 1;
                    morePages = true;
                    List<Post> posts = resp.getResult();
                    if (posts == null || posts.size() == 0) {
                        morePages = false;
                        return;
                    }
                    mPostList.clear();
                    mPostList.addAll(posts);
                    mPostListAdapter.setPostList(mPostList);
                    mPostListAdapter.notifyDataSetChanged();
                }, ex -> {
                    Log.e(TAG, "getPostList: " + ex.getMessage());
                    afterReplacePage();
                    pageNumber = pageNumber; //
                });
    }

    private void loadPage() {
        int pageSize = 10;
        beforeLoadPage();
        Services.postService()
                .getPostList(new PostService.GetPostList(StrUtils.token(),
                        mTopicId,
                        String.valueOf(pageNumber + 1), // next page
                        String.valueOf(pageSize)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getPostList: " + resp.toString());
                    afterLoadPage();
                    pageNumber += 1;
                    List<Post> posts = resp.getResult();
                    if (posts == null || posts.size() == 0) {
                        morePages = false;
                        return;
                    }
                    int previousCount = mPostList.size();
                    mPostList.addAll(posts);
                    mPostListAdapter.notifyItemRangeInserted(previousCount, posts.size());
                }, ex -> {
                    Log.e(TAG, "getPostList: " + ex.getMessage());
                    afterLoadPage();
                    pageNumber = pageNumber; //
                });
    }
}
