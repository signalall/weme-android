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
    private int curPage = 0;
    private boolean canLoadMore = true;

    private TopicAdapter mAdapter;

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

    @OnClick(R.id.fab)
    public void onFabClick() {
        Intent i = new Intent(AtyTopic.this, AtyPostNew.class);
        i.putExtra(AtyPostNew.INTENT_ID, mTopicId);
        startActivityForResult(i, REQUEST_NEW_POST);
    }
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
                if (!isLoading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + 1) && canLoadMore) {
                    // End has been reached
                    Log.i(TAG, "scroll to end  load page " + (curPage + 1));
                    loadPage(curPage + 1);
                }
            }
        });
        mAdapter = new TopicAdapter(AtyTopic.this);
        mRecyclerView.setAdapter(mAdapter);
        mPostList = new ArrayList<>();
        mAdapter.setPostList(mPostList);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isRefreshing) {
                    Log.d(TAG, "ignore manually update!");
                } else {
                    loadPage(1, true);
                }
            }
        });
        loadAll();
    }

    private void loadAll() {
        Services.topicService()
                .getTopicInfo(new TopicService.GetTopicInfo(StrUtils.token(), mTopicId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getTopicInfo: " + resp.toString());
                    PostTopic postTopic = resp.getResult();
                    mPostTopic = postTopic;
                    mImage.setImageURI(Uri.parse(mPostTopic.imageUrl));
                    mTvSlogan.setText(mPostTopic.slogan);
                    mTvTheme.setText(mPostTopic.theme);
                    mAdapter.setTopic(mPostTopic);
                }, ex -> {
                    Log.e(TAG, ex.getMessage());
                });
        loadPage(1, true);
    }

    private void beforeLoadPage(boolean replace) {
        if (replace) {
            isRefreshing = true;
            curPage = 1;
        } else {
            isLoading = true;
            mPostList.add(null);
            mAdapter.notifyItemInserted(mPostList.size());
        }
    }

    private void afterLoadPage(boolean replace) {
        if (replace) {
            mSwipeRefreshLayout.setRefreshing(false);
            isRefreshing = false;
        } else {
            isLoading = false;
            mPostList.remove(mPostList.size() - 1);
            mAdapter.notifyItemRemoved(mPostList.size());
        }
    }

    private void loadPage(int page) {
        loadPage(page, false);
    }

    private void loadPage(int page, final boolean replace) {
        beforeLoadPage(replace);
        Services.postService()
                .getPostList(new PostService.GetPostList(StrUtils.token(), mTopicId, page + ""))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getPostList: " + resp.toString());
                    List<Post> posts = resp.getResult();
                    afterLoadPage(replace);
                    if (replace) {
                        canLoadMore = true;
                    } else {
                        curPage++;
                    }
                    int previousCount = mPostList.size();
                    int size = posts.size();
                    if (replace) {
                        mPostList.clear();
                    }
                    if (size == 0) {
                        canLoadMore = false;
                        return;
                    }
                    mPostList.addAll(posts);
                    mAdapter.setPostList(mPostList);
                    if (replace) {
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mAdapter.notifyItemRangeInserted(previousCount, size);
                    }
                }, ex -> {
                    Log.e(TAG, "getPostList: " + ex.getMessage());
                    afterLoadPage(replace);
                });
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
}
