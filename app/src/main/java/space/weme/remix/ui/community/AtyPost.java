package space.weme.remix.ui.community;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import space.weme.remix.model.PostComment;
import space.weme.remix.service.PostService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.base.SwipeActivity;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 2016/1/30.
 * liujilong.me@gmail.com
 * 每个Post的具体内容
 */
public class AtyPost extends SwipeActivity {
    private static final String TAG = "AtyPost";
    public static final String POST_INTENT = "postId";
    public static final String THEME_INTENT = "theme";

    private static final int REPLY_CODE = 10;

    private String mPostID;

    private boolean isLoading = false;
    private int pageNumber = 1;
    private boolean morePages = true;

    private List<PostComment> mPostCommentList;
    private Post mPost;

    private PostAdapter mPostCommentListAdapter;

    private ProgressDialog mProgressDialog;

    @BindView(R.id.chat_view_holder)
    LinearLayout mChatView;

    @BindView(R.id.activity_post_editor)
    EditText mEditText;

    @BindView(R.id.activity_post_commit)
    TextView mCommentText;

    @BindView(R.id.activity_post_add_image)
    ImageView mAddImage;

    @BindView(R.id.post_detail_delete)
    TextView tvDelete;

    @BindView(R.id.post_detail_toolbar)
    TextView toolbar;

    @BindView(R.id.post_detail_recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_post);
        ButterKnife.bind(this);

