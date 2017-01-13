package space.weme.remix.ui.find;

import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
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

    @BindView(R.id.title_text)
    TextView tvTitle;

    @BindView(R.id.right_text)
    TextView tvRight;

    @BindView(R.id.frame_container)
    FrameLayout frameContainer;

    FgtAddFood fgtAddFood;
    FgtPrice fgtPrice;
    FgtFoodMap fgtMap;

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

    @OnClick(R.id.right_text)
    public void onRightTextClick() {
        uploadFood();
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
        ArrayMap<String, String> map = new ArrayMap<>();
        map.put("token", StrUtils.token());
        map.put("title", fgtAddFood.etTitle.getText().toString());
        map.put("comment", fgtAddFood.etComment.getText().toString());
        map.put("location", fgtAddFood.poiItem.getTitle() + " " + fgtAddFood.poiItem.getSnippet());
        map.put("latitude", Double.toString(fgtAddFood.poiItem.getLatLonPoint().getLatitude()));
        map.put("longitude", Double.toString(fgtAddFood.poiItem.getLatLonPoint().getLongitude()));
        map.put("price", fgtAddFood.price);
        OkHttpUtils.post(StrUtils.PUBLISH_CARD, map, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onResponse(String s) {
                JSONObject j = OkHttpUtils.parseJSON(AtyAddFood.this, s);
                if (j == null) {
                    return;
                }
                String id = j.optString("id");
                ArrayMap<String, String> params = new ArrayMap<>();
                params.put("token", StrUtils.token());
                params.put("type", "-11");
                params.put("foodcardid", id);
                OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL, params, StrUtils.cropFilePath, StrUtils.MEDIA_TYPE_IMG, TAG, new OkHttpUtils.SimpleOkCallBack() {
                    @Override
                    public void onResponse(String s) {
                        JSONObject j = OkHttpUtils.parseJSON(AtyAddFood.this, s);
                        if (j == null) {
                            Toast.makeText(AtyAddFood.this, R.string.upload_food_fail, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(AtyAddFood.this, R.string.upload_food_finish, Toast.LENGTH_SHORT).show();

                        rx.Observable.timer(500, TimeUnit.MILLISECONDS)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(resp -> {
                                    finish();
                                });
                    }
                });
            }
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
