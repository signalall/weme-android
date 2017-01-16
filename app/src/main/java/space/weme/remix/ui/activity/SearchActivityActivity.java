package space.weme.remix.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.Activity;
import space.weme.remix.service.ActivityService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.base.SwipeActivity;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.StrUtils;

public class SearchActivityActivity extends SwipeActivity {

    private static final String TAG = SearchActivityActivity.class.getSimpleName();

    @BindView(R.id.edit_search)
    EditText editSearch;

    @BindView(R.id.txt_search_cancel)
    TextView txtCancel;

    @BindView(R.id.aty_search_activity)
    RecyclerView recyclerSearch;

    private List<Activity> activityList = new ArrayList<>();

    private searchAdapter adapter = new searchAdapter();

    @OnClick(R.id.txt_search_cancel)
    public void onSearchCancel() {
        onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_activity);
        ButterKnife.bind(this);
        recyclerSearch.setItemAnimator(new DefaultItemAnimator());
        recyclerSearch.setLayoutManager(new LinearLayoutManager(SearchActivityActivity.this));
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str != null && !str.equals(""))
                    searchActivity(str);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    void searchActivity(String text) {
        Services.activityService()
                .searchActivity(new ActivityService.SearchActivity(StrUtils.token(), text))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if ("successful".equals(resp.getState())) {
                        activityList.clear();
                        activityList.addAll(resp.getResult());
                        recyclerSearch.setAdapter(adapter);
                        adapter.setList(activityList);
                        adapter.notifyDataSetChanged();
                    }
                }, ex -> {
                    Log.e(TAG, "searchActivity: " + ex.getMessage());
                });
    }

    protected String tag() {
        return TAG;
    }

    class searchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<Activity> mList;

        public void setList(List<Activity> list) {
            mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            View v = LayoutInflater.from(SearchActivityActivity.this).inflate(R.layout.list_item_activity_item, parent, false);
            vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Activity activity = mList.get(position);
            ViewHolder item = (ViewHolder) holder;
            item.mTvTitle.setText(activity.getTitle());
            item.mAvatar.setImageURI(Uri.parse(StrUtils.thumForID(activity.getAuthorID() + "")));
            String count = activity.getSignNumber() + "/" + activity.getCapacity();
            item.mTvCount.setText(count);
            item.mTvTime.setText(activity.getTime());
            item.mTvLocation.setText(activity.getLocation());
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.fgt_activity_item_image)
            SimpleDraweeView mAvatar;

            @BindView(R.id.fgt_activity_item_title)
            TextView mTvTitle;

            @BindView(R.id.fgt_activity_item_count)
            TextView mTvCount;

            @BindView(R.id.fgt_activity_item_time)
            TextView mTvTime;

            @BindView(R.id.fgt_activity_item_location)
            TextView mTvLocation;

            public ViewHolder(View itemView) {
                super(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detail = new Intent(SearchActivityActivity.this, ActivityDetailActivity.class);
                        detail.putExtra("activityid", mList.get(getAdapterPosition()).getId());
                        LogUtils.e(TAG, "id:" + mList.get(getAdapterPosition()).getId());
                        startActivity(detail);
                    }
                });

                ButterKnife.bind(this, itemView);

                RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
                roundingParams.setRoundAsCircle(true);
                mAvatar.getHierarchy().setRoundingParams(roundingParams);
            }
        }
    }
}
