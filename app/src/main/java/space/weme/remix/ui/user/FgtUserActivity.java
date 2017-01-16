package space.weme.remix.ui.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.Activity;
import space.weme.remix.model.ResponseWrapper;
import space.weme.remix.service.ActivityService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.activity.ActivityDetailActivity;
import space.weme.remix.ui.base.BaseFragment;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 16/2/7.
 * liujilong.me@gmail.com
 */
public class FgtUserActivity extends BaseFragment {
    private static final String TAG = "FgtUserActivity";
    ActivityAdapter adapter;
    boolean isRefreshing = false;
    boolean isLoading = false;
    boolean canLoadMore = true;
    int curPage = 1;
    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = (Activity) v.getTag();
            Intent i = new Intent(getActivity(), ActivityDetailActivity.class);
            i.putExtra(ActivityDetailActivity.INTENT, activity.getId());
            getActivity().startActivity(i);
        }
    };
    private int pagerPage;
    private List<Activity> activityList;

    public static FgtUserActivity newInstance(int page) {
        FgtUserActivity f = new FgtUserActivity();
        Bundle bundle = new Bundle();
        bundle.putInt("page", page);
        f.setArguments(bundle);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fgt_user_activity, container, false);
        pagerPage = getArguments().getInt("page");
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.fgt_user_activity_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isRefreshing) {
                    Log.d(TAG, "ignore manually update!");
                } else {
                    fetchActivities(1);
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                    fetchActivities(curPage + 1);
                }
            }
        });

        activityList = new ArrayList<>();
        adapter = new ActivityAdapter(getActivity());
        adapter.setActivities(activityList);
        recyclerView.setAdapter(adapter);
        fetchActivities(1);
        return v;
    }

    private void fetchActivities(final int page) {
        isRefreshing = true;
        isLoading = true;

        ActivityService.GetActivity req = new ActivityService.GetActivity(StrUtils.token(), String.valueOf(page));
        Observable<ResponseWrapper<List<Activity>>> observable = null;
        switch (pagerPage) {
            case 0:
                observable = Services.activityService()
                        .getPublishActivity(req);
                break;
            case 1:
                observable = Services.activityService()
                        .getLikeActivity(req);
                break;
            case 2:
                observable = Services.activityService()
                        .getRegisterActivity(req);
                break;
        }
        if (observable == null)
            return;
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getActivity: " + resp.toString());
                    List<Activity> activities = resp.getResult();
                    isRefreshing = false;
                    isLoading = false;
                    if (activities == null) {
                        return;
                    }
                    if (page == 1) {
                        activityList.clear();
                    }
                    int previousCount = activityList.size();
                    if (activities.isEmpty()) {
                        canLoadMore = false;
                    }
                    activityList.addAll(activities);
                    if (page == 1) {
                        adapter.notifyDataSetChanged();
                    } else {
                        adapter.notifyItemRangeInserted(previousCount, activityList.size());
                    }
                }, ex -> {
                    Log.e(TAG, "getActivity: " + ex.getMessage());
                    Toast.makeText(getActivity(),
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected String tag() {
        return TAG;
    }

    class ActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<Activity> activities;
        Context mContext;

        public ActivityAdapter(Context context) {
            mContext = context;
        }

        public void setActivities(List<Activity> activities) {
            this.activities = activities;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.fgt_user_activity_cell, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            VH item = (VH) holder;
            Activity activity = activities.get(position);
            item.mTvTitle.setText(activity.getTitle());
            item.mAvatar.setImageURI(Uri.parse(StrUtils.thumForID(activity.getAuthorID() + "")));
            String count = activity.getSignNumber() + "/" + activity.getCapacity();
            item.mTvCount.setText(count);
            item.mTvTime.setText(activity.getTime());
            item.mTvLocation.setText(activity.getLocation());
            item.mTvStatus.setText(activity.getStatus());
            item.itemView.setTag(activity);
            item.itemView.setOnClickListener(listener);
        }

        @Override
        public int getItemCount() {
            return activities == null ? 0 : activities.size();
        }

        class VH extends RecyclerView.ViewHolder {

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

            @BindView(R.id.fgt_activity_item_status)
            TextView mTvStatus;

            public VH(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
