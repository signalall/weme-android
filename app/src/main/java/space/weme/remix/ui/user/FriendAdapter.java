package space.weme.remix.ui.user;

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
import space.weme.remix.model.FriendData;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 16/2/5.
 * liujilong.me@gmail.com
 */
public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    View.OnClickListener mCellListener;

    public FriendAdapter(Context context) {
        mContext = context;
        mCellListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = (String) v.getTag();
                Intent i = new Intent(mContext, AtyInfo.class);
                i.putExtra(AtyInfo.ID_INTENT, id);
                mContext.startActivity(i);
            }
        };
    }

    List<FriendData> list;

    public void setList(List<FriendData> list) {
        this.list = list;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.aty_friend_cell, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UserViewHolder h = (UserViewHolder) holder;
        FriendData f = list.get(position);
        h.avatar.setImageURI(Uri.parse(StrUtils.thumForID(f.id)));
        h.name.setText(f.name);
        h.school.setText(f.school);
        boolean isBoy = f.gender.equals(mContext.getResources().getString(R.string.boy));
        h.gender.setImageResource(isBoy ? R.mipmap.boy : R.mipmap.girl);
        h.itemView.setTag(f.id);
        h.itemView.setOnClickListener(mCellListener);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.aty_friend_cell_avatar)
        SimpleDraweeView avatar;

        @BindView(R.id.aty_friend_cell_name)
        TextView name;

        @BindView(R.id.aty_friend_cell_gender)
        ImageView gender;

        @BindView(R.id.aty_friend_cell_school)
        TextView school;

        public UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
