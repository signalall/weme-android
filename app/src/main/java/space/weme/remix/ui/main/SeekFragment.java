package space.weme.remix.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import space.weme.remix.R;
import space.weme.remix.ui.base.BaseFragment;
import space.weme.remix.ui.find.AtyDiscovery;
import space.weme.remix.ui.find.AtyDiscoveryFood;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.SeekPathView;

/**
 * Created by Liujilong on 2016/2/21.
 * liujilong.me@gmail.com
 */
public class SeekFragment extends BaseFragment {

    private static final String TAG = SeekFragment.class.getSimpleName();

    @BindView(R.id.seek_path)
    SeekPathView seekPathView;

    public static SeekFragment newInstance() {
        Bundle args = new Bundle();
        SeekFragment fragment = new SeekFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_seek, container, false);
        ButterKnife.bind(this, rootView);
        seekPathView.setOnFoodClickListener(() ->
                startActivity(new Intent(getActivity(), AtyDiscoveryFood.class)));
        seekPathView.setOnFriendClickListener(() ->
                startActivity(new Intent(getActivity(), AtyDiscovery.class)));
        SharedPreferences sp = getActivity().getSharedPreferences(StrUtils.SP_USER, Context.MODE_PRIVATE);
        String gender = sp.getString(StrUtils.SP_USER_GENDER, "");
        boolean boyOrGirl = gender.equals(getResources().getString(R.string.male));
        if (boyOrGirl) {
            seekPathView.setSeekingBoyFriend();
        } else {
            seekPathView.setSeekingGirlFriend();
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (seekPathView != null) {
            seekPathView.stopAnimation();
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }
}