        mPostID = getIntent().getStringExtra(POST_INTENT);
        String theme = getIntent().getStringExtra(THEME_INTENT);
        toolbar.setText(theme);
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeSize(DimensionUtils.getDisplay().widthPixels / 2);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(AtyPost.this));
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
                        (totalItemCount - visibleItemCount) <= (firstVisibleItem + 2) &&
                        morePages) {
                    loadPage();
                }
            }
        });
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mChatView.getVisibility() == View.VISIBLE) {
                    mChatView.setVisibility(View.INVISIBLE);
                    return true;
                }
                return false;
            }
        });
        mPostCommentListAdapter = new PostAdapter(this);
        mRecyclerView.setAdapter(mPostCommentListAdapter);
        mPostCommentList = new ArrayList<>();
        mPostCommentListAdapter.setReplyList(mPostCommentList);
        loadAll();
    }


    private View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final ProgressDialog progressDialog = new ProgressDialog(AtyPost.this);
            progressDialog.show();
            Services.postService()
                    .deletePost(new PostService.DeletePost(StrUtils.token(), mPostID))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        Log.d(TAG, "deletePost: " + resp.toString());
                        progressDialog.dismiss();
                        finish();
                    }, ex -> {
                        progressDialog.dismiss();
                    });
        }
    };


    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REPLY_CODE && resultCode == Activity.RESULT_OK) {
            loadAll();
        }
    }

    /**
     * 添加图片
     */
    @OnClick(R.id.activity_post_add_image)
    public void onAddImageClick() {
        Intent i = new Intent(AtyPost.this, AtyPostReply.class);
        i.putExtra(AtyPostReply.INTENT_ID, mPostID);
        i.putExtra(AtyPostReply.INTENT_CONTENT, mEditText.getText().toString());
        startActivityForResult(i, REPLY_CODE);
    }

    /**
     * 发送评论
     */
    @OnClick(R.id.activity_post_commit)
    public void onPostCommentClick() {
        if (mEditText.getText().length() == 0) {
            return;
        }
        mProgressDialog = ProgressDialog.show(AtyPost.this, null, getResources().getString(R.string.commenting));
        Services.postService()
                .commentToPost(new PostService.CommentToPost(StrUtils.token(),
                        mEditText.getText().toString(),
                        mPostID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "commentToPost: " + resp.toString());
                    mProgressDialog.dismiss();
                    clearChatView();
                    loadAll();
                }, ex -> {
                    Log.e(TAG, "commentToPost: " + ex.getMessage());
                    mProgressDialog.dismiss();
                });
    }


    /**
     * 评价Post
     */
    void commentToPost() {
        mChatView.setVisibility(View.VISIBLE);
        mAddImage.setVisibility(View.VISIBLE);
        mEditText.setText("");
        mEditText.setHint("");
    }

    /**
     * 评价Comment
     *
     * @param postComment
     */
    void commentToComment(final PostComment postComment) {
        mChatView.setVisibility(View.VISIBLE);
        mAddImage.setVisibility(View.GONE);
        mEditText.setHint(getString(R.string.comment) + postComment.getName() + ":");
        mEditText.setText("");
        mCommentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.getText().length() == 0) {
                    return;
                }
                mProgressDialog = ProgressDialog.show(AtyPost.this, null, getResources().getString(R.string.commenting));
                Services.postService()
                        .commentToComment(new PostService.CommentToComment(StrUtils.token(), mEditText.getText().toString(), postComment.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resp -> {
                            Log.d(TAG, "commentToComment: " + resp.toString());
                            mProgressDialog.dismiss();
                            clearChatView();
                            loadAll();
                        }, ex -> {
                            mProgressDialog.dismiss();
                        });
            }
        });
    }

    private void clearChatView() {
        mChatView.setVisibility(View.GONE);
        mEditText.setText("");
        mCommentText.setOnClickListener(null);
    }

    private void beforeReplacePage() {
        isLoading = true;
    }

    private void afterReplacePage() {
        isLoading = false;
    }

    private void beforeLoadPage() {
        if (!isLoading) {
            isLoading = true;
            mPostCommentList.add(null);
            mPostCommentListAdapter.notifyItemInserted(mPostCommentList.size() - 1);
        }
    }

    private void afterLoadPage() {
        if (isLoading && mPostCommentList.get(mPostCommentList.size() - 1) == null) {
            isLoading = false;
            mPostCommentList.remove(mPostCommentList.size() - 1);
            mPostCommentListAdapter.notifyItemRemoved(mPostCommentList.size());
        }
    }

    private void loadAll() {
        Services.postService()
                .getPostDetail(new PostService.GetPostDetail(StrUtils.token(), mPostID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getPostDetail: " + resp.toString());
                    if (resp.getResult() == null) {
                        return;
                    }
                    mPost = resp.getResult();
                    mPostCommentListAdapter.setPost(mPost);
                    mPostCommentListAdapter.notifyDataSetChanged();
                    if (TextUtils.equals(mPost.getUserId(), StrUtils.id())) {
                        tvDelete.setVisibility(View.VISIBLE);
                        tvDelete.setOnClickListener(deleteListener);
                    } else {
                        tvDelete.setVisibility(View.GONE);
                    }
                }, ex -> {
                    Log.d(TAG, "getPostDetail: " + ex.getMessage());
                });
        replacePage();
        mChatView.setVisibility(View.INVISIBLE);
        morePages = true;
    }

    private void replacePage() {
        int pageSize = 10;
        beforeReplacePage();
        Services.postService()
                .getPostComment(new PostService.GetPostComment(StrUtils.token(),
                        mPostID,
                        String.valueOf(1),
                        String.valueOf(pageSize)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getPostComment: " + resp.toString());
                    afterReplacePage();
                    pageNumber = 1;
                    morePages = true;
                    List<PostComment> comments = resp.getResult();
                    if (comments == null || comments.isEmpty()) {
                        morePages = false;
                        return;
                    }
                    mPostCommentList.clear();
                    mPostCommentList.addAll(comments);
                    mPostCommentListAdapter.setReplyList(mPostCommentList);
                    mPostCommentListAdapter.notifyDataSetChanged();
                }, ex -> {
                    Log.e(TAG, "getPostComment: " + ex.getMessage());
                    afterReplacePage();
                    pageNumber = pageNumber;
                });
    }

    private void loadPage() {
        int pageSize = 10;
        beforeLoadPage();
        Services.postService()
                .getPostComment(new PostService.GetPostComment(StrUtils.token(),
                        mPostID,
                        String.valueOf(pageNumber + 1),
                        String.valueOf(pageSize)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getPostComment: " + resp.toString());
                    afterLoadPage();
                    pageNumber += 1;
                    List<PostComment> comments = resp.getResult();
                    if (comments == null || comments.isEmpty()) {
                        morePages = false;
                        return;
                    }
                    int positionStart = mPostCommentList.size();
                    mPostCommentList.addAll(comments);
                    mPostCommentListAdapter.notifyItemRangeInserted(positionStart, comments.size());
                }, ex -> {
                    Log.e(TAG, "getPostComment: " + ex.getMessage());
                    afterLoadPage();
                    pageNumber = pageNumber;
                });
    }

}
