package space.weme.remix.ui.find;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.Food;
import space.weme.remix.service.FoodService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.util.BitmapUtils;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.CardFood;

/**
 * Todo: bugs
 * Created by Liujilong on 16/2/14.
 * liujilong.me@gmail.com
 */
public class AtyDiscoveryFood extends BaseActivity {
    private static final String TAG = "AtyDiscoveryFood";

    private static final int STATE_FIRST = 0x1;
    private static final int STATE_ANIMATING = 0x2;
    private static final int STATE_READY = 0x3;

    private int state = STATE_FIRST;

    private CardFood mCard;

    @BindView(R.id.aty_discovery_background)
    FrameLayout flBackground;

    private float mTranslationY;

    ExecutorService exec;
    private Handler mHandler;

    private float preValue;

    private List<Food> foodList;
    private int currentIndex = 0;
    private boolean isLoading = false;

    AMapLocation mapLocation;

    @BindView(R.id.aty_discovery_card)
    CardView cardView;

    @OnClick(R.id.aty_discovery_back)
    public void onBackClick() {
        finish();
    }

    @OnClick(R.id.aty_discovery_more)
    public void onMoreClick() {
        ivMoreClicked();
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_discovery_food);
        ButterKnife.bind(this);


        exec = Executors.newSingleThreadExecutor();
        mHandler = new Handler();

        BitmapDrawable b = (BitmapDrawable) getResources().getDrawable(R.mipmap.spade_bk);
        if (b != null) {
            setBackground(b.getBitmap());
        }

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
        DisplayMetrics displayMetrics = DimensionUtils.getDisplay();

        params.width = displayMetrics.widthPixels * 7 / 10;
        params.height = params.width * 3 / 2;
        params.gravity = Gravity.CENTER;
        mCard = CardFood.fromXML(this, flBackground, params);
        flBackground.addView(mCard);

        mTranslationY = displayMetrics.heightPixels / 2 + 21 * displayMetrics.widthPixels / 40;
        cardView.setLayoutParams(params);
        cardView.setTranslationY(mTranslationY - DimensionUtils.dp2px(64));

        mCard.setTranslationY(mTranslationY);

        foodList = new ArrayList<>();

        // get current location
        final AMapLocationClient mLocationClient = new AMapLocationClient(getApplicationContext());
        AMapLocationListener mLocationListener = new AMapLocationListener() {

            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                mapLocation = aMapLocation;
                mLocationClient.stopLocation();
            }
        };
        mLocationClient.setLocationListener(mLocationListener);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setWifiActiveScan(true);
        mLocationOption.setMockEnable(false);
        mLocationOption.setInterval(2000);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();

    }

    @OnClick(R.id.aty_discovery_text)
    public void onDiscoveryClick() {
        if (state != STATE_ANIMATING) {
            startAnimation();
        }
    }

    private void fetchFood() {
        isLoading = true;
        Services.foodService()
                .getRecommendFood(new FoodService.GetRecommendFood(StrUtils.token()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    isLoading = false;
                    LogUtils.i(TAG, resp.toString());
                    if (resp.getResult() == null) {
                        return;
                    }
                    foodList.clear();
                    foodList.addAll(resp.getResult());
                    if (foodList.size() > 0) {
                        mCard.showFood(foodList.get(0));
                    }
                }, ex -> {
                    isLoading = false;
                    Toast.makeText(AtyDiscoveryFood.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
    }

    @SuppressWarnings("deprecation")
    public void setBackground(final Bitmap b) {
        LogUtils.i("Time", "Label 1 : " + System.currentTimeMillis());
        if (b == null) {
            return;
        }
        exec.execute(new Runnable() {
            @Override
            public void run() {
                LogUtils.i("Time", "Label 2 : " + System.currentTimeMillis());
                Bitmap sized = BitmapUtils.scale(b, 40, 40 * b.getHeight() / b.getWidth());
                LogUtils.i("Time", "Label 3 : " + System.currentTimeMillis());
                final int radius = 5;
                final Bitmap blur = BitmapUtils.blur(sized, radius);
                LogUtils.i("Time", "Label 4 : " + System.currentTimeMillis());
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.i("Time", "Label 5 : " + System.currentTimeMillis());
                        flBackground.setBackgroundDrawable(new BitmapDrawable(getResources(), blur));
                    }
                });
            }
        });
    }


    private void startAnimation() {
        currentIndex++;
        if (currentIndex >= foodList.size() && !isLoading) {
            fetchFood();
            currentIndex = 0;
        }
        ObjectAnimator a1 = ObjectAnimator.ofFloat(mCard, "TranslationY", mTranslationY, 0)
                .setDuration(500);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(mCard, "RotationX", 0, 180).setDuration(500);
        a2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();

                mCard.resize();
                LogUtils.d(TAG, "" + value);
                if (preValue < 90 && value >= 90) {
                    mCard.turnToFront();
                    if (foodList.size() != 0) {
                        mCard.showFood(foodList.get(currentIndex));
                    }
                }
                preValue = value;
            }
        });
        a2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                state = STATE_READY;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                state = STATE_READY;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        if (state == STATE_FIRST) {
            state = STATE_ANIMATING;
            AnimatorSet set = new AnimatorSet();
            set.playSequentially(a1, a2);
            set.start();
        } else {
            state = STATE_ANIMATING;
            ObjectAnimator a3 = ObjectAnimator.ofFloat(mCard, "RotationX", 180, 0).setDuration(500);
            a3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();

                    mCard.resize();
                    if (preValue > 90 && value <= 90) {
                        mCard.turnToBack();
                    }
                    preValue = value;
                }
            });
            ObjectAnimator a4 = ObjectAnimator.ofFloat(mCard, "TranslationY", 0, mTranslationY)
                    .setDuration(500);

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(a3, a4, a1, a2);
            set.start();
        }
    }


    private void ivMoreClicked() {
        final Dialog dialog = new Dialog(AtyDiscoveryFood.this, R.style.DialogSlideAnim);
        View content = LayoutInflater.from(this).inflate(R.layout.aty_discovery_food_option, flBackground, false);
        View.OnClickListener popupListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.aty_discovery_option_add) {
                    LogUtils.i(TAG, "add food card");
                    startActivity(new Intent(AtyDiscoveryFood.this, AtyAddFood.class));
                }
                dialog.dismiss();
            }
        };
        content.findViewById(R.id.aty_discovery_option_cancel).setOnClickListener(popupListener);
        content.findViewById(R.id.aty_discovery_option_add).setOnClickListener(popupListener);
        dialog.setContentView(content);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM | Gravity.START;
        wmlp.x = 0;   //x position
        wmlp.y = 0;   //y position
        wmlp.width = DimensionUtils.getDisplay().widthPixels;
        dialog.show();
    }

    public LatLng getCurrentLatLng() {
        if (mapLocation == null) {
            return null;
        } else {
            return new LatLng(mapLocation.getLatitude(), mapLocation.getLongitude());
        }
    }


    @Override
    public String tag() {
        return TAG;
    }
}
