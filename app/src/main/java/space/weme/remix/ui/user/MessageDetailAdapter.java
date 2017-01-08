package space.weme.remix.ui.user;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import space.weme.remix.R;
import space.weme.remix.model.Message;
import space.weme.remix.ui.AtyImage;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.GridLayout;

/**
 * Created by Liujilong on 16/2/6.
 * liujilong.me@gmail.com
 */
public class MessageDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<Message> messageList;
    private View.OnClickListener mAvatarListener;
    private View.OnClickListener mReplyListener;
    private View.OnClickListener mImageListener;
    private String sendId;

    public MessageDetailAdapter(Context context, String id) {
        mContext = context;
        sendId = id;
        mAvatarListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendId = (String) v.getTag();
                Intent i = new Intent(mContext, AtyInfo.class);
                i.putExtra(AtyInfo.ID_INTENT, sendId);
                mContext.startActivity(i);
            }
        };
        mReplyListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sendId = (String) v.getTag();
                Intent i = new Intent(mContext, AtyMessageReply.class);
                i.putExtra(AtyMessageReply.INTENT_ID, sendId);
                i.putExtra(AtyMessageReply.INTENT_REPLY, true);
                ((Activity) mContext).startActivityForResult(i, AtyMessageDetail.REQUEST_CODE);
            }
        };
        mImageListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = (String) v.getTag();
                Intent i = new Intent(mContext, AtyImage.class);
                i.putExtra(AtyImage.URL_INTENT, url);
                mContext.startActivity(i);
                ((android.app.Activity) mContext).overridePendingTransition(0, 0);
            }
        };
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.aty_message_detail_cell, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        Message m = messageList.get(position);
        viewHolder.avatar.setImageURI(Uri.parse(StrUtils.thumForID(m.sendId)));
        viewHolder.avatar.setTag(m.sendId);
        viewHolder.avatar.setOnClickListener(mAvatarListener);
        viewHolder.tvName.setText(m.name);
        viewHolder.tvText.setText(m.text);
        viewHolder.tvSchool.setText(m.school);
        if (m.time != null) {
            viewHolder.tvTime.setText(StrUtils.timeTransfer(m.time));
        }
        if (!sendId.equals(m.sendId)) {
            viewHolder.ivReply.setVisibility(View.GONE);
        } else {
            viewHolder.ivReply.setVisibility(View.VISIBLE);
            viewHolder.ivReply.setTag(m.sendId);
            viewHolder.ivReply.setOnClickListener(mReplyListener);
        }
        viewHolder.glImages.setImageLists(m.images, mImageListener);
    }

    @Override
    public int getItemCount() {
        return messageList == null ? 0 : messageList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.aty_message_detail_cell_avatar)
        SimpleDraweeView avatar;

        @BindView(R.id.aty_message_detail_cell_name)
        TextView tvName;

        @BindView(R.id.aty_message_detail_cell_text)
        TextView tvText;

        @BindView(R.id.aty_message_detail_cell_school)
        TextView tvSchool;

        @BindView(R.id.aty_message_detail_cell_time)
        TextView tvTime;

        @BindView(R.id.aty_message_detail_cell_reply)
        ImageView ivReply;

        @BindView(R.id.aty_message_detail_cell_images)
        GridLayout glImages;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            glImages.setNumInRow(3);
        }
    }
}
