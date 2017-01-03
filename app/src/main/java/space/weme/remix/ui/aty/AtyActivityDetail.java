package space.weme.remix.ui.aty;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.imid.swipebacklayout.lib.SwipeBackLayout;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import okhttp3.MediaType;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.AtyDetail;
import space.weme.remix.service.ActivityService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.base.SwipeActivity;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.OkHttpUtils;
import space.weme.remix.util.OkHttpUtils.SimpleOkCallBack;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.WDialog;

public class AtyActivityDetail extends SwipeActivity {
    private static final String TAG = "AtyDetail";
    public static final String INTENT = "activityid";
    private static final int MAX_PICTURE = 2;
    private static final int REQUEST_IMAGE = 2;
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private int activityid;
    private ArrayList<String> path;

    @BindView(R.id.sign_time)
    TextView txtTime;

    @BindView(R.id.sign_location)
    TextView txtLocation;

    @BindView(R.id.txt_public_school)
    TextView txtSchool;

    @BindView(R.id.txt_public_author)
    TextView txtAuthor;

    @BindView(R.id.sign_detail)
    TextView txtDetail;

    @BindView(R.id.sign_remark)
    TextView txtRemark;

    @BindView(R.id.sign_number)
    TextView txtSignNumber;

    @BindView(R.id.btn_sign)
    Button btnSign;

    @BindView(R.id.btn_love)
    Button btnLove;

    @BindView(R.id.image)
    SimpleDraweeView avatar;

    @BindView(R.id.slogan)
    TextView tvSlogan;

