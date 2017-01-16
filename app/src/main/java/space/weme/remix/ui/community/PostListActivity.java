package space.weme.remix.ui.community;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import space.weme.remix.model.Topic;
import space.weme.remix.service.PostService;
import space.weme.remix.service.Services;
import space.weme.remix.service.TopicService;
import space.weme.remix.ui.AtyImage;
import space.weme.remix.ui.base.SwipeActivity;
import space.weme.remix.ui.user.AtyInfo;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.GridLayout;

/**
 * Created by Liujilong on 2016/1/28.
 * liujilong.me@gmail.com
 * 每个Topic下的Post列表
 */
public class PostListActivity extends SwipeActivity {
    public static final String TOPIC_ID = "topic_id";
    private static final String TAG = PostListActivity.class.getSimpleName();
    private static final int REQUEST_NEW_POST = 0xef;
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
    private String mTopicId;
    private Topic mTopic;
    private ArrayList<Post> mPostList;
    private boolean isRefreshing = false;
    private boolean isLoading = false;
    private int pageNumber = 0;
    private boolean morePages = true;
    private PostListAdapter mPostListAdapter;

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

        mRecyclerView.setLayoutManager(new LinearLayoutManager(PostListActivity.this));
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
        mPostListAdapter = new PostListAdapter(this);
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
        Intent intent = new Intent(PostListActivity.this, AtyPostNew.class);
        intent.putExtra(AtyPostNew.INTENT_ID, mTopicId);
        startActivityForResult(intent, REQUEST_NEW_POST);
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
                    mTopic = resp.getResult();
                    mImage.setImageURI(Uri.parse(mTopic.imageUrl));
                    mTvSlogan.setText(mTopic.slogan);
                    mTvTheme.setText(mTopic.theme);
                    mPostListAdapter.setTopic(mTopic);
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

    /**
     * Created by Liujilong on 2016/1/28.
     * liujilong.me@gmail.com
     * Post列表的Adapter
     */
    static class PostListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int imageID = StrUtils.generateViewId();
        private static final int itemID = StrUtils.generateViewId();
        private final int TYPE_ITEM = 0x2;
        private final int TYPE_PROGRESS = 0x3;
        private final int KEY_1 = 0x4;
        private final int KEY_2 = 0x5;
        private Context mContext;
        private List<Post> mPostList;
        private Topic mTopic;
        private View.OnClickListener mListener;

        PostListAdapter(Context context) {
            mContext = context;
            mListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.aty_topic_item_avatar) {
                        String userId = (String) v.getTag();
                        Intent i = new Intent(mContext, AtyInfo.class);
                        i.putExtra(AtyInfo.ID_INTENT, userId);
                        mContext.startActivity(i);
                    } else if (v.getId() == imageID) {
                        Intent i = new Intent(mContext, AtyImage.class);
                        i.putExtra(AtyImage.INTENT_JSON, (String) v.getTag());
                        mContext.startActivity(i);
                        ((Activity) mContext).overridePendingTransition(0, 0);
                    } else if (v.getId() == itemID) {
                        Post post = (Post) v.getTag();
                        Intent i = new Intent(mContext, PostDetailActivity.class);
                        i.putExtra(PostDetailActivity.POST_INTENT, post.getPostId());
                        i.putExtra(PostDetailActivity.THEME_INTENT, mTopic == null ? "" : mTopic.theme);
                        mContext.startActivity(i);
                    }

                }
            };
        }

        void setPostList(List<Post> postList) {
            mPostList = postList;
        }

        void setTopic(Topic topic) {
            mTopic = topic;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder = null;
            if (viewType == TYPE_ITEM) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.aty_topic_item, parent, false);
                holder = new ItemViewHolder(v);
            } else if (viewType == TYPE_PROGRESS) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.aty_topic_progress, parent, false);
                holder = new ProgressViewHolder(v);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder item = (ItemViewHolder) holder;
                Post post = mPostList.get(position);
                item.avatar.setImageURI(Uri.parse(StrUtils.thumForID(post.getUserId())));
                item.avatar.setTag(post.getUserId());
                item.avatar.setOnClickListener(mListener);
                item.userName.setText(post.getName());
                item.university.setText(post.getSchool());
                item.time.setText(StrUtils.timeTransfer(post.getTimestamp()));
                item.title.setText(post.getTitle());
                item.content.setText(post.getContent());
                item.like_number.setText(post.getLikeNumber());
                item.comment_number.setText(post.getCommentNumber());
                item.grid.removeAllViews();
                for (int i = 0; i < post.getThumbnailUrl().size() && i < 4; i++) {
                    String url = post.getThumbnailUrl().get(i);
                    SimpleDraweeView image = new SimpleDraweeView(mContext);
                    item.grid.addView(image);
                    image.setImageURI(Uri.parse(url));
                    image.setId(imageID);
                    image.setOnClickListener(mListener);
                    try {
                        JSONObject j = new JSONObject();
                        JSONArray array = new JSONArray(post.getImageUrl());
                        j.put(AtyImage.KEY_ARRAY, array);
                        j.put(AtyImage.KEY_INDEX, i);
                        image.setTag(j.toString());
                    } catch (JSONException e) {
                        // ignore
                    }
                }
                item.itemView.setTag(post);
                item.itemView.setOnClickListener(mListener);
            } else if (holder instanceof ProgressViewHolder) {
                ProgressViewHolder progress = (ProgressViewHolder) holder;
                progress.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mPostList.get(position) != null) {
                return TYPE_ITEM;
            } else {
                return TYPE_PROGRESS;
            }
        }

        @Override
        public int getItemCount() {
            return mPostList == null ? 0 : mPostList.size();
        }

        static class ItemViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.aty_topic_item_avatar)
            SimpleDraweeView avatar;

            @BindView(R.id.aty_topic_item_name)
            TextView userName;

            @BindView(R.id.aty_topic_item_university)
            TextView university;

            @BindView(R.id.aty_topic_item_time)
            TextView time;

            @BindView(R.id.aty_topic_item_title)
            TextView title;

            @BindView(R.id.aty_topic_item_content)
            TextView content;

            @BindView(R.id.aty_topic_item_like_number)
            TextView like_number;

            @BindView(R.id.aty_topic_item_comment_number)
            TextView comment_number;

            @BindView(R.id.aty_topic_item_grid)
            GridLayout grid;

            public ItemViewHolder(View itemView) {
                super(itemView);
                itemView.setId(itemID);
                ButterKnife.bind(this, itemView);
                grid.setNumInRow(4);
            }
        }

        static class ProgressViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.progress)
            public ProgressBar progressBar;

            public ProgressViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }
        }
    }
}
