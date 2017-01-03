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
 */
public class AtyPost extends SwipeActivity {
    private static final String TAG = "AtyPost";
    public static final String POST_INTENT = "postId";
    public static final String THEME_INTENT = "theme";

    private static final int REPLY_CODE = 10;

    private String mPostID;


    private boolean isLoading = false;
    private int curPage = 1;
    private boolean canLoadMore = true;

    private List<PostComment> mPostCommentList;
    private Post mPost;

    private PostAdapter mAdapter;

    ProgressDialog mProgressDialog;

    private LinearLayout mChatView;
    private EditText mEditText;
    private TextView mCommentText;
    private ImageView mAddImage;

    private TextView tvDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPostID = getIntent().getStringExtra(POST_INTENT);
        String theme = getIntent().getStringExtra(THEME_INTENT);
        setContentView(R.layout.aty_post);
        TextView toolbar = (TextView) findViewById(R.id.post_detail_toolbar);
        toolbar.setText(theme);
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeSize(DimensionUtils.getDisplay().widthPixels / 2);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        tvDelete = (TextView) findViewById(R.id.post_detail_delete);

        mChatView = (LinearLayout) findViewById(R.id.chat_view_holder);
        mEditText = (EditText) findViewById(R.id.activity_post_editor);
        mCommentText = (TextView) findViewById(R.id.activity_post_commit);
        mAddImage = (ImageView) findViewById(R.id.activity_post_add_image);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.post_detail_recycler_view);
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
                if (!isLoading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + 2) && canLoadMore) {
                    LogUtils.i(TAG, "scroll to end  load page " + (curPage + 1));
                    loadPage(curPage + 1);
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
        mAdapter = new PostAdapter(this);
        mPostCommentList = new ArrayList<>();
        mAdapter.setReplyList(mPostCommentList);
        mRecyclerView.setAdapter(mAdapter);
        refreshAll();
    }

    private void refreshAll() {
        mPostCommentList.clear();
        Services.postService()
                .getPostDetail(new PostService.GetPostDetail(StrUtils.token(), mPostID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(post -> {
                    if (post.getResult() == null) {
                        return;
                    }
                    mPost = post.getResult();
                    if (TextUtils.equals(mPost.getUserId(), StrUtils.id())) {
                        tvDelete.setVisibility(View.VISIBLE);
                        tvDelete.setOnClickListener(deleteListener);
                    } else {
                        tvDelete.setVisibility(View.GONE);
                    }
                    mAdapter.setPost(mPost);
                    mAdapter.notifyDataSetChanged();
                }, ex -> {
                });
        loadPage(1);
        mChatView.setVisibility(View.INVISIBLE);
        canLoadMore = true;
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

    private void loadPage(final int page) {
        beforeLoadPage(page);
        curPage = page;
        Services.postService()
                .getPostComment(new PostService.GetPostComment(StrUtils.token(), mPostID, page + ""))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getPostComment: " + resp.toString());
                    afterLoadPage(page);
                    List<PostComment> comments = resp.getResult();
                    if (comments == null || comments.isEmpty()) {
                        canLoadMore = false;
                        return;
                    }
                    int previousCount = mPostCommentList.size();
                    mPostCommentList.addAll(comments);
                    if (page == 1) {
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mAdapter.notifyItemRangeInserted(previousCount, comments.size());
                    }
                }, ex -> {
                    Log.e(TAG, "getPostComment: " + ex.getMessage());
                    afterLoadPage(page);
                });
    }

    private void beforeLoadPage(int page) {
        isLoading = true;
        if (page != 1) {
            mPostCommentList.add(null);
            mAdapter.notifyItemInserted(mPostCommentList.size() + 1);
        }
    }

    private void afterLoadPage(int page) {
        isLoading = false;
        if (page != 1) {
            mPostCommentList.remove(mPostCommentList.size() - 1);
            mAdapter.notifyItemRemoved(mPostCommentList.size() + 1);
        }
    }


    @Override
    protected String tag() {
        return TAG;
    }

    /**
     * 评价Post
     */
    void commentToPost() {
        mChatView.setVisibility(View.VISIBLE);
        mAddImage.setVisibility(View.VISIBLE);
        mEditText.setText("");
        mEditText.setHint("");
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AtyPost.this, AtyPostReply.class);
                i.putExtra(AtyPostReply.INTENT_ID, mPostID);
                i.putExtra(AtyPostReply.INTENT_CONTENT, mEditText.getText().toString());
                startActivityForResult(i, REPLY_CODE);
            }
        });
        mCommentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.getText().length() == 0) {
                    return;
                }
                mProgressDialog = ProgressDialog.show(AtyPost.this, null, getResources().getString(R.string.commenting));
                Services.postService()
                        .commentToPost(new PostService.CommentToPost(StrUtils.token(), mEditText.getText().toString(), mPostID))
                        .subscribe(resp -> {
                            Log.d(TAG, "commentToPost: " + resp.toString());
                            mProgressDialog.dismiss();
                            LogUtils.i(TAG, resp.toString());
                            clearChatView();
                            refreshAll();
                        }, ex -> {
                            mProgressDialog.dismiss();
                        });
            }
        });
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
                            refreshAll();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REPLY_CODE && resultCode == Activity.RESULT_OK) {
            refreshAll();
        }
    }
}