    @BindView(R.id.sign_pic)
    SimpleDraweeView atyAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_detail);
        ButterKnife.bind(this);

        SwipeBackLayout mSwipeBackLayout = getSwipeBackLayout();
        mSwipeBackLayout.setEdgeSize(DimensionUtils.getDisplay().widthPixels / 2);
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);

        activityid = getIntent().getIntExtra(INTENT, -1);
        LogUtils.d(TAG, "id:" + activityid);
        if (activityid != -1) {
            initView();
            initData();
        }
    }

    @BindView(R.id.main_title)
    TextView mainTitle;

    void initView() {
        btnSign.setBackgroundResource(R.drawable.bg_login_btn_pressed);
        btnLove.setBackgroundResource(R.drawable.bg_login_btn_pressed);
        ViewGroup.LayoutParams params = avatar.getLayoutParams();
        params.height = DimensionUtils.getDisplay().widthPixels / 2;
        avatar.setLayoutParams(params);
        mainTitle.setText(R.string.activity_detail);
    }

    void initData() {
        Services.activityService()
                .getActivityDetail(new ActivityService.GetActivityDetail(StrUtils.token(), String.valueOf(activityid)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getActivityDetail: " + resp.toString());
                    AtyDetail detail = resp.getResult();
                    if (detail == null) return;
                    RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
                    roundingParams.setRoundAsCircle(true);
                    ((TextView) findViewById(R.id.main_title)).setText(detail.getTitle());
                    atyAvatar.getHierarchy().setRoundingParams(roundingParams);
                    atyAvatar.setImageURI(Uri.parse(StrUtils.thumForID(detail.getAuthorid() + "")));
                    txtTime.setText(detail.getTime());
                    txtDetail.setText(detail.getDetail());
                    txtAuthor.setText(detail.getAuthor());
                    txtSignNumber.setText(detail.getSignnumber());
                    txtRemark.setText(detail.getRemark());
                    txtSchool.setText(detail.getSchool());
                    txtLocation.setText(detail.getLocation());
                    if ("no".equals(detail.getState())) {
                        btnSign.setText("我要报名");
                        btnSign.setBackgroundResource(R.drawable.bg_login_btn_pressed);
                    } else {
                        btnSign.setText("已报名");
                        btnSign.setBackgroundResource(R.drawable.bg_login_btn_common);
                    }
                    if ("0".equals(detail.getLikeflag())) {
                        btnLove.setText("关注一下");
                        btnLove.setBackgroundResource(R.drawable.bg_login_btn_pressed);
                    } else {
                        btnLove.setText("已关注");
                        btnLove.setBackgroundResource(R.drawable.bg_login_btn_common);
                    }
                    btnSign.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ("no".equals(detail.getState())) {
                                if ("false".equals(detail.getWhetherimage()))
                                    showDialog("确定参加活动吗？", 1);
                                else {
                                    showDialog("请上传您的生活照", 5);
                                }
                            } else {
                                showDialog("是否取消参加该活动吗？", 2);
                            }
                        }
                    });
                    btnLove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if ("0".equals(detail.getLikeflag())) {
                                showDialog("确定关注吗？", 3);
                            } else {
                                showDialog("是否取消关注？", 4);
                            }
                        }
                    });
                    avatar.setImageURI(Uri.parse(detail.getImageurl()));
                    tvSlogan.setText(detail.getAdvertise());
                }, ex -> {
                    Log.e(TAG, "getActivityDetail: " + ex.getMessage());
                });
    }

    protected void showDialog(String msg, final int flag) {
        new WDialog.Builder(AtyActivityDetail.this).setMessage(msg)
                .setPositive(R.string.sure, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (flag) {
                            case 1:
                                signActivity();
                                break;
                            case 2:
                                delSignActivity();
                                break;
                            case 3:
                                likeActivity();
                                break;
                            case 4:
                                unlikeActivity();
                                break;
                            case 5:
                                chooseImage();
                                break;
                            default:
                                LogUtils.e(TAG, "error" + flag);
                                break;
                        }
                    }


                }).show();

    }


    void singIn() {
        ActivityService.SignActivity sa = new ActivityService.SignActivity(
                StrUtils.token(),
                String.valueOf(activityid),
                String.valueOf(activityid)
        );
        Services.activityService()
                .signActivity(sa)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if (resp != null) {
                        initData();
                        Toast.makeText(AtyActivityDetail.this, "报名成功", Toast.LENGTH_SHORT).show();
                    }
                }, ex -> {

                });
    }

    void likeActivity() {
        ActivityService.LikeActivity sa = new ActivityService.LikeActivity(
                StrUtils.token(),
                String.valueOf(activityid),
                String.valueOf(activityid)
        );
        Services.activityService()
                .likeActivity(sa)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if (resp != null)
                        initData();
                }, ex -> {

                });
    }

    private void unlikeActivity() {
        ActivityService.UnlikeActivity sa = new ActivityService.UnlikeActivity(
                StrUtils.token(),
                String.valueOf(activityid),
                String.valueOf(activityid)
        );
        Services.activityService()
                .unlikeActivity(sa)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if (resp != null)
                        initData();
                }, ex -> {

                });
    }

    private void delSignActivity() {
        ActivityService.DelSignActivity sa = new ActivityService.DelSignActivity(
                StrUtils.token(),
                String.valueOf(activityid),
                String.valueOf(activityid)
        );
        Services.activityService()
                .delSignActivity(sa)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if (resp != null)
                        initData();
                }, ex -> {

                });
    }

    private void signActivity() {
        ActivityService.SignActivity sa = new ActivityService.SignActivity(
                StrUtils.token(),
                String.valueOf(activityid),
                String.valueOf(activityid)
        );
        Services.activityService()
                .signActivity(sa)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if (resp != null)
                        initData();
                }, ex -> {

                });
    }


    void chooseImage() {
        Intent intent = new Intent(AtyActivityDetail.this, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量(多图情况下)
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, MAX_PICTURE);
        // 选择模式
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == android.app.Activity.RESULT_OK) {
            path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            if (path != null && !path.isEmpty()) {
                Map<String, String> map = new ArrayMap<>();
                map.put("token", StrUtils.token());
                map.put("type", "-9");
                map.put("activityid", String.valueOf(activityid));
                final int total = path.size();
                final int[] cur = {0};
                for (int i = 0; i < path.size(); i++) {
                    map.put("number", String.valueOf(i + 1));
                    Toast.makeText(AtyActivityDetail.this, "正在上传生活照,请等待", Toast.LENGTH_SHORT).show();
                    OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL, map, path.get(i), MEDIA_TYPE_PNG, TAG, new SimpleOkCallBack() {
                        @Override
                        public void onResponse(String s) {
                            JSONObject j = OkHttpUtils.parseJSON(AtyActivityDetail.this, s);
                            if (j != null) {
                                cur[0]++;
                                if (cur[0] == total) {
                                    singIn();
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }
}
