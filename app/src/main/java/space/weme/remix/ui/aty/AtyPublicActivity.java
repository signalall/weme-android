package space.weme.remix.ui.aty;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.service.ActivityService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.base.SwipeActivity;
import space.weme.remix.util.OkHttpUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.WDialog;
import space.weme.remix.widgt.WSwitch;

public class AtyPublicActivity extends SwipeActivity {

    private static final String TAG = "AtyPublicActivity";
    private static final int REQUEST_IMAGE = 2;

    @BindView(R.id.img_act_add)
    SimpleDraweeView actAdd;

    @BindView(R.id.txt_activity_public)
    TextView txtPublic;

    @BindView(R.id.edit_title)
    EditText editTitle;

    @BindView(R.id.edit_time)
    EditText editTime;

    @BindView(R.id.edit_location)
    EditText editLocation;

    @BindView(R.id.edit_number)
    EditText editNumber;

    @BindView(R.id.edit_advertise)
    EditText editAdvertise;

    @BindView(R.id.edit_detail)
    EditText editDetail;

    @BindView(R.id.edit_labe)
    EditText editLabe;

    @BindView(R.id.tog_btn)
    WSwitch wSwitch;

    private ArrayList<String> path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_public_activity);
        ButterKnife.bind(this);
        wSwitch.setOn(false);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            Uri uri = Uri.fromFile(new File(path.get(0)));

            // Uri uri = "file:///mnt/sdcard/MyApp/myfile.jpg";
            //解决图片大于4M不显示问题(由于openGL最大支持4M，有局限只能缩小图片了)
            Log.e(TAG, actAdd.getLayoutParams().width + " " + actAdd.getLayoutParams().height);
            ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setAutoRotateEnabled(true)
                    .setResizeOptions(new ResizeOptions(actAdd.getLayoutParams().width, actAdd.getLayoutParams().height));

            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(imageRequestBuilder.build())
                    .setOldController(actAdd.getController())
                    .build();

            actAdd.setController(controller);
            Log.d(TAG, "uri " + uri.toString());
            //     actAdd.setImageURI(uri);
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @OnClick(R.id.img_act_add)
    public void onAddImageClick() {
        Intent intent = new Intent(AtyPublicActivity.this, MultiImageSelectorActivity.class);
        // 是否显示拍摄图片
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        // 最大可选择图片数量(多图情况下)
        //intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, Config.MAX_PICTURE);
        // 选择模式
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @OnClick(R.id.txt_activity_public)
    public void onPublishClick() {
        if (path != null && !path.isEmpty()) {
            new WDialog.Builder(AtyPublicActivity.this).setMessage("确定发布活动吗？")
                    .setPositive("发布", v -> publishActivity()).show();
        } else {
            new WDialog.Builder(AtyPublicActivity.this).setMessage("请填写必填信息").show();
        }
    }

    void publishActivity() {
        ActivityService.PublishActivity pa = new ActivityService.PublishActivity(
                StrUtils.token(),
                editTitle.getText().toString(),
                editLocation.getText().toString(),
                editNumber.getText().toString(),
                editTime.getText().toString(),
                editAdvertise.getText().toString(),
                wSwitch.isOn() ? "true" : "false",
                editDetail.getText().toString(),
                editLabe.getText().toString()
        );
        Services.activityService()
                .publishActivity(pa)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "publishActivity: " + resp.toString());
                    if ("successful".equals(resp.getState())) {
                        new WDialog.Builder(AtyPublicActivity.this)
                                .setTitle("提示")
                                .setMessage("活动发布成功，请等待审核")
                                .show();

                        // 上传图片
                        Map<String, String> params = new HashMap<>();
                        params.put("token", StrUtils.token());
                        params.put("type", "-10");
                        params.put("activityid", String.valueOf(resp.getId()));
                        for (int i = 0; i < path.size(); i++) {
                            String p = path.get(i);
                            params.put("number", String.valueOf(i));
                            OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL,
                                    params,
                                    path.get(i),
                                    StrUtils.MEDIA_TYPE_IMG,
                                    TAG,
                                    new OkHttpUtils.OkCallBack() {
                                        @Override
                                        public void onFailure(IOException e) {
                                        }

                                        @Override
                                        public void onResponse(String res) {
                                        }
                                    });
                        }

                    } else {
                        new WDialog.Builder(AtyPublicActivity.this)
                                .setTitle("提示")
                                .setMessage("活动发布失败")
                                .setPositive("确认", v -> onBackPressed())
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "publishActivity: " + ex.getMessage());
                });
    }

}
