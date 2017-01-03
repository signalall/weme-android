package space.weme.remix.ui.community;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.Comment;
import space.weme.remix.model.Post;
import space.weme.remix.model.PostComment;
import space.weme.remix.service.PostService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.AtyImage;
import space.weme.remix.ui.user.AtyInfo;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.GridLayout;

/**
 * Created by Liujilong on 2016/1/30.
 * liujilong.me@gmail.com
 */
public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context mContext;
    AtyPost aty;


    private static final String TAG = "PostAdapter";

    Post mPost;
    List<PostComment> mPostCommentList;

    View.OnClickListener mListener;

    private static final int VIEW_TITLE = 1;
    private static final int VIEW_ITEM = 2;
    private static final int VIEW_PROGRESS = 3;

    private int imageID = StrUtils.generateViewId();
    private int avatarId = StrUtils.generateViewId();


    PostAdapter(Context context) {
        mContext = context;
        aty = (AtyPost) context;
        mListener = new PostListener();
    }

    void setPost(Post post) {
        mPost = post;
    }

    void setReplyList(List<PostComment> list) {
        mPostCommentList = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        if (viewType == VIEW_TITLE) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.aty_post_title, parent, false);
            holder = new PostViewHolder(v);
        } else if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.aty_post_reply, parent, false);
            holder = new ItemViewHolder(v);
        } else {
            View v = LayoutInflater.from(mContext).inflate(R.layout.aty_post_progress, parent, false);
            holder = new ProgressViewHolder(v);
        }
        return holder;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostViewHolder) {
            if (mPost == null) {
                return;
            }
            PostViewHolder viewHolder = (PostViewHolder) holder;
            viewHolder.avatarDraw.setImageURI(Uri.parse(StrUtils.thumForID(mPost.getUserId())));
            viewHolder.avatarDraw.setTag(mPost.getUserId());
            viewHolder.avatarDraw.setOnClickListener(mListener);
            viewHolder.tvName.setText(mPost.getName());
            viewHolder.tvUniversity.setText(mPost.getSchool());
            viewHolder.tvTime.setText(StrUtils.timeTransfer(mPost.getTimestamp()));
            viewHolder.tvTitle.setText(mPost.getTitle());
            viewHolder.tvContent.setText(mPost.getContent());
            viewHolder.imagesGridLayout.removeAllViews();
            if (mPost.thumbnailUrl.size() <= 1) {
                viewHolder.imagesGridLayout.setNumInRow(1);
            } else if (mPost.thumbnailUrl.size() <= 4) {
                viewHolder.imagesGridLayout.setNumInRow(2);
            } else {
                viewHolder.imagesGridLayout.setNumInRow(3);
            }
            for (int i = 0; i < mPost.thumbnailUrl.size(); i++) {
                String thumbUrl = mPost.imageUrl.get(i);
                SimpleDraweeView image = new SimpleDraweeView(mContext);
                viewHolder.imagesGridLayout.addView(image);
                image.setImageURI(Uri.parse(thumbUrl));
                image.setId(imageID);
                image.setOnClickListener(mListener);
                try {
                    JSONObject j = new JSONObject();
                    JSONArray array = new JSONArray(mPost.imageUrl);
                    j.put(AtyImage.KEY_INDEX, i);
                    j.put(AtyImage.KEY_ARRAY, array);
                    image.setTag(j);
                } catch (JSONException e) {
                    // ignore
                }
            }
            viewHolder.tvLikeNumber.setText(mPost.likeNumber);
            viewHolder.tvCommit.setText(mPost.commentNumber);
            if (mPost.flag.equals("0")) {
                viewHolder.ivLike.setImageResource(R.mipmap.like_off);
                viewHolder.likeLayout.setOnClickListener(mListener);
            } else {
                viewHolder.ivLike.setImageResource(R.mipmap.like_on);
            }
            viewHolder.commitLayout.setOnClickListener(mListener);
            viewHolder.llLikePeoples.removeAllViews();
            for (int id : mPost.likeUserIds) {
                SimpleDraweeView avatar = (SimpleDraweeView) LayoutInflater.from(mContext).
                        inflate(R.layout.aty_post_avatar, viewHolder.llLikePeoples, false);
                viewHolder.llLikePeoples.addView(avatar);
                avatar.setImageURI(Uri.parse(StrUtils.thumForID(Integer.toString(id))));
                avatar.setTag(String.format("%d", id));
                avatar.setId(avatarId);
                avatar.setOnClickListener(mListener);
                // todo show more
            }
        } else if (holder instanceof ItemViewHolder) {
            final PostComment postComment = mPostCommentList.get(position - 1);
            if (postComment == null) {
                return;
            }
            final ItemViewHolder item = (ItemViewHolder) holder;
            item.avatarDraw.setImageURI(Uri.parse(StrUtils.thumForID(postComment.getUserId())));
            item.avatarDraw.setTag(postComment.getUserId());
            item.avatarDraw.setOnClickListener(mListener);
            item.tvName.setText(postComment.getName());
            item.tvUniversity.setText(postComment.getSchool());
            item.tvTime.setText(StrUtils.timeTransfer(postComment.getTimestamp()));
            item.tvContent.setText(postComment.getContent());
            item.tvLike.setText(String.format("%d", postComment.getLikeCount()));
            item.tvCommit.setText(String.format("%d", postComment.getCommentCount()));
            if (postComment.getFlag().equals("0")) {
                item.ivLike.setImageResource(R.mipmap.like_off);
                item.llLike.setTag(position);
                item.llLike.setOnClickListener(mListener);
            } else {
                item.ivLike.setImageResource(R.mipmap.like_on);
            }
            item.llCommit.setTag(postComment);
            item.llCommit.setOnClickListener(mListener);
            item.llReplyList.removeAllViews();
            item.llReplyList.setVisibility(postComment.getComments().size() == 0 ? View.GONE : View.VISIBLE);
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
                item.llReplyList.addView(tv);
            }
            item.imagesGridLayout.removeAllViews();

            if (postComment.getImage().size() <= 1) {
                item.imagesGridLayout.setNumInRow(1);
            } else if (postComment.getImage().size() <= 4) {
                item.imagesGridLayout.setNumInRow(2);
            } else {
                item.imagesGridLayout.setNumInRow(3);
            }
            if (postComment.getImage() == null || postComment.getImage().size() == 0) {
                item.imagesGridLayout.setVisibility(View.GONE);
            } else {
                item.imagesGridLayout.setVisibility(View.VISIBLE);
                for (int i = 0; i < postComment.getThumbnail().size(); i++) {
                    SimpleDraweeView drawView = new SimpleDraweeView(mContext);
                    drawView.setImageURI(Uri.parse(postComment.getImage().get(i)));
                    drawView.setId(imageID);
                    drawView.setOnClickListener(mListener);
                    item.imagesGridLayout.addView(drawView);
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
        } else if (mPostCommentList.get(position - 1) != null) {
            return VIEW_ITEM;
        } else {
            return VIEW_PROGRESS;
        }
    }

    @Override
    public int getItemCount() {
        return 1 + (mPostCommentList == null ? 0 : mPostCommentList.size());
    }


    class PostViewHolder extends RecyclerView.ViewHolder {

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

        @BindView(R.id.aty_post_title_like_number)
        TextView tvLikeNumber; // show like number

        @BindView(R.id.aty_post_title_like_image)
        ImageView ivLike;

        // for like clickListener
        @BindView(R.id.aty_post_title_like_layout)
        LinearLayout likeLayout;

        @BindView(R.id.aty_post_title_reply_number)
        TextView tvCommit; // show commit number

        @BindView(R.id.aty_post_title_reply_layout)
        LinearLayout commitLayout; // for commit listener

        @BindView(R.id.aty_post_title_like_people)
        LinearLayout llLikePeoples; // liked people

        public PostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.aty_post_reply_avatar)
        SimpleDraweeView avatarDraw;

        @BindView(R.id.aty_post_reply_name)
        TextView tvName;

        @BindView(R.id.aty_post_reply_university)
        TextView tvUniversity;

        @BindView(R.id.aty_post_reply_time)
        TextView tvTime;

        @BindView(R.id.aty_post_reply_content)
        TextView tvContent;

        @BindView(R.id.aty_post_reply_reply_list)
        LinearLayout llReplyList;

        @BindView(R.id.aty_post_reply_images)
        GridLayout imagesGridLayout;

        @BindView(R.id.aty_post_reply_like_number)
        TextView tvLike;

        @BindView(R.id.aty_post_reply_comment_number)
        TextView tvCommit;

        @BindView(R.id.aty_post_reply_like_layout)
        LinearLayout llLike;

        @BindView(R.id.aty_post_reply_commit_layout)
        LinearLayout llCommit;

        @BindView(R.id.aty_post_reply_like_image)
        ImageView ivLike;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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

    private class PostListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.aty_post_title_avatar
                    || v.getId() == R.id.aty_post_reply_avatar
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
            } else if (v.getId() == R.id.aty_post_title_like_layout) {
                likePost();
            } else if (v.getId() == R.id.aty_post_title_reply_layout) {
                aty.commentToPost();
            } else if (v.getId() == R.id.aty_post_reply_like_layout) {
                likeComment(v);
            } else if (v.getId() == R.id.aty_post_reply_commit_layout) {
                PostComment postComment = (PostComment) v.getTag();
                aty.commentToComment(postComment);
            }
        }
    }

    private void likePost() {
        String userId = StrUtils.id();
        if (!mPost.likeUserIds.contains(userId)) { // Like
            Services.postService()
                    .likePost(new PostService.LikePost(StrUtils.token(), mPost.getPostId()))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resp -> {
                        Log.d(TAG, "likePost: " + resp.toString());
                        mPost.likeNumber = (Integer.parseInt(mPost.likeNumber) + 1) + "";
                        mPost.likeUserIds.add(Integer.parseInt(StrUtils.id()));
                        mPost.flag = "1";
                        notifyItemChanged(0);
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
                        mPost.likeNumber = (Integer.parseInt(mPost.likeNumber) - 1) + "";
                        mPost.likeUserIds.remove(Integer.parseInt(StrUtils.id()));
                        mPost.flag = "0";
                        notifyItemChanged(0);
                    }, ex -> {
                        Log.e(TAG, "unlikePost: " + ex.getMessage());
                    });
        }
    }

    private void likeComment(final View v) {
        String userId = StrUtils.id();
        final int position = (int) v.getTag();
        final PostComment postComment = mPostCommentList.get(position - 1);
        Services.postService()
                .likeComment(new PostService.LikeComment(StrUtils.token(), postComment.getId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "likeComment: " + resp.toString());
                    postComment.setFlag("1");
                    postComment.setLikeCount(postComment.getLikeCount() + 1);
                    notifyItemChanged(position);
                }, ex -> {
                    Log.e(TAG, "likeComment: " + ex.getMessage());
                });
    }
}
