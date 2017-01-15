package space.weme.remix.ui.main;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import space.weme.remix.R;
import space.weme.remix.ui.aty.AtyPublicActivity;
import space.weme.remix.ui.aty.AtySearchActivity;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.ui.intro.AtyLogin;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.UpdateUtils;
import space.weme.remix.widgt.TabItem;

/**
 * Created by Liujilong on 16/1/24.
 * liujilong.me@gmail.com
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String INTENT_LOGOUT = "intent_lougout";
    public static final String INTENT_UPDATE = "intent_update";

    private static final int PAGE_COUNT = 4;

    private int[] mTitleTexts = new int[]{
            R.string.activity,
            R.string.community,
            R.string.find,
            R.string.me
    };

    @BindViews({R.id.main_item_activity,
            R.id.main_item_community,
            R.id.main_item_find,
            R.id.main_item_me
    })
    List<TabItem> mTabItems;

    @BindView(R.id.main_pager)
    ViewPager mViewPager;

    @BindView(R.id.main_title)
    TextView mTitleTExtView;

    @BindView(R.id.more_action)
    ImageView mMoreImageView;

    @BindView(R.id.whole_layout)
    ViewGroup mViewGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(INTENT_LOGOUT, false)) {
            Intent i = new Intent(MainActivity.this, AtyLogin.class);
            startActivity(i);
            finish();
            return;
        }
        if (getIntent().getBooleanExtra(INTENT_UPDATE, false)) {
            UpdateUtils.checkUpdate(MainActivity.this);
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupViews();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @OnClick(R.id.more_action)
    public void onMoreClick() {
        Dialog dialog = new Dialog(MainActivity.this, R.style.DialogMain);
        View content = LayoutInflater.from(MainActivity.this).inflate(R.layout.main_menu, mViewGroup, false);
        content.findViewById(R.id.action_search).setOnClickListener(v -> {
            Intent search = new Intent(MainActivity.this, AtySearchActivity.class);
            startActivity(search);
            dialog.dismiss();
        });
        content.findViewById(R.id.action_publish).setOnClickListener(v -> {
            Intent publicActivity = new Intent(MainActivity.this, AtyPublicActivity.class);
            startActivity(publicActivity);
            dialog.dismiss();
        });
        content.findViewById(R.id.action_qrcode).setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(content);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.gravity = Gravity.TOP | Gravity.END;
        params.x = DimensionUtils.dp2px(20);   //x position
        params.y = DimensionUtils.dp2px(56) + DimensionUtils.getStatusBarHeight();   //y position
        params.width = DimensionUtils.dp2px(160);
        params.height = DimensionUtils.dp2px(123);
        dialog.show();
    }

    private void setupViews() {
        mTabItems.get(0).setEnable(true);
        mTitleTExtView.setText(R.string.activity);
        TabPagerAdapter mTabPagerAdapter = new TabPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mTabPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mTitleTExtView.setText(mTitleTexts[position]);
                for (int i = 0; i < PAGE_COUNT; i++) {
                    mTabItems.get(i).setEnable(i == position);
                }
                if (position == 0)
                    mMoreImageView.setVisibility(View.VISIBLE);
                else {
                    mMoreImageView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        View.OnClickListener mTabItemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int p = (int) v.getTag();
                mViewPager.setCurrentItem(p);
            }
        };
        for (int i = 0; i < PAGE_COUNT; i++) {
            mTabItems.get(i).setTag(i);
            mTabItems.get(i).setOnClickListener(mTabItemClickListener);
        }
    }

    static class TabPagerAdapter extends FragmentPagerAdapter {

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ActivityFragment.newInstance();
                case 1:
                    return CommunityFragment.newInstance();
                case 2:
                    return SeekFragment.newInstance();
                case 3:
                    return MyFragment.newInstance();
                default:
                    throw new RuntimeException("position can not be larger than 3");
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
}
