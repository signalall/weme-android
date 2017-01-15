package space.weme.remix.ui.find;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.service.FoodService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.OkHttpUtils;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 2016/3/3.
 * liujilong.me@gmail.com
 */
public class AtyAddFood extends BaseActivity
        implements
        FgtAddFood.OnAddLocationButtonClickListener,
        FgtAddFood.OnAddPriceButtonClickListener,
        FgtAddFood.OnAddPictureButtonClickListener {
    private static final String TAG = "AtyAddFood";

    static final int REQUEST_IMAGE = 0x12;
    static final int REQUEST_CROP = 0x13;

    FgtAddFood fgtAddFood;

    FgtPrice fgtPrice;

    FgtFoodMap fgtMap;

    ProgressDialog mProgressDialog;

    @BindView(R.id.title_text)
    TextView tvTitle;

    @BindView(R.id.right_text)
    TextView tvRight;

    @BindView(R.id.frame_container)
    FrameLayout frameContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_add_food);
        ButterKnife.bind(this);

        fgtAddFood = FgtAddFood.newInstance();
        fgtPrice = FgtPrice.newInstance();
        fgtMap = FgtFoodMap.newInstance();

        setFragment(fgtAddFood);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i(TAG, "onActivityResult");
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_IMAGE) {
            List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            performCrop(paths.get(0));
            //mDrawAvatar.setImageURI(Uri.parse("file://"+mAvatarPath));
        } else if (requestCode == REQUEST_CROP) {
            fgtAddFood.setPicture();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if (fgtAddFood.isVisible()) {
            super.onBackPressed();
        } else {
            setFragment(fgtAddFood);
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @OnClick(R.id.right_text)
    public void onRightTextClick() {
        uploadFood();
    }

    private void performCrop(String picUri) {
        try {
            //Start Crop Activity
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // indicate image type and Uri
            File f = new File(picUri);
            Uri contentUri = Uri.fromFile(f);

            cropIntent.setDataAndType(contentUri, "image/*");
            // set crop properties
            cropIntent.putExtra("crop", "true");
            // indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);


            // retrieve data on return
            //cropIntent.putExtra("return-data", true);
            // start the activity - we handle returning in onActivityResult
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + StrUtils.cropFilePath));
            cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());


            startActivityForResult(cropIntent, REQUEST_CROP);
        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "your device doesn't support the crop action!";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void uploadFood() {
        if (fgtAddFood.etTitle.getText().length() == 0) {
            Toast.makeText(this, R.string.please_input_food_title, Toast.LENGTH_SHORT).show();
            return;
        }
        if (fgtAddFood.poiItem == null) {
            Toast.makeText(this, R.string.please_choose_location, Toast.LENGTH_SHORT).show();
            return;
        }
        if (fgtAddFood.price == null) {
            Toast.makeText(this, R.string.please_choose_price, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!fgtAddFood.pictureChosen) {
            Toast.makeText(this, R.string.please_choose_picutre, Toast.LENGTH_SHORT).show();
            return;
        }

        // 制作美食卡片

        String token = StrUtils.token();
        String title = fgtAddFood.etTitle.getText().toString();
        String comment = fgtAddFood.etComment.getText().toString();
        String location = fgtAddFood.poiItem.getTitle() + " " + fgtAddFood.poiItem.getSnippet();
        String latitude = String.valueOf(fgtAddFood.poiItem.getLatLonPoint().getLatitude());
        String longitude = String.valueOf(fgtAddFood.poiItem.getLatLonPoint().getLongitude());
        String price = fgtAddFood.price;

        mProgressDialog = ProgressDialog.show(AtyAddFood.this, null, "正在上传");
        Services.foodService()
                .publishFood(new FoodService.PublishFood(token, title, comment, location, latitude, longitude, price))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if ("successful".equals(resp.getState())) {

                        // 上传图片
                        Map<String, String> params = new HashMap<>();
                        params.put("token", StrUtils.token());
                        params.put("type", "-11");
                        params.put("foodcardid", resp.getId());
                        OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL, params, StrUtils.cropFilePath, StrUtils.MEDIA_TYPE_IMG, TAG,
                                new OkHttpUtils.SimpleOkCallBack() {

                                    @Override
                                    public void onFailure(IOException e) {
                                        mProgressDialog.dismiss();
                                        Toast.makeText(AtyAddFood.this,
                                                R.string.network_error,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                    @Override
                                    public void onResponse(String s) {
                                        mProgressDialog.dismiss();
                                        JSONObject j = OkHttpUtils.parseJSON(AtyAddFood.this, s);
                                        if (j == null) {
                                            return;
                                        }
                                        Toast.makeText(AtyAddFood.this,
                                                R.string.upload_food_finish,
                                                Toast.LENGTH_SHORT)
                                                .show();
                                        rx.Observable.timer(200, TimeUnit.MILLISECONDS)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(resp -> {
                                                    finish();
                                                });
                                    }
                                });
                    } else {
                        mProgressDialog.dismiss();
                        Toast.makeText(AtyAddFood.this,
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    mProgressDialog.dismiss();
                    Toast.makeText(AtyAddFood.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT)
                            .show();
                });
    }

    public void setFragment(Fragment frag) {
        android.app.FragmentManager fm = getFragmentManager();
        fm.beginTransaction()
                .replace(R.id.frame_container, frag)
                .commit();
        if (frag == fgtAddFood) {
            tvTitle.setText(R.string.edit_food_card);
            tvRight.setVisibility(View.VISIBLE);
        } else if (frag == fgtPrice) {
            tvTitle.setText(R.string.price_range);
            tvRight.setVisibility(View.GONE);
        } else if (frag == fgtMap) {
            tvTitle.setText(R.string.location);
            tvRight.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAddLocationButtonClick() {
        setFragment(fgtMap);
    }

    @Override
    public void onAddPriceButtonClick() {
        setFragment(fgtPrice);
    }

    @Override
    public void onAddPictureButtonClick() {
        Intent intent = new Intent(this, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        startActivityForResult(intent, AtyAddFood.REQUEST_IMAGE);
    }
}
