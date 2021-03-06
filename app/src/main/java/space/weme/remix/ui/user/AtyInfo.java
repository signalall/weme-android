package space.weme.remix.ui.user;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.APP;
import space.weme.remix.R;
import space.weme.remix.model.TimeLine;
import space.weme.remix.model.UserImage;
import space.weme.remix.service.Services;
import space.weme.remix.service.UserService;
import space.weme.remix.ui.AtyImage;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.ui.community.PostDetailActivity;
import space.weme.remix.ui.intro.AtyEditInfo;
import space.weme.remix.util.BitmapUtils;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.OkHttpUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.LoadingPrompt;
import space.weme.remix.widgt.TagView;
import space.weme.remix.widgt.WDialog;

/**
 * Created by Liujilong on 2016/1/29.
 * liujilong.me@gmail.com
 */
public class AtyInfo extends BaseActivity {
    public static final String ID_INTENT = "id";
    private static final String TAG = "AtyInfo";
    private static final int REQUEST_IMAGE = 0xef;
    private static final int REQUEST_AVATAR = 0xff;
    final int GRID_COUNT = 3;
    boolean isMe = false;
    LinearLayout mWholeLayout;
    SimpleDraweeView mDrawBackground;
    int birthFlag;
    int followFlag;
    boolean isLoading_2 = false;
    boolean canLoadMore_2 = true;
    int page_2;
    boolean isLoading_3 = false;
    int page_3_previous_id;
    List<TimeLine> timeLineList;
    TimeLineAdapter mTimeLineAdapter;
    List<UserImage> userImageList;
    UserImageAdapter mUserImageAdapter;
    WindowListener mWindowListener;
    UserImageListener mUserImageListener;
    private String mId;
    private UserService.GetProfileByUserIdResp mUser;
    private TextView mLikeCount;
    private TextView mTvVisit;
    private TextView mTvConstellation;
    private ImageView mIvGender;
    private View[] mPagerViews;
    private ViewPager mViewPager;
    private SimpleDraweeView mDrawAvatar;
    private SwipeRefreshLayout swipe_2;
    private SwipeRefreshLayout swipe_3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mId = getIntent().getStringExtra(ID_INTENT);
        setContentView(R.layout.aty_info);
        isMe = Integer.parseInt(StrUtils.id()) == Integer.parseInt(mId);

        mWholeLayout = (LinearLayout) findViewById(R.id.aty_info_layout);


        mTvVisit = (TextView) findViewById(R.id.aty_info_visit);
        mTvConstellation = (TextView) findViewById(R.id.aty_info_constellation);
        mIvGender = (ImageView) findViewById(R.id.aty_info_gender);
        mDrawAvatar = (SimpleDraweeView) findViewById(R.id.aty_info_avatar);
        final TabLayout mTabLayout = (TabLayout) findViewById(R.id.aty_info_tabs);
        mViewPager = (ViewPager) findViewById(R.id.aty_info_pager);
        mViewPager.setAdapter(new InfoAdapter());
        mTabLayout.setupWithViewPager(mViewPager);

        mDrawAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(AtyInfo.this, AtyImage.class);
                i.putExtra(AtyImage.URL_INTENT, StrUtils.avatarForID(mId));
                startActivity(i);
                overridePendingTransition(0, 0);
            }
        });


        View view = findViewById(R.id.aty_info_top);
        int width = DimensionUtils.getDisplay().widthPixels;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width * 3 / 5);
        view.setLayoutParams(params);

        mDrawBackground = (SimpleDraweeView) findViewById(R.id.aty_info_background);
        mDrawBackground.setImageURI(Uri.parse(StrUtils.backgroundForID(mId)));
        GenericDraweeHierarchy hierarchy = mDrawBackground.getHierarchy();
        hierarchy.setPlaceholderImage(R.mipmap.info_default);

        mWindowListener = new WindowListener();
        if (StrUtils.id().equals(mId)) {
            mDrawBackground.setOnClickListener(mWindowListener);
            findViewById(R.id.aty_info_like_layout).setVisibility(View.VISIBLE);
            mLikeCount = (TextView) findViewById(R.id.aty_info_like_count);
            ArrayMap<String, String> map = new ArrayMap<>();
            map.put("token", StrUtils.token());
            OkHttpUtils.post(StrUtils.GET_LIKE_COUNT, map, TAG, new OkHttpUtils.SimpleOkCallBack() {
                @Override
                public void onResponse(String s) {
                    JSONObject j = OkHttpUtils.parseJSON(AtyInfo.this, s);
                    if (j == null) {
                        return;
                    }
                    int count = j.optInt("likeNumber");
                    mLikeCount.setText(count + "");
                }
            });
        }

        findViewById(R.id.aty_info_more).setOnClickListener(mWindowListener);

        visitUser();

        mUserImageListener = new UserImageListener();

        setUpPagerViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fireInfo();
        configView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDrawAvatar.setImageURI(Uri.parse(StrUtils.thumForID(mId)));
    }

    private void visitUser() {
        if (isMe) return;
        ArrayMap<String, String> param = new ArrayMap<>();
        param.put("token", StrUtils.token());
        param.put("userid", mId);
        OkHttpUtils.post(StrUtils.VISIT_USER, param, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onResponse(String s) {
                LogUtils.i(TAG, "visit User " + s);
            }
        });
    }

    private void fireInfo() {
        ArrayMap<String, String> param = new ArrayMap<>();
        param.put("token", StrUtils.token());
        param.put("userid", mId);
        OkHttpUtils.post(StrUtils.GET_VISIT_INFO, param, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onResponse(String s) {
                JSONObject j = OkHttpUtils.parseJSON(AtyInfo.this, s);
                if (j == null) {
                    return;
                }
                JSONObject result = j.optJSONObject("result");
                int today = result.optInt("today");
                int total = result.optInt("total");
                String visitInfo = getResources().getString(R.string.today_visit) + " " + today + " " +
                        getResources().getString(R.string.total_visit) + " " + total + " ";
                mTvVisit.setText(visitInfo);
            }
        });
        param.clear();

    }

    private void setUpPagerViews() {
        mPagerViews = new View[3];
        View v0 = LayoutInflater.from(this).inflate(R.layout.aty_info_view1, mViewPager, false);
        View v1 = LayoutInflater.from(this).inflate(R.layout.aty_info_view2, mViewPager, false);
        View v2 = LayoutInflater.from(this).inflate(R.layout.aty_info_view3, mViewPager, false);
        mPagerViews[0] = v0;
        mPagerViews[1] = v1;
        mPagerViews[2] = v2;
    }

    private void configView() {
        configView1();

        configView2();

        configView3();
    }

    private void configView1() {
        final TagView tagView = (TagView) mPagerViews[0].findViewById(R.id.tag_view);
        final TagView.TagAdapter adapter = new TagView.TagAdapter(this);
        ArrayMap<String, String> params = new ArrayMap<>();
        params.put("token", StrUtils.token());
        params.put("userid", mId);
        OkHttpUtils.post(StrUtils.GET_TAGS_BY_ID, params, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onResponse(String s) {
                JSONObject j = OkHttpUtils.parseJSON(AtyInfo.this, s);
                if (j == null) return;
                JSONObject result = j.optJSONObject("result");
                if (result == null) return;
                JSONObject tags = result.optJSONObject("tags");
                if (tags == null) return;
                JSONArray tagsArray = tags.optJSONArray("custom");
                if (tagsArray == null) return;
                ArrayList<String> tagList = new ArrayList<>();
                for (int i = 0; i < tagsArray.length(); i++) {
                    tagList.add(tagsArray.optString(i));
                }
                adapter.setTags(tagList);
            }
        });
        if (isMe) {
            adapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    uploadTags(adapter.getTags());
                }

                @Override
                public void onInvalidated() {
                    uploadTags(adapter.getTags());
                }
            });

            adapter.setCanEdit(true);
            adapter.setOnAddListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View vvv = LayoutInflater.from(AtyInfo.this).inflate(R.layout.tag_view_edit, null);
                    final EditText et = (EditText) vvv.findViewById(R.id.edit_tag_edit_text);
                    TextView tv = (TextView) vvv.findViewById(R.id.edit_tag);
                    tv.setText(R.string.add_tag);
                    new WDialog.Builder(AtyInfo.this).setCustomView(vvv)
                            .setPositive(R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    adapter.addTag(et.getText().toString());
                                }
                            }).show();
                }
            });
            adapter.setOnItemClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sp = APP.context().getSharedPreferences("Info3", Context.MODE_PRIVATE);
                    if (sp.getBoolean("shown", false)) return;
                    new WDialog.Builder(AtyInfo.this).setMessage(R.string.longclick).hideNegative(true).show();
                    sp.edit().putBoolean("shown", true).apply();
                }
            });

            adapter.setOnItemLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int index = -1;
                    for (int i = 0; i < adapter.getCount(); i++) {
                        if (v == tagView.getChildAt(i)) index = i;
                    }
                    if (index < 0) return false;
                    final int position = index;
                    View vv = LayoutInflater.from(AtyInfo.this).inflate(R.layout.tag_view_edit_or_delete, null);
                    final WDialog dialog = new WDialog.Builder(AtyInfo.this).setCustomView(vv).hideButtons(true).show();
                    vv.findViewById(R.id.delete_tag).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            new WDialog.Builder(AtyInfo.this).setMessage("确定要删除标签：" + adapter.getItem(position))
                                    .setPositive(R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            adapter.removeTagAtPosition(position);
                                        }
                                    }).show();
                        }
                    });
                    vv.findViewById(R.id.edit_tag).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            View vvv = LayoutInflater.from(AtyInfo.this).inflate(R.layout.tag_view_edit, null);
                            final EditText et = (EditText) vvv.findViewById(R.id.edit_tag_edit_text);
                            et.setText((CharSequence) adapter.getItem(position));
                            new WDialog.Builder(AtyInfo.this).setCustomView(vvv)
                                    .setPositive(R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            adapter.setTagAtPosition(position, et.getText().toString());
                                        }
                                    }).show();
                        }
                    });
                    return true;
                }
            });
        }

        tagView.setAdapter(adapter);
        Services.userService()
                .getProfileByUserId(new UserService.GetProfileByUserId(StrUtils.token(), mId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getProfileByUserId: " + resp.toString());
                    if (!"successful".equals(resp.getState())) {
                        finish();
                    } else {
                        mUser = resp;
                        birthFlag = resp.getBirthFlag();
                        followFlag = resp.getFollowFlag();
                        mTvConstellation.setText(resp.getConstellation());
                        boolean male = getResources().getString(R.string.male).equals(mUser.getGender());
                        mIvGender.setImageResource(male ? R.mipmap.boy : R.mipmap.girl);
                        TextView tvName = (TextView) mPagerViews[0].findViewById(R.id.aty_info_name);
                        tvName.setText(resp.getName());
                        TextView tvBirth = (TextView) mPagerViews[0].findViewById(R.id.aty_info_birth);
                        tvBirth.setText(resp.getBirthday());

                        TextView tvSchool = (TextView) mPagerViews[0].findViewById(R.id.aty_info_school);
                        tvSchool.setText(resp.getSchool());
                        TextView tvEducation = (TextView) mPagerViews[0].findViewById(R.id.aty_info_education);
                        tvEducation.setText(resp.getDegree());
                        TextView tvMajor = (TextView) mPagerViews[0].findViewById(R.id.aty_info_major);
                        tvMajor.setText(resp.getDepartment());

                        TextView tvHome = (TextView) mPagerViews[0].findViewById(R.id.aty_info_home);
                        tvHome.setText(resp.getHometown());

                        TextView tvQQ = (TextView) mPagerViews[0].findViewById(R.id.aty_info_qq);
                        tvQQ.setText(resp.getQq());

                        TextView tvWeChat = (TextView) mPagerViews[0].findViewById(R.id.aty_info_we_chat);
                        tvWeChat.setText(resp.getWechat());

                        final Button btnFollow = (Button) mPagerViews[0].findViewById(R.id.aty_info_follow_btn);
                        LinearLayout llBtn = (LinearLayout) mPagerViews[0].findViewById(R.id.aty_info_view1_btn);
                        if (mId.equals(StrUtils.id())) {
                            llBtn.setVisibility(View.GONE);
                        } else {
                            llBtn.setVisibility(View.VISIBLE);
                            switch (followFlag) {
                                case 1:
                                case 3:
                                    btnFollow.setText(R.string.unfollow);
                                    break;
                                case 0:
                                case 2:
                                    btnFollow.setText(R.string.add_follow);
                            }
                            btnFollow.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (followFlag == 1 || followFlag == 3) {
                                        unfollow();
                                    } else {
                                        follow();
                                    }
                                }
                            });
                            mPagerViews[0].findViewById(R.id.aty_info_message_btn).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    sendMessage();
                                }
                            });
                        }
                    }
                }, ex -> {
                    Log.e(TAG, "getProfileByUserId: " + ex.getMessage());
                    Toast.makeText(AtyInfo.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadTags(List<String> tags) {
        JSONObject j = new JSONObject();
        try {
            j.put("token", StrUtils.token());
            JSONObject tagObject = new JSONObject();
            JSONArray tagArray = new JSONArray(tags);
            tagObject.put("custom", tagArray);
            j.put("tags", tagObject);
        } catch (JSONException e) {
            return;
        }
        LogUtils.d(TAG, "uploadTags: param: " + j.toString());
        OkHttpUtils.post(StrUtils.SET_TAGS, j, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onResponse(String s) {
                LogUtils.d(TAG, "uploadTag: " + s);
            }
        });
    }

    private void configView2() {
        swipe_2 = (SwipeRefreshLayout) mPagerViews[1];
        RecyclerView recyclerView = (RecyclerView) swipe_2.findViewById(R.id.aty_info_view2_recycler);
        mTimeLineAdapter = new TimeLineAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(AtyInfo.this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mTimeLineAdapter);
        timeLineList = new ArrayList<>();
        mTimeLineAdapter.setTimeLineList(timeLineList);
        getTimeLine(1);
        swipe_2.setColorSchemeResources(R.color.colorPrimary);
        swipe_2.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading_2) {
                    getTimeLine(1);
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
                if (!isLoading_2 && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + 2) && canLoadMore_2) {
                    Log.i(TAG, "scroll to end  load page " + (page_2 + 1));
                    getTimeLine(page_2 + 1);
                }
            }
        });
    }

    private void getTimeLine(final int page) {
        ArrayMap<String, String> param = new ArrayMap<>();
        param.put("token", StrUtils.token());
        param.put("userid", mId);
        param.put("page", String.format("%d", page));
        isLoading_2 = true;
        OkHttpUtils.post(StrUtils.GET_TIME_LINE_URL, param, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onFailure(IOException e) {
                isLoading_2 = false;
                swipe_2.setRefreshing(false);
            }

            @Override
            public void onResponse(String s) {
                isLoading_2 = false;
                swipe_2.setRefreshing(false);
                JSONObject j = OkHttpUtils.parseJSON(AtyInfo.this, s);
                if (j == null) {
                    return;
                }
                JSONArray array = j.optJSONArray("result");
                if (page == 1) {
                    timeLineList.clear();
                }
                page_2 = page;
                int size = array.length();
                int preCount = timeLineList.size();
                canLoadMore_2 = size != 0;
                for (int i = 0; i < array.length(); i++) {
                    timeLineList.add(TimeLine.fromJSON(array.optJSONObject(i)));
                }
                if (preCount == 0) {
                    mTimeLineAdapter.notifyDataSetChanged();
                } else {
                    mTimeLineAdapter.notifyItemRangeInserted(preCount, size);
                }
            }
        });
    }


    private void configView3() {
        swipe_3 = (SwipeRefreshLayout) mPagerViews[2].findViewById(R.id.aty_info_view3_swipe);
        RecyclerView recyclerView = (RecyclerView) swipe_3.findViewById(R.id.aty_info_view3_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(AtyInfo.this, GRID_COUNT));
        recyclerView.setHasFixedSize(true);
        mUserImageAdapter = new UserImageAdapter();
        userImageList = new ArrayList<>();
        mUserImageAdapter.setUserImageList(userImageList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mUserImageAdapter);
        swipe_3.setColorSchemeResources(R.color.colorPrimary);
        swipe_3.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading_3) {
                    getUserImages(1);
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
                if (!isLoading_3 && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + 2)) {
                    Log.i(TAG, "scroll to end  load page " + (page_3_previous_id - 1));
                    getUserImages(page_3_previous_id - 1);
                }
            }
        });
        page_3_previous_id = 0;
        getUserImages(0);

        FloatingActionButton fab = (FloatingActionButton) mPagerViews[2].findViewById(R.id.fab);
        if (isMe) {
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadAvatar();
                }
            });
        } else {
            fab.setVisibility(View.GONE);
        }
    }

    private void uploadAvatar() {
        Intent intent = new Intent(AtyInfo.this, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
        startActivityForResult(intent, REQUEST_AVATAR);
    }

    private void getUserImages(final int previous_id) {
        if (page_3_previous_id < 0) return;
        ArrayMap<String, String> param = new ArrayMap<>();
        param.put("token", StrUtils.token());
        param.put("userid", mId);
        param.put("previous_id", String.format("%d", previous_id));
        isLoading_3 = true;
        OkHttpUtils.post(StrUtils.GET_PERSIONAL_IMAGE_URL, param, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onFailure(IOException e) {
                isLoading_3 = false;
            }

            @Override
            public void onResponse(String s) {
                isLoading_3 = false;
                swipe_3.setRefreshing(false);
                JSONObject j = OkHttpUtils.parseJSON(AtyInfo.this, s);
                if (j == null) {
                    return;
                }
                if (previous_id == 0) {
                    userImageList.clear();
                }
                JSONArray result = j.optJSONArray("result");
                int size = result.length();
                int preCount = userImageList.size();
                for (int i = 0; i < result.length(); i++) {
                    userImageList.add(UserImage.fromJSON(result.optJSONObject(i)));
                }
                if (userImageList.size() == 0) {
                    page_3_previous_id = -1;
                    return;
                }
                page_3_previous_id = Integer.parseInt(userImageList.get(userImageList.size() - 1).id);
                if (preCount == 0) {
                    mUserImageAdapter.notifyDataSetChanged();
                } else {
                    mUserImageAdapter.notifyItemRangeInserted(preCount, size);
                }
            }
        });
    }

    private void unfollow() {
        new WDialog.Builder(AtyInfo.this)
                .setMessage(R.string.sure_to_unfollow)
                .setPositive(R.string.unfollow, v -> unfollowUser()
                ).show();
    }

    private void unfollowUser() {
        Services.userService()
                .unfollowUser(new UserService.UnfollowUser(StrUtils.token(), String.valueOf(mId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if ("successful".equals(resp.getState())) {
                        Toast.makeText(AtyInfo.this,
                                R.string.unfollow_success,
                                Toast.LENGTH_SHORT)
                                .show();
                        configView1();
                    } else {
                        Toast.makeText(AtyInfo.this,
                                resp.getReason(),
                                Toast.LENGTH_SHORT).show();
                    }
                }, ex -> {
                    Toast.makeText(AtyInfo.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT)
                            .show();
                });
    }

    private void follow() {
        Services.userService()
                .followUser(new UserService.FollowUser(StrUtils.token(), String.valueOf(mId)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if ("successful".equals(resp.getState())) {
                        Toast.makeText(AtyInfo.this,
                                R.string.follow_success,
                                Toast.LENGTH_SHORT)
                                .show();
                        configView1();
                    } else {
                        Toast.makeText(AtyInfo.this,
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Toast.makeText(AtyInfo.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT)
                            .show();
                });
    }

    @Override
    protected String tag() {
        return TAG;
    }

    private void changeBackground() {
        Intent intent = new Intent(AtyInfo.this, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void sendMessage() {
        Intent i = new Intent(AtyInfo.this, AtyMessageReply.class);
        i.putExtra(AtyMessageReply.INTENT_ID, mId + "");
        startActivity(i);
    }

    private void editMyInfo() {
        LogUtils.i(TAG, "edit my info");
        Intent i = new Intent(AtyInfo.this, AtyEditInfo.class);
        i.putExtra(AtyEditInfo.INTENT_EDIT, true);
        if (mUser != null) {
            i.putExtra(AtyEditInfo.INTENT_INFO, (new Gson()).toJson(mUser));
        }
        startActivity(i);
    }

    private void audioRecord() {
        LogUtils.i(TAG, "audio record");
        Intent i = new Intent(AtyInfo.this, AtyAudioRecord.class);
        startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_IMAGE) {
            List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            String mAvatarPath = paths.get(0);
            int width = mDrawBackground.getWidth();
            int height = mDrawBackground.getHeight();
            BitmapUtils.showResizedPicture(mDrawBackground, Uri.parse("file://" + mAvatarPath), width, height);
            ArrayMap<String, String> map = new ArrayMap<>();
            map.put("token", StrUtils.token());
            map.put("type", "-1");
            map.put("number", "1");
            OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL, map, mAvatarPath, StrUtils.MEDIA_TYPE_IMG, TAG, new OkHttpUtils.SimpleOkCallBack() {
                @Override
                public void onResponse(String s) {
                    LogUtils.d(TAG, s);
                    JSONObject j = OkHttpUtils.parseJSON(AtyInfo.this, s);
                    if (j == null) {
                        return;
                    }
                    ImagePipeline imagePipeline = Fresco.getImagePipeline();
                    imagePipeline.evictFromCache(Uri.parse(StrUtils.backgroundForID(mId)));
                }
            });
        } else if (requestCode == REQUEST_AVATAR) {
            final LoadingPrompt prompt = new LoadingPrompt(AtyInfo.this);
            List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            if (path != null && !path.isEmpty()) {
                Map<String, String> map = new ArrayMap<>();
                map.put("token", StrUtils.token());
                map.put("type", "-16");
                final int total = path.size();
                final int[] cur = {0};
                prompt.show(getWindow().getDecorView(), "正在上传图片");
                for (int i = 0; i < path.size(); i++) {
                    OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL, map, path.get(i), StrUtils.MEDIA_TYPE_IMG, TAG, new OkHttpUtils.SimpleOkCallBack() {
                        @Override
                        public void onResponse(String s) {
                            JSONObject j = OkHttpUtils.parseJSON(AtyInfo.this, s);
                            if (j != null) {
                                cur[0]++;
                                if (cur[0] == total) {
                                    prompt.dismiss();
                                    configView3();
                                }
                            }
                        }

                        @Override
                        public void onFailure(IOException e) {
                            super.onFailure(e);
                        }
                    });
                }
            }
        }
    }

    private class InfoAdapter extends PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = mPagerViews[position];
            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] titles = getResources().getStringArray(R.array.aty_info_tabs);
            return titles[position];
        }
    }

    private class TimeLineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<TimeLine> timeLineList;

        public void setTimeLineList(List<TimeLine> timeLineList) {
            this.timeLineList = timeLineList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(AtyInfo.this).inflate(R.layout.aty_info_view2_cell, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final TimeLine timeLine = timeLineList.get(position);
            VH vh = (VH) holder;
            vh.tvTimeStamp.setText(StrUtils.timeTransfer(timeLine.timestamp));
            vh.tvTopic.setText(timeLine.topic);
            vh.mDrawImage.setImageURI(Uri.parse(timeLine.image));
            vh.tvTitle.setText(timeLine.title);
            vh.tvContent.setText(timeLine.body);
            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(AtyInfo.this, PostDetailActivity.class);
                    i.putExtra(PostDetailActivity.POST_INTENT, timeLine.postId);
                    i.putExtra(PostDetailActivity.THEME_INTENT, timeLine.topic);
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return timeLineList == null ? 0 : timeLineList.size();
        }

        private class VH extends RecyclerView.ViewHolder {
            TextView tvTimeStamp;
            TextView tvTopic;
            SimpleDraweeView mDrawImage;
            TextView tvTitle;
            TextView tvContent;

            public VH(View itemView) {
                super(itemView);
                tvTimeStamp = (TextView) itemView.findViewById(R.id.aty_info_view2_cell_timestamp);
                tvTopic = (TextView) itemView.findViewById(R.id.aty_info_view2_cell_topic);
                mDrawImage = (SimpleDraweeView) itemView.findViewById(R.id.aty_info_view2_cell_image);
                tvTitle = (TextView) itemView.findViewById(R.id.aty_info_view2_cell_title);
                tvContent = (TextView) itemView.findViewById(R.id.aty_info_view2_cell_content);

            }
        }
    }

    private class UserImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        List<UserImage> userImageList;

        public void setUserImageList(List<UserImage> userImageList) {
            this.userImageList = userImageList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(AtyInfo.this).inflate(R.layout.aty_info_view3_cell, parent, false);
            int size = DimensionUtils.getDisplay().widthPixels / GRID_COUNT;
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = size;
            params.width = size;
            v.setLayoutParams(params);
            v.setOnClickListener(mUserImageListener);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            VH vh = (VH) holder;
            vh.draw.setImageURI(Uri.parse(userImageList.get(position).thumbnail));
            vh.itemView.setTag(position);
        }

        @Override
        public int getItemCount() {
            return userImageList == null ? 0 : userImageList.size();
        }

        private class VH extends RecyclerView.ViewHolder {
            SimpleDraweeView draw;

            public VH(View itemView) {
                super(itemView);
                draw = (SimpleDraweeView) itemView.findViewById(R.id.aty_info_view3_cell_image);
            }
        }
    }

    private class WindowListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            View content;
            View.OnClickListener listener;
            final Dialog dialog = new Dialog(AtyInfo.this, R.style.DialogSlideAnim);
            if (v.getId() == R.id.aty_info_background) {
                content = LayoutInflater.from(AtyInfo.this).inflate(R.layout.aty_info_option1, mWholeLayout, false);
                listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (v.getId() == R.id.aty_info_option_change_background) {
                            changeBackground();
                        }
                        dialog.dismiss();
                    }
                };
                content.findViewById(R.id.aty_info_option_cancel).setOnClickListener(listener);
                content.findViewById(R.id.aty_info_option_change_background).setOnClickListener(listener);
                dialog.setContentView(content);
            } else if (v.getId() == R.id.aty_info_more) {
                if (Integer.parseInt(StrUtils.id()) == Integer.parseInt(mId)) {
                    content = LayoutInflater.from(AtyInfo.this).inflate(R.layout.aty_info_option3, mWholeLayout, false);
                    listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.getId() == R.id.aty_info_option_change_background) {
                                changeBackground();
                            } else if (v.getId() == R.id.aty_info_option_edit_info) {
                                editMyInfo();
                            } else if (v.getId() == R.id.aty_info_option_audio_record) {
                                audioRecord();
                            }
                            dialog.dismiss();
                        }
                    };
                    content.findViewById(R.id.aty_info_option_audio_record).setOnClickListener(listener);
                    content.findViewById(R.id.aty_info_option_cancel).setOnClickListener(listener);
                    content.findViewById(R.id.aty_info_option_change_background).setOnClickListener(listener);
                    content.findViewById(R.id.aty_info_option_edit_info).setOnClickListener(listener);
                    dialog.setContentView(content);
                } else {
                    content = LayoutInflater.from(AtyInfo.this).inflate(R.layout.aty_info_option2, mWholeLayout, false);
                    listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (v.getId() == R.id.aty_info_option_message) {
                                sendMessage();
                            }
                            dialog.dismiss();
                        }
                    };
                    content.findViewById(R.id.aty_info_option_cancel).setOnClickListener(listener);
                    content.findViewById(R.id.aty_info_option_message).setOnClickListener(listener);
                    dialog.setContentView(content);
                }
            }
            WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
            wmlp.gravity = Gravity.BOTTOM | Gravity.START;
            wmlp.x = 0;   //x position
            wmlp.y = 0;   //y position
            wmlp.width = DimensionUtils.getDisplay().widthPixels;
            dialog.show();
        }
    }

    private class UserImageListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            JSONArray array = new JSONArray();
            for (UserImage image : userImageList) {
                array.put(image.toJSON());
            }
            int index = (int) v.getTag();
            Intent i = new Intent(AtyInfo.this, AtyImagePager.class);
            i.putExtra(AtyImagePager.INTENT_CONTENT, array.toString());
            i.putExtra(AtyImagePager.INTENT_INDEX, index);
            startActivity(i);
        }
    }

}
