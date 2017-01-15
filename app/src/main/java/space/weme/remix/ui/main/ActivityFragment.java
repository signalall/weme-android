package space.weme.remix.ui.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.Activity;
import space.weme.remix.model.TopActivity;
import space.weme.remix.service.ActivityService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.aty.AtyActivityDetail;
import space.weme.remix.ui.base.BaseFragment;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.PageIndicator;

/**
 * Created by Liujilong on 16/1/24.
 * liujilong.me@gmail.com
 */
public class ActivityFragment extends BaseFragment {

    private static final String TAG = "ActivityFragment";

    // views
    @BindView(R.id.fgt_activity_swipe_layout)
    SwipeRefreshLayout mSwipeLayout;

    @BindView(R.id.fgt_activity_recycler_view)
    RecyclerView mActivityListRecyclerView;

    private TopActivityListAdapter mTopActivityListAdapter;
    private ActivityListAdapter mActivityListAdapter;

    // data
    private int pageNumber = 0;
    private boolean isRefreshing = false;
    private boolean isLoadingActivity = false;
    private boolean isLoadingTopActivity = false;
    private boolean morePages = true;

    public static ActivityFragment newInstance() {
        Bundle args = new Bundle();
        ActivityFragment fragment = new ActivityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_activity, container, false);
        ButterKnife.bind(this, rootView);
        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing) {
                    if (!isLoadingTopActivity)
                        loadTopActivity();
                    if (!isLoadingActivity && morePages)
                        loadActivity();
                }
            }
        });
        mActivityListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mActivityListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = recyclerView.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (!isLoadingActivity && (totalItemCount - visibleItemCount) <= (firstVisibleItem + 2) && morePages) {
                    loadActivity();
                }
            }
        });
        mTopActivityListAdapter = new TopActivityListAdapter();
        mTopActivityListAdapter.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = (int) v.getTag();
                Intent intent = new Intent(getActivity(), AtyActivityDetail.class);
                intent.putExtra("activityid", id);
                startActivity(intent);
            }
        });
        mActivityListAdapter = new ActivityListAdapter();
        mActivityListRecyclerView.setAdapter(mActivityListAdapter);

        //
        pageNumber = 0;
        loadTopActivity();
        loadActivity();
        return rootView;
    }

    private void checkRefreshing() {
        if (!isLoadingActivity && !isLoadingTopActivity) {
            isRefreshing = false;
            mSwipeLayout.setRefreshing(false);
        }
    }

    private void loadTopActivity() {
        isRefreshing = true;
        isLoadingTopActivity = true;
        Services.activityService()
                .getTopActivity(new ActivityService.GetTopActivity(StrUtils.token()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getTopActivity: " + resp.toString());
                    isLoadingTopActivity = false;
                    checkRefreshing();
                    if ("successful".equals(resp.getState())) {
                        mTopActivityListAdapter.setTopActivityList(resp.getResult());
                        mTopActivityListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(),
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "getTopActivity: " + ex.getMessage());
                    isLoadingTopActivity = false;
                    checkRefreshing();
                    Toast.makeText(getActivity(),
                            R.string.network_error,
                            Toast.LENGTH_SHORT)
                            .show();
                });
    }

    private void loadActivity() {
        morePages = true;
        isLoadingActivity = true;
        isRefreshing = true;
        Services.activityService()
                .getActivityInfo(new ActivityService.GetActivityInfo(StrUtils.token(),
                        String.valueOf(pageNumber + 1)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getActivityInfo: " + resp.toString());
                    isLoadingActivity = false;
                    checkRefreshing();
                    if ("successful".equals(resp.getState())) {
                        pageNumber = resp.getPages();
                        List<Activity> activities = resp.getResult();
                        if (activities == null || activities.isEmpty()) {
                            morePages = false;
                            return;
                        }
                        mActivityListAdapter.setActivityList(activities);
                        mActivityListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(),
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "getActivityInfo: " + ex.getMessage());
                    isLoadingActivity = false;
                    checkRefreshing();
                    Toast.makeText(getActivity(),
                            R.string.network_error,
                            Toast.LENGTH_SHORT)
                            .show();
                });
    }

    @Override
    protected String tag() {
        return TAG;
    }

    class ActivityListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int TYPE_TOP = 1;
        static final int TYPE_ACTIVITY = 2;
        List<Activity> mActivityList;

        void setActivityList(List<Activity> activityList) {
            this.mActivityList = activityList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder vh;
            if (viewType == TYPE_TOP) {
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.top_pager, parent, false);
                ViewGroup.LayoutParams params = v.getLayoutParams();
                params.height = DimensionUtils.getDisplay().widthPixels / 2;
                v.setLayoutParams(params);
                vh = new TopViewHolder(v);
            } else {
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.fgt_activity_item, parent, false);
                vh = new ItemViewHolder(v);
            }
            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TopViewHolder) {
                TopViewHolder top = (TopViewHolder) holder;
                top.mViewPager.setAdapter(mTopActivityListAdapter);
                top.mIndicator.setViewPager(top.mViewPager);
            } else {
                Activity activity = mActivityList.get(position - 1);
                ItemViewHolder item = (ItemViewHolder) holder;
                item.mTvTitle.setText(activity.getTitle());
                item.mAvatar.setImageURI(Uri.parse(StrUtils.thumForID(activity.getAuthorID() + "")));
                String count = activity.getSignNumber() + "/" + activity.getCapacity();
                item.mTvCount.setText(count);
                item.mTvTime.setText(activity.getTime());
                item.mTvLocation.setText(activity.getLocation());
                item.mTimeState.setText(activity.getTimeState());
                if (activity.getTimeState().equals("已结束")) {
                    item.mTimeState.setTextColor(0xffc8c8dc);
                } else {
                    item.mTimeState.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                }
                int cc = Integer.parseInt(activity.getTop());
                if (cc != 0) {
                    item.mTopImage.setVisibility(View.VISIBLE);
                } else {
                    item.mTopImage.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mActivityList == null ? 1 : 1 + mActivityList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? TYPE_TOP : TYPE_ACTIVITY;
        }

        class TopViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.top_pager_view)
            ViewPager mViewPager;

            @BindView(R.id.top_pager_indicator)
            PageIndicator mIndicator;

            TopViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

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

            @BindView(R.id.fgt_activity_item_time_state)
            TextView mTimeState;

            @BindView(R.id.fgt_activity_item_top_image)
            ImageView mTopImage;

            ItemViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detail = new Intent(getActivity(), AtyActivityDetail.class);
                        detail.putExtra(AtyActivityDetail.INTENT, mActivityList.get(getAdapterPosition() - 1).getId());
                        LogUtils.d(TAG, "id:" + mActivityList.get(getAdapterPosition() - 1).getId());
                        startActivity(detail);
                    }
                });
                RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
                roundingParams.setRoundAsCircle(true);
                mAvatar.getHierarchy().setRoundingParams(roundingParams);
                mTopImage.setRotation(45);
            }
        }
    }

    class TopActivityListAdapter extends PagerAdapter {
        List<TopActivity> topActivityList;
        View.OnClickListener mListener;

        void setTopActivityList(List<TopActivity> topActivityList) {
            this.topActivityList = topActivityList;
        }

        public void setListener(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public int getCount() {
            return topActivityList == null ? 0 : topActivityList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SimpleDraweeView image = new SimpleDraweeView(getActivity());
            Uri uri = Uri.parse(topActivityList.get(position).getUrl());
            image.setImageURI(uri);
            image.setTag(topActivityList.get(position).getId());
            image.setOnClickListener(mListener);
            container.addView(image);
            return image;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
