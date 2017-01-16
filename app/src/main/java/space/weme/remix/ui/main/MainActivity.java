package space.weme.remix.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import space.weme.remix.R;
import space.weme.remix.ui.activity.PublishActivityActivity;
import space.weme.remix.ui.activity.SearchActivityActivity;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.ui.intro.AtyLogin;
import space.weme.remix.util.UpdateUtils;
import space.weme.remix.widgt.TabItem;

/**
 * Created by Liujilong on 16/1/24.
 * liujilong.me@gmail.com
 */
public class MainActivity extends BaseActivity {

    public static final String INTENT_LOGOUT = "intent_lougout";
    public static final String INTENT_UPDATE = "intent_update";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PAGE_COUNT = 4;

    @BindViews({R.id.main_item_activity,
            R.id.main_item_community,
            R.id.main_item_find,
            R.id.main_item_me
    })
    List<TabItem> mTabItems;

    @BindView(R.id.main_pager)
    ViewPager mViewPager;

    @BindView(R.id.whole_layout)
    ViewGroup mViewGroup;

    @BindView(R.id.toolbar_main)
    Toolbar toolbar;

    private int[] mTitleTexts = new int[]{
            R.string.activity,
            R.string.community,
            R.string.find,
            R.string.me
    };

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

    private void setupMenu(int index) {
        if (index == 0) {
            if (toolbar.getMenu() != null)
                toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_activity_fragment);
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_search) {
                    Intent search = new Intent(MainActivity.this, SearchActivityActivity.class);
                    startActivity(search);
                    return true;
                } else if (item.getItemId() == R.id.action_publish) {
                    Intent publicActivity = new Intent(MainActivity.this, PublishActivityActivity.class);
                    startActivity(publicActivity);
                    return true;
                } else if (item.getItemId() == R.id.action_qrcode) {
                    return true;
                }
                return false;
            });
        } else {
            if (toolbar.getMenu() != null)
                toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.menu_empty);
        }
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setTitle(mTitleTexts[index]);
    }

    private void setupViews() {
        mTabItems.get(0).setEnable(true);
        setupMenu(0);

        TabPagerAdapter mTabPagerAdapter = new TabPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mTabPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < PAGE_COUNT; i++) {
                    mTabItems.get(i).setEnable(i == position);
                }
                setupMenu(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        for (int i = 0; i < PAGE_COUNT; i++) {
            mTabItems.get(i).setTag(i);
            mTabItems.get(i).setOnClickListener(v -> {
                int p = (int) v.getTag();
                mViewPager.setCurrentItem(p);
            });
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
