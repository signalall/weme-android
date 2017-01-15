package space.weme.remix.ui.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.TopTopic;
import space.weme.remix.model.Topic;
import space.weme.remix.service.Services;
import space.weme.remix.service.TopicService;
import space.weme.remix.ui.base.BaseFragment;
import space.weme.remix.ui.community.AtyPost;
import space.weme.remix.ui.community.AtyTopic;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.PageIndicator;

/**
 * Created by Liujilong on 16/1/24.
 * liujilong.me@gmail.com
 */
public class CommunityFragment extends BaseFragment {

    private static final String TAG = CommunityFragment.class.getSimpleName();

    @BindView(R.id.fgt_community_grid_layout)
    GridLayout mGridLayout;

    @BindView(R.id.top_pager_view)
    ViewPager mTopTopicViewPager;

    @BindView(R.id.top_pager_indicator)
    PageIndicator mIndicator;

    private TopTopicListAdapter mTopicListAdapter;

    private View.OnClickListener mClickListener;

    private boolean isRefreshing = false;

    private List<Subscription> subscriptions = new ArrayList<>();

    public static CommunityFragment newInstance() {
        Bundle args = new Bundle();
        CommunityFragment fragment = new CommunityFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_community, container, false);
        ButterKnife.bind(this, rootView);
        View v = rootView.findViewById(R.id.top_container);
        ViewGroup.LayoutParams params = v.getLayoutParams();
        params.height = DimensionUtils.getDisplay().widthPixels / 2;
        v.setLayoutParams(params);

        mTopicListAdapter = new TopTopicListAdapter(getActivity());
        mTopicListAdapter.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = (int) v.getTag();
                Intent i = new Intent(getActivity(), AtyPost.class);
                i.putExtra(AtyPost.POST_INTENT, id + "");
                i.putExtra(AtyPost.THEME_INTENT, "");
                startActivity(i);
            }
        });
        mTopTopicViewPager.setAdapter(mTopicListAdapter);
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AtyTopic.class);
                i.putExtra(AtyTopic.TOPIC_ID, (String) v.getTag());
                startActivity(i);
            }
        };

        //
        loadTopTopicList();
        loadTopicList();
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    private void loadTopTopicList() {
        Subscription sub = Services.topicService()
                .getTopTopicList(new TopicService.GetTopTopicList(StrUtils.token()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getTopTopicList: " + resp.toString());
                    isRefreshing = false;
                    if ("successful".equals(resp.getState())) {
                        mTopicListAdapter.setTopTopicList(resp.getResult());
                        mTopicListAdapter.notifyDataSetChanged();
                        mIndicator.setViewPager(mTopTopicViewPager);
                    } else {
                        Toast.makeText(getActivity(),
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    isRefreshing = false;
                    Toast.makeText(getActivity(),
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
        subscriptions.add(sub);
    }

    private void loadTopicList() {
        isRefreshing = true;
        Subscription sub = Services.topicService()
                .getTopicList(new TopicService.GetTopicList(StrUtils.token()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getTopicList: " + resp.toString());
                    isRefreshing = false;
                    if ("successful".equals(resp.getState())) {
                        addGridViews(mGridLayout, resp.getResult());
                    } else {
                        Toast.makeText(getActivity(),
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "getTopicList: " + ex.toString());
                    isRefreshing = false;
                    Toast.makeText(getActivity(),
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
        subscriptions.add(sub);
    }

    private void addGridViews(GridLayout grid, List<Topic> lists) {
        for (int i = 0; i < lists.size(); i++) {
            Topic topic = lists.get(i);
            RelativeLayout convertView = (RelativeLayout) LayoutInflater.from(getActivity()).inflate(
                    R.layout.fgt_community_topic_cell, grid, false);
            TextView tvTheme = (TextView) convertView.findViewById(R.id.community_topic_cell_theme);
            TextView tvNote = (TextView) convertView.findViewById(R.id.community_topic_cell_note);
            TextView tvNumber = (TextView) convertView.findViewById(R.id.number);
            SimpleDraweeView ivImage = (SimpleDraweeView) convertView.findViewById(R.id.community_topic_cell_image);
            tvTheme.setText(topic.theme);
            tvNote.setText(topic.note);
            String text = topic.number + "";
            tvNumber.setText(text);
            ivImage.setImageURI(Uri.parse(topic.imageUrl));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(i / 3), GridLayout.spec(i % 3));
            params.width = (grid.getWidth() / grid.getColumnCount()) - params.rightMargin - params.leftMargin;
            RelativeLayout.LayoutParams ivParams = (RelativeLayout.LayoutParams) ivImage.getLayoutParams();
            ivParams.width = params.width;
            ivParams.height = ivParams.width / 2;
            ivImage.setLayoutParams(ivParams);
            convertView.setTag(topic.id);
            convertView.setOnClickListener(mClickListener);
            grid.addView(convertView, params);
        }
    }

    static class TopTopicListAdapter extends PagerAdapter {
        List<TopTopic> mTopTopics;
        Context context;
        View.OnClickListener mListener;

        public TopTopicListAdapter(Context context) {
            this.context = context;
        }

        public void setTopTopicList(List<TopTopic> infoList) {
            this.mTopTopics = infoList;
        }

        public void setListener(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public int getCount() {
            return mTopTopics == null ? 0 : mTopTopics.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            SimpleDraweeView image = new SimpleDraweeView(context);
            Uri uri = Uri.parse(mTopTopics.get(position).getUrl());
            image.setImageURI(uri);
            image.setTag(mTopTopics.get(position).getId());
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
