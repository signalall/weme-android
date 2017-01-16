package space.weme.remix.ui.community;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.Constants;
import space.weme.remix.R;
import space.weme.remix.model.Comment;
import space.weme.remix.model.Post;
import space.weme.remix.model.PostComment;
import space.weme.remix.service.PostService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.AtyImage;
import space.weme.remix.ui.base.SwipeActivity;
import space.weme.remix.ui.user.AtyInfo;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.GridLayout;

/**
 * Created by Liujilong on 2016/1/30.
 * liujilong.me@gmail.com
 * 每个Post的具体内容
 */
public class PostDetailActivity extends SwipeActivity {
    public static final String POST_INTENT = "postId";
    public static final String THEME_INTENT = "theme";
    private static final String TAG = PostDetailActivity.class.getSimpleName();
    private static final int REPLY_CODE = 10;
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
    private String mPostID;
    private boolean isLoading = false;
    private int pageNumber = 1;
    private boolean morePages = true;
    private List<PostComment> mPostCommentList;
    private Post mPost;
    private CommentListAdapter mPostCommentListAdapter;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ButterKnife.bind(this);

        mPostID = getIntent().getStringExtra(POST_INTENT);
        String theme = getIntent().getStringExtra(THEME_INTENT);
        toolbar.setText(theme);
        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeSize(DimensionUtils.getDisplay().widthPixels / 2);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(PostDetailActivity.this));
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
        mPostCommentListAdapter = new CommentListAdapter(this);
        mRecyclerView.setAdapter(mPostCommentListAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPostCommentList = new ArrayList<>();
        mPostCommentListAdapter.setComments(mPostCommentList);
        loadAll();
    }

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

    @OnClick(R.id.post_detail_delete)
    public void onDeleteClick() {
        ProgressDialog progressDialog = new ProgressDialog(PostDetailActivity.this);
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

    /**
     * 添加图片
     */
    @OnClick(R.id.activity_post_add_image)
    public void onAddImageClick() {
        Intent i = new Intent(PostDetailActivity.this, AtyPostReply.class);
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
        mProgressDialog = ProgressDialog.show(PostDetailActivity.this, null, getResources().getString(R.string.commenting));
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
                    showNetworkError();
                });
    }


    /**
     * 评价Post
     */
    void showCommentToPostChatView() {
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
    private void showCommentToCommentChatView(final PostComment postComment) {
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
                mProgressDialog = ProgressDialog.show(PostDetailActivity.this, null, getResources().getString(R.string.commenting));
                Services.postService()
                        .commentToComment(new PostService.CommentToComment(StrUtils.token(), mEditText.getText().toString(), postComment.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resp -> {
                            Log.d(TAG, "showCommentToCommentChatView: " + resp.toString());
                            mProgressDialog.dismiss();
                            clearChatView();
                            loadAll();
                        }, ex -> {
                            mProgressDialog.dismiss();
                            showNetworkError();
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

    private void showNetworkError() {
        Toast.makeText(PostDetailActivity.this,
                R.string.network_error,
                Toast.LENGTH_SHORT)
                .show();
    }

    private void loadAll() {
        Services.postService()
                .getPostDetail(new PostService.GetPostDetail(StrUtils.token(), mPostID))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getPostDetail: " + resp.toString());
                    if (Constants.STATE_SUCCESSFUL.equals(resp.getState())) {
                        mPost = resp.getResult();
                        mPostCommentListAdapter.setTitle(mPost);
                        mPostCommentListAdapter.notifyDataSetChanged();
                        if (StrUtils.id().equals(mPost.getUserId())) {
                            tvDelete.setVisibility(View.VISIBLE);
                        } else {
                            tvDelete.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(PostDetailActivity.this,
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "getPostDetail: " + ex.getMessage());
                    showNetworkError();
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
                    mPostCommentListAdapter.setComments(mPostCommentList);
                    mPostCommentListAdapter.notifyDataSetChanged();
                }, ex -> {
                    Log.e(TAG, "getPostComment: " + ex.getMessage());
                    afterReplacePage();
                    pageNumber = pageNumber;
                    showNetworkError();
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
                    showNetworkError();
                });
    }

    class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final String TAG = "CommentListAdapter";
        private static final int VIEW_TITLE = 1;
        private static final int VIEW_ITEM = 2;
        private static final int VIEW_PROGRESS = 3;

        private Context mContext;
        private Post mPost;
        private List<PostComment> mComments;
        private View.OnClickListener mListener;
        private int imageID = StrUtils.generateViewId();
        private int avatarId = StrUtils.generateViewId();


        CommentListAdapter(Context context) {
            mContext = context;
            mListener = new PostListener();
        }

        void setTitle(Post post) {
            mPost = post;
        }

        void setComments(List<PostComment> list) {
            mComments = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder;
            if (viewType == VIEW_TITLE) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_post_comment_title, parent, false);
                holder = new TitleViewHolder(v);
            } else if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_comment_item, parent, false);
                holder = new CommentViewHolder(v);
            } else {
                View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_progress, parent, false);
                holder = new ProgressViewHolder(v);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TitleViewHolder) {
                if (mPost == null) {
                    return;
                }
                TitleViewHolder viewHolder = (TitleViewHolder) holder;
                viewHolder.avatarDraw.setImageURI(Uri.parse(StrUtils.thumForID(mPost.getUserId())));
                viewHolder.avatarDraw.setTag(mPost.getUserId());
                viewHolder.avatarDraw.setOnClickListener(mListener);
                viewHolder.tvName.setText(mPost.getName());
                viewHolder.tvUniversity.setText(mPost.getSchool());
                viewHolder.tvTime.setText(StrUtils.timeTransfer(mPost.getTimestamp()));
                viewHolder.tvTitle.setText(mPost.getTitle());
                viewHolder.tvContent.setText(mPost.getContent());
                viewHolder.imagesGridLayout.removeAllViews();
                if (mPost.getThumbnailUrl().size() <= 1) {
                    viewHolder.imagesGridLayout.setNumInRow(1);
                } else if (mPost.getThumbnailUrl().size() <= 4) {
                    viewHolder.imagesGridLayout.setNumInRow(2);
                } else {
                    viewHolder.imagesGridLayout.setNumInRow(3);
                }
                for (int i = 0; i < mPost.getThumbnailUrl().size(); i++) {
                    String thumbUrl = mPost.getImageUrl().get(i);
                    SimpleDraweeView image = new SimpleDraweeView(mContext);
                    viewHolder.imagesGridLayout.addView(image);
                    image.setImageURI(Uri.parse(thumbUrl));
                    image.setId(imageID);
                    image.setOnClickListener(mListener);
                    try {
                        JSONObject j = new JSONObject();
                        JSONArray array = new JSONArray(mPost.getImageUrl());
                        j.put(AtyImage.KEY_INDEX, i);
                        j.put(AtyImage.KEY_ARRAY, array);
                        image.setTag(j);
                    } catch (JSONException e) {
                        // ignore
                    }
                }

                // Like and comment
                viewHolder.postLikeCount.setText(mPost.getLikeNumber());
                viewHolder.postCommentCount.setText(mPost.getCommentNumber());
                boolean off = mPost.getFlag().equals("0");
                viewHolder.postLikeImageView.setImageResource(off ? R.mipmap.like_off : R.mipmap.like_on);

                viewHolder.postLikeImageView.setOnClickListener(v -> likePost());
                viewHolder.postLikeCount.setOnClickListener(v -> likePost());
                viewHolder.postCommentImageView.setOnClickListener(v -> showCommentToPostChatView());
                viewHolder.postCommentCount.setOnClickListener(v -> showCommentToPostChatView());

                viewHolder.llLikePeoples.removeAllViews();
                for (int id : mPost.getLikeUserIds()) {
                    SimpleDraweeView avatar = (SimpleDraweeView) LayoutInflater.from(mContext).
                            inflate(R.layout.aty_post_avatar, viewHolder.llLikePeoples, false);
                    viewHolder.llLikePeoples.addView(avatar);
                    avatar.setImageURI(Uri.parse(StrUtils.thumForID(Integer.toString(id))));
                    avatar.setTag(String.valueOf(id));
                    avatar.setId(avatarId);
                    avatar.setOnClickListener(mListener);
                    // todo show more
                }
            } else if (holder instanceof CommentViewHolder) {
                final PostComment postComment = mComments.get(position - 1);
                if (postComment == null) {
                    return;
                }
                final CommentViewHolder viewHolder = (CommentViewHolder) holder;
                viewHolder.avatarDraw.setImageURI(Uri.parse(StrUtils.thumForID(postComment.getUserId())));
                viewHolder.avatarDraw.setTag(postComment.getUserId());
                viewHolder.avatarDraw.setOnClickListener(mListener);
                viewHolder.tvName.setText(postComment.getName());
                viewHolder.tvUniversity.setText(postComment.getSchool());
                viewHolder.tvTime.setText(StrUtils.timeTransfer(postComment.getTimestamp()));
                viewHolder.tvContent.setText(postComment.getContent());

                // Like and comment
                viewHolder.commentLikeCount.setText(String.valueOf(postComment.getLikeNumber()));
                viewHolder.commentCount.setText(String.valueOf(postComment.getCommentCount()));
                boolean off = postComment.getFlag().equals("0");
                viewHolder.commentLikeImageView.setImageResource(off ? R.mipmap.like_off : R.mipmap.like_on);
                viewHolder.commentLikeImageView.setOnClickListener(v -> likeComment((int) v.getTag()));
                viewHolder.commentLikeCount.setOnClickListener(v -> likeComment((int) v.getTag()));
                viewHolder.commentCommentImageView.setOnClickListener(v -> showCommentToCommentChatView(postComment));
                viewHolder.commentCount.setOnClickListener(v -> showCommentToCommentChatView(postComment));

                viewHolder.llReplyList.removeAllViews();
                viewHolder.llReplyList.setVisibility(postComment.getComments().size() == 0 ? View.GONE : View.VISIBLE);
                for (Comment commentReply : postComment.getComments()) {
                    TextView tv = new TextView(mContext);
                    SpannableStringBuilder builder = new SpannableStringBuilder();
                    builder.append(commentReply.getFromUsername());
                    int color = mContext.getResources().getColor(R.color.colorPrimary);
                    builder.setSpan(new ForegroundColorSpan(color), 0, builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    if (commentReply.getToCommentId() != null) {
                        builder.append(mContext.getString(R.string.postComment));
                        int len = builder.length();
                        builder.append(commentReply.getToUsername());
                        builder.setSpan(new ForegroundColorSpan(color), len, builder.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                    }
                    builder.append(":").append(commentReply.getContent());
                    tv.setText(builder);
                    viewHolder.llReplyList.addView(tv);
                }
                viewHolder.imagesGridLayout.removeAllViews();

                if (postComment.getImage().size() <= 1) {
                    viewHolder.imagesGridLayout.setNumInRow(1);
                } else if (postComment.getImage().size() <= 4) {
                    viewHolder.imagesGridLayout.setNumInRow(2);
                } else {
                    viewHolder.imagesGridLayout.setNumInRow(3);
                }
                if (postComment.getImage() == null || postComment.getImage().size() == 0) {
                    viewHolder.imagesGridLayout.setVisibility(View.GONE);
                } else {
                    viewHolder.imagesGridLayout.setVisibility(View.VISIBLE);
                    for (int i = 0; i < postComment.getThumbnail().size(); i++) {
                        SimpleDraweeView drawView = new SimpleDraweeView(mContext);
                        drawView.setImageURI(Uri.parse(postComment.getImage().get(i)));
                        drawView.setId(imageID);
                        drawView.setOnClickListener(mListener);
                        viewHolder.imagesGridLayout.addView(drawView);
                        try {
                            JSONObject j = new JSONObject();
                            JSONArray array = new JSONArray(postComment.getImage());
                            j.put(AtyImage.KEY_ARRAY, array);
                            j.put(AtyImage.KEY_INDEX, i);
                            drawView.setTag(j);
                        } catch (JSONException e) {
                            // ignore
                        }
                    }
                }
            } else {
                ProgressViewHolder progress = (ProgressViewHolder) holder;
                progress.progressBar.setIndeterminate(true);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_TITLE;
            } else if (mComments.get(position - 1) != null) {
                return VIEW_ITEM;
            } else {
                return VIEW_PROGRESS;
            }
        }

        @Override
        public int getItemCount() {
            return 1 + (mComments == null ? 0 : mComments.size());
        }

        private void likePost() {
            if ("0".equals(mPost.getFlag())) { // Like
                Services.postService()
                        .likePost(new PostService.LikePost(StrUtils.token(), mPost.getPostId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resp -> {
                            Log.d(TAG, "likePost: " + resp.toString());
                            if ("successful".equals(resp.getState())) {
                                mPost.setLikeNumber(String.valueOf(resp.getLikeNumber()));
                                mPost.getLikeUserIds().add(Integer.parseInt(StrUtils.id()));
                                mPost.setFlag("1");
                                notifyItemChanged(0);
                            }
                        }, ex -> {
                            Log.e(TAG, "likePost: " + ex.getMessage());
                        });
            } else { // Unlike
                Services.postService()
                        .unlikePost(new PostService.UnlikePost(StrUtils.token(), mPost.getPostId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resp -> {
                            Log.d(TAG, "unlikePost: " + resp.toString());
                            if ("successful".equals(resp.getState())) {
                                mPost.setLikeNumber(String.valueOf(resp.getLikeNumber()));
                                mPost.getLikeUserIds().remove(Integer.parseInt(StrUtils.id()));
                                mPost.setFlag("0");
                                notifyItemChanged(0);
                            }
                        }, ex -> {
                            Log.e(TAG, "unlikePost: " + ex.getMessage());
                        });
            }
        }

        private void likeComment(int position) {
            PostComment postComment = mComments.get(position - 1);

            if ("0".equals(postComment.getFlag())) {
                Services.postService()
                        .likeComment(new PostService.LikeComment(StrUtils.token(), postComment.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resp -> {
                            Log.d(TAG, "likeComment: " + resp.toString());
                            if ("successful".equals(resp.getState())) {
                                postComment.setFlag("1");
                                postComment.setLikeNumber(resp.getLikeNumber());
                                notifyItemChanged(position);
                            }
                        }, ex -> {
                            Log.e(TAG, "likeComment: " + ex.getMessage());
                        });
            } else {
                Services.postService()
                        .unlikeComment(new PostService.UnlikeComment(StrUtils.token(), postComment.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resp -> {
                            Log.d(TAG, "unlikeComment: " + resp.toString());
                            if ("successful".equals(resp.getState())) {
                                postComment.setFlag("0");
                                postComment.setLikeNumber(resp.getLikeNumber());
                                notifyItemChanged(position);
                            }
                        }, ex -> {
                            Log.e(TAG, "unlikeComment: " + ex.getMessage());
                        });
            }
        }

        class TitleViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.aty_post_title_avatar)
            SimpleDraweeView avatarDraw;

            @BindView(R.id.aty_post_title_user)
            TextView tvName;

            @BindView(R.id.aty_post_title_university)
            TextView tvUniversity;

            @BindView(R.id.aty_post_title_time)
            TextView tvTime;

            @BindView(R.id.aty_post_title_title)
            TextView tvTitle;

            @BindView(R.id.aty_post_title_content)
            TextView tvContent;

            @BindView(R.id.aty_post_title_image)
            GridLayout imagesGridLayout; // post images

            @BindView(R.id.aty_post_title_like_image)
            ImageView postLikeImageView;

            @BindView(R.id.aty_post_title_like_number)
            TextView postLikeCount;

            @BindView(R.id.aty_post_title_comment_post)
            ImageView postCommentImageView;

            @BindView(R.id.aty_post_title_reply_number)
            TextView postCommentCount;

            @BindView(R.id.aty_post_title_like_people)
            LinearLayout llLikePeoples; // liked people

            public TitleViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class CommentViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.comment_avatar)
            SimpleDraweeView avatarDraw;

            @BindView(R.id.comment_name)
            TextView tvName;

            @BindView(R.id.comment_school)
            TextView tvUniversity;

            @BindView(R.id.comment_time)
            TextView tvTime;

            @BindView(R.id.comment_content)
            TextView tvContent;

            @BindView(R.id.comment_to_comment)
            LinearLayout llReplyList;

            @BindView(R.id.comment_image)
            GridLayout imagesGridLayout;

            @BindView(R.id.comment_like_count)
            TextView commentLikeCount;

            @BindView(R.id.comment_comment_count)
            TextView commentCount;

            @BindView(R.id.comment_like_image_view)
            ImageView commentLikeImageView;

            @BindView(R.id.comment_comment_image_view)
            ImageView commentCommentImageView;

            public CommentViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class ProgressViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.progress)
            ProgressBar progressBar;

            ProgressViewHolder(View v) {
                super(v);
                ButterKnife.bind(this, v);
            }
        }

        private class PostListener implements View.OnClickListener {

            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.aty_post_title_avatar
                        || v.getId() == R.id.activity_avatar
                        || v.getId() == avatarId) {
                    String userID = (String) v.getTag();
                    Intent i = new Intent(mContext, AtyInfo.class);
                    i.putExtra(AtyInfo.ID_INTENT, userID);
                    mContext.startActivity(i);
                } else if (v.getId() == imageID) {
                    JSONObject json = (JSONObject) v.getTag();
                    Intent i = new Intent(mContext, AtyImage.class);
                    i.putExtra(AtyImage.INTENT_JSON, json.toString());
                    mContext.startActivity(i);
                    ((Activity) mContext).overridePendingTransition(0, 0);
                } else if (v.getId() == R.id.aty_post_title_like_image) {
                    likePost();
                } else if (v.getId() == R.id.aty_post_title_comment_post) {
                    showCommentToPostChatView();
                }

//                else if (v.getId() == R.id.comment_like_linear_layout) {
//                    likeComment(v);
//                }
//                } else if (v.getId() == R.id.comment_linear_layout) {
//                    PostComment postComment = (PostComment) v.getTag();
//                    showCommentToCommentChatView(postComment);
//                }
            }
        }
    }
}
