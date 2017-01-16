package space.weme.remix.ui.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.Constants;
import space.weme.remix.R;
import space.weme.remix.model.ActivityComment;
import space.weme.remix.model.ActivityDetail;
import space.weme.remix.service.ActivityService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.AtyImage;
import space.weme.remix.ui.base.SwipeActivity;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.GridLayout;

/**
 * Created by Joyce on 2017/1/16.
 */
public class ActivityCommentActivity extends SwipeActivity {

    private static final String TAG = ActivityCommentActivity.class.getSimpleName();
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.main_title)
    TextView mMainTitle;
    @BindView(R.id.chat_view_holder)
    LinearLayout mChatView;
    @BindView(R.id.activity_post_editor)
    EditText mEditText;
    @BindView(R.id.activity_post_commit)
    TextView mCommentText;
    @BindView(R.id.activity_post_add_image)
    ImageView mAddImage;
    private int mActivityId;
    private Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_comment);
        ButterKnife.bind(this);
        mActivityId = getIntent().getIntExtra("activityid", 0);

        mAdapter = new Adapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        loadActivityDetail();
    }

    @Override
    protected String tag() {
        return TAG;
    }


    @OnClick(R.id.activity_post_commit)
    public void onCommentToActivityClick() {
        // Todo
    }

    @OnClick(R.id.activity_post_add_image)
    public void onAddImageClick() {
        // Todo
    }

    private void updateView(ActivityDetail activityDetail) {
        mMainTitle.setText(activityDetail.getTitle());
        mAdapter.setTitle(activityDetail);
        mAdapter.notifyItemChanged(0);
    }

    private void updateView(List<ActivityComment> activityComments) {
        mAdapter.setComments(activityComments);
        mAdapter.notifyDataSetChanged();
    }

    private void showNetworkError() {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
    }

    private void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    private void loadActivityDetail() {
        Services.activityService()
                .getActivityDetail(new ActivityService.GetActivityDetail(
                        StrUtils.token(),
                        String.valueOf(mActivityId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getActivityDetail: " + resp.toString());
                    if (Constants.STATE_SUCCESSFUL.equals(resp.getState())) {
                        updateView(resp.getResult());
                        loadActivityComment();
                    } else {
                        showError(resp.getReason());
                    }
                }, ex -> {
                    Log.e(TAG, "getActivityDetail: " + ex.getMessage());
                    showNetworkError();
                });
    }

    private void loadActivityComment() {
        Services.activityService()
                .getActivityComment(new ActivityService.GetActivityCommennt(StrUtils.token(),
                        String.valueOf(mActivityId), "100"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                            Log.d(TAG, "getActivityComment: " + resp.toString());
                            if (Constants.STATE_SUCCESSFUL.equals(resp.getState())) {
                                updateView(resp.getResult());
                            } else {
                                showError(resp.getReason());
                            }
                        },
                        ex -> {
                            Log.e(TAG, "getActivityComment: " + ex.getMessage());
                            showNetworkError();
                        });
    }

    private void showCommentToActivityChatView() {
        mChatView.setVisibility(View.VISIBLE);
        mAddImage.setVisibility(View.VISIBLE);
        mEditText.setText("");
        mEditText.setHint("");
    }

    private void showCommentToCommentChatView(ActivityComment activityComment) {
        mChatView.setVisibility(View.VISIBLE);
        mAddImage.setVisibility(View.GONE);
        mEditText.setHint(getString(R.string.comment) + activityComment.getName() + ":");
        mEditText.setHint("");
    }

    private void likeActivity(ActivityDetail mActivityDetail) {
        // Todo
    }

    private void likeComment(ActivityComment activityComment) {
        // Todo
    }

    class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TITLE = 1;
        private static final int VIEW_ITEM = 2;
        private static final int VIEW_PROGRESS = 3;
        private final Context mContext;
        private ActivityDetail mActivityDetail;
        private List<ActivityComment> mComments;

        public Adapter(Context context) {
            mContext = context;
        }

        void setComments(List<ActivityComment> comments) {
            this.mComments = comments;
        }

        void setTitle(ActivityDetail activityDetail) {
            mActivityDetail = activityDetail;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            if (viewType == VIEW_TITLE) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_activity_comment_title, parent, false);
                viewHolder = new TitleViewHolder(v);
            } else if (viewType == VIEW_ITEM) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_comment_item, parent, false);
                viewHolder = new CommentViewHolder(v);
            } else {
                View v = LayoutInflater.from(mContext).inflate(R.layout.list_item_progress, parent, false);
                viewHolder = new ProgressViewHolder(v);
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TitleViewHolder) {
                if (mActivityDetail != null) {
                    TitleViewHolder viewHolder = (TitleViewHolder) holder;
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
                    roundingParams.setRoundAsCircle(true);
                    viewHolder.mAvatar.getHierarchy().setRoundingParams(roundingParams);
                    viewHolder.mAvatar.setImageURI(Uri.parse(StrUtils.thumForID(String.valueOf(mActivityDetail.getAuthorid()))));
                    viewHolder.mName.setText(mActivityDetail.getAuthor());
                    viewHolder.mUniversity.setText(mActivityDetail.getSchool());
                    viewHolder.mDateTime.setText(StrUtils.timeTransfer(mActivityDetail.getTime()));
                    viewHolder.mContent.setText(mActivityDetail.getDetail());


                    // Like and comment
                    boolean likeFlag = !"0".equals(mActivityDetail.getFlag());
                    viewHolder.commentLikeImageView.setImageResource(likeFlag ? R.mipmap.like_on : R.mipmap.like_off);
                    viewHolder.commentLikeImageView.setOnClickListener((v) -> likeActivity(mActivityDetail));
                    viewHolder.commentCommentImageView.setOnClickListener(v -> showCommentToActivityChatView());
                }
            } else if (holder instanceof CommentViewHolder) {
                ActivityComment activityComment = mComments.get(position - 1);
                if (activityComment == null) return;
                CommentViewHolder viewHolder = (CommentViewHolder) holder;
                viewHolder.avatarDraw.setImageURI(Uri.parse(StrUtils.thumForID(String.valueOf(activityComment.getUserId()))));
                viewHolder.avatarDraw.setTag(activityComment.getUserId());
                viewHolder.avatarDraw.setOnClickListener((v) -> showCommentToCommentChatView(activityComment));
                viewHolder.tvName.setText(activityComment.getName());
                viewHolder.tvUniversity.setText(activityComment.getSchool());
                viewHolder.tvTime.setText(StrUtils.timeTransfer(activityComment.getTimestamp()));
                viewHolder.tvContent.setText(activityComment.getBody());
                viewHolder.llReplyList.removeAllViews();
                viewHolder.llReplyList.setVisibility(activityComment.getReply().size() == 0 ? View.GONE : View.VISIBLE);
                viewHolder.imagesGridLayout.removeAllViews();

                // Like and comment
                viewHolder.commentLikeCount.setText(String.valueOf(activityComment.getLikeNumber()));
                viewHolder.commentCommentCount.setText(String.valueOf(activityComment.getCommentNumber()));
                boolean likeFlag = !"0".equals(activityComment.getFlag());
                viewHolder.commentLikeImageView.setImageResource(likeFlag ? R.mipmap.like_on : R.mipmap.like_off);
                viewHolder.commentLikeImageView.setOnClickListener((v) -> likeComment(activityComment));
                viewHolder.commentLikeCount.setOnClickListener(v -> likeComment(activityComment));
                viewHolder.commentCommentImageView.setOnClickListener(v -> showCommentToCommentChatView(activityComment));
                viewHolder.commentCommentCount.setOnClickListener(v -> showCommentToCommentChatView(activityComment));


                if (activityComment.getImage().size() <= 1) {
                    viewHolder.imagesGridLayout.setNumInRow(1);
                } else if (activityComment.getImage().size() <= 4) {
                    viewHolder.imagesGridLayout.setNumInRow(2);
                } else {
                    viewHolder.imagesGridLayout.setNumInRow(3);
                }
                if (activityComment.getImage() == null || activityComment.getImage().size() == 0) {
                    viewHolder.imagesGridLayout.setVisibility(View.GONE);
                } else {
                    viewHolder.imagesGridLayout.setVisibility(View.VISIBLE);
                    for (int i = 0; i < activityComment.getThumbnail().size(); i++) {
                        SimpleDraweeView drawView = new SimpleDraweeView(mContext);
                        drawView.setImageURI(Uri.parse(activityComment.getImage().get(i)));
                        // drawView.setId(imageID);
                        // drawView.setOnClickListener(mListener);
                        viewHolder.imagesGridLayout.addView(drawView);
                        try {
                            JSONObject j = new JSONObject();
                            JSONArray array = new JSONArray(activityComment.getImage());
                            j.put(AtyImage.KEY_ARRAY, array);
                            j.put(AtyImage.KEY_INDEX, i);
                            drawView.setTag(j);
                        } catch (JSONException e) {
                            // ignore
                        }
                    }
                }

            } else if (holder instanceof ProgressViewHolder) {
                ProgressViewHolder viewHolder = (ProgressViewHolder) holder;
            }
        }


        @Override
        public int getItemCount() {
            return 1 + (mComments == null ? 0 : mComments.size());
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

        class TitleViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.activity_avatar)
            SimpleDraweeView mAvatar;

            @BindView(R.id.activity_name)
            TextView mName;

            @BindView(R.id.activity_school)
            TextView mUniversity;

            @BindView(R.id.activity_time)
            TextView mDateTime;

            @BindView(R.id.activity_body)
            TextView mContent;

            @BindView(R.id.activity_like_image_view)
            ImageView commentLikeImageView;

            @BindView(R.id.activity_comment_image_view)
            ImageView commentCommentImageView;

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

            @BindView(R.id.comment_like_image_view)
            ImageView commentLikeImageView;

            @BindView(R.id.comment_like_count)
            TextView commentLikeCount;

            @BindView(R.id.comment_comment_image_view)
            ImageView commentCommentImageView;

            @BindView(R.id.comment_comment_count)
            TextView commentCommentCount;


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
    }
}
