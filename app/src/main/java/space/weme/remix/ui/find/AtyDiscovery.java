package space.weme.remix.ui.find;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.model.User;
import space.weme.remix.service.Services;
import space.weme.remix.service.UserService;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.ui.user.AtyMessageReply;
import space.weme.remix.util.BitmapUtils;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.OkHttpUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.Card;

/**
 * Todo: bugs
 * Created by Liujilong on 16/2/14.
 * liujilong.me@gmail.com
 */
public class AtyDiscovery extends BaseActivity {

    private static final String TAG = "AtyDiscovery";

    private Subscription getRecommondUser;

    private static final int STATE_FIRST = 0x1;
    private static final int STATE_ANIMATING = 0x2;
    private static final int STATE_READY = 0x3;

    private int state = STATE_FIRST;

    private Card mCard;

    Dialog dialog;

    private float mTranslationY;

    private boolean isLoading = false;

    private float preValue = 0;

    private List<User> userList;

    private int currentIndex = 0;

    @BindView(R.id.aty_discovery_background)
    FrameLayout flBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_discovery);
        ButterKnife.bind(this);
        setupViews();
    }

    @Optional
    @OnClick(R.id.aty_discovery_back)
    public void onImageViewDiscoveryBackClick() {
        finish();
    }

    @OnClick(R.id.aty_discovery_more)
    public void onImageViewDiscoveryMoreClick() {
        ivMoreClicked();
    }

    @OnClick(R.id.aty_discovery_text)
    public void onDiscoveryTextClick() {
        if (state != STATE_ANIMATING) {
            startAnimation();
        }
    }

    @Optional
    @OnClick(R.id.aty_discovery_option_cancel)
    public void onDiscoveryOptionCancel() {
        if (currentIndex >= userList.size()) return;
        String id = userList.get(currentIndex).getId() + "";
        dialog.dismiss();
    }

    @Optional
    @OnClick(R.id.aty_discovery_option_follow)
    public void onDiscoveryOptionFollow() {
        if (currentIndex >= userList.size()) return;
        String id = userList.get(currentIndex).getId() + "";
        followUser(id);
        dialog.dismiss();
    }

    @Optional
    @OnClick(R.id.aty_discovery_option_message)
    public void onDiscoveryOptionMessage() {
        if (currentIndex >= userList.size()) return;
        String id = userList.get(currentIndex).getId() + "";
        Intent i = new Intent(AtyDiscovery.this, AtyMessageReply.class);
        i.putExtra(AtyMessageReply.INTENT_ID, id);
        startActivity(i);
        dialog.dismiss();
    }

    public void showLikeEachOther(final User user) {
        View v = LayoutInflater.from(this).inflate(R.layout.aty_discovery_like_each_other, flBackground, false);
        final Dialog d = new Dialog(AtyDiscovery.this, R.style.DialogLike);
        d.setContentView(v);
        flBackground.setDrawingCacheEnabled(true);
        setBackground(flBackground.getDrawingCache(), v);
        TextView tvCong = (TextView) v.findViewById(R.id.dialog_cong);
        Typeface tf = Typeface.createFromAsset(getAssets(), "scriptina_pro.otf");
        tvCong.setTypeface(tf, Typeface.BOLD);

        TextView tvText = (TextView) v.findViewById(R.id.like_text);
        String text = "你和" + user.getName() + "互相喜欢对方";
        tvText.setText(text);

        SimpleDraweeView yourAvatar = (SimpleDraweeView) v.findViewById(R.id.my_avatar);
        SimpleDraweeView userAvatar = (SimpleDraweeView) v.findViewById(R.id.user_avatar);
        ViewGroup.MarginLayoutParams params1 = (ViewGroup.MarginLayoutParams) yourAvatar.getLayoutParams();
        ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams) userAvatar.getLayoutParams();
        int size = DimensionUtils.getDisplay().widthPixels / 13;
        params1.width = size * 5;
        params1.height = size * 5;
        params1.setMargins(size * 2, 0, 0, 0);
        yourAvatar.setLayoutParams(params1);
        params2.width = size * 5;
        params2.height = size * 5;
        params2.setMargins(size * 6, 0, 0, 0);
        userAvatar.setLayoutParams(params2);
        yourAvatar.setImageURI(Uri.parse(StrUtils.thumForID(StrUtils.id())));
        userAvatar.setImageURI(Uri.parse(user.getAvatar()));

        v.findViewById(R.id.continue_find).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        v.findViewById(R.id.send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                Intent i = new Intent(AtyDiscovery.this, AtyMessageReply.class);
                i.putExtra(AtyMessageReply.INTENT_ID, user.getId() + "");
                startActivity(i);
            }
        });


        WindowManager.LayoutParams wmlp = d.getWindow().getAttributes();
        wmlp.width = DimensionUtils.getDisplay().widthPixels;
        wmlp.height = DimensionUtils.getDisplay().heightPixels;
        d.show();
    }

    @SuppressWarnings("deprecation")
    private void setupViews() {
        mCard = Card.fromXML(this, flBackground);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(0, 0);
        DisplayMetrics displayMetrics = DimensionUtils.getDisplay();
        params.width = displayMetrics.widthPixels * 7 / 10;
        params.height = params.width * 3 / 2;
        params.gravity = Gravity.CENTER;
        mCard.setLayoutParams(params);
        mCard.setAvatarSize();
        mTranslationY = displayMetrics.heightPixels / 2 + 21 * displayMetrics.widthPixels / 40;
        mCard.setTranslationY(mTranslationY);
        flBackground.addView(mCard);
        BitmapDrawable b = (BitmapDrawable) getResources().getDrawable(R.mipmap.spade_bk);
        if (b != null) {
            setBackground(b.getBitmap());
        }
        CardView cardView = (CardView) findViewById(R.id.aty_discovery_card);
        cardView.setLayoutParams(params);
        cardView.setTranslationY(mTranslationY - DimensionUtils.dp2px(64));
        userList = new ArrayList<>();
    }

    private void ivMoreClicked() {
        if (state != STATE_READY) {
            return;
        }
        dialog = new Dialog(AtyDiscovery.this, R.style.DialogSlideAnim);
        View content = LayoutInflater.from(this).inflate(R.layout.aty_discovery_option, flBackground, false);
        dialog.setContentView(content);
        ButterKnife.bind(this, content);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.BOTTOM | Gravity.START;
        wmlp.x = 0;   //x position
        wmlp.y = 0;   //y position
        wmlp.width = DimensionUtils.getDisplay().widthPixels;
        dialog.show();
    }

    private void followUser(String id) {
        ArrayMap<String, String> param = new ArrayMap<>();
        param.put("token", StrUtils.token());
        param.put("id", id);
        OkHttpUtils.post(StrUtils.FOLLOW_USER, param, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onFailure(IOException e) {
                Toast.makeText(AtyDiscovery.this, R.string.follow_fail, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(String s) {
                JSONObject j = OkHttpUtils.parseJSON(AtyDiscovery.this, s);
                if (j == null) {
                    Toast.makeText(AtyDiscovery.this, R.string.follow_fail, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AtyDiscovery.this, R.string.follow_success, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setBackground(Bitmap b) {
        setBackground(b, flBackground);
    }

    private void setBackground(final Bitmap b, final View v) {
        Log.i("Time", "Label 1 : " + System.currentTimeMillis());
        if (b == null) {
            return;
        }
        Observable
                .<Bitmap>create(subscriber -> {
                    Log.i("Time", "Label 2 : " + System.currentTimeMillis());
                    Bitmap sized = BitmapUtils.scale(b, 40, 40 * b.getHeight() / b.getWidth());
                    Log.i("Time", "Label 3 : " + System.currentTimeMillis());
                    int radius = 5;
                    Bitmap blur = BitmapUtils.blur(sized, radius);
                    Log.i("Time", "Label 4 : " + System.currentTimeMillis());
                    subscriber.onNext(blur);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(blur -> {
                    // important, clear cache of flBackground used in showLikeEachOther
                    flBackground.setDrawingCacheEnabled(false);
                    LogUtils.i("Time", "Label 5 : " + System.currentTimeMillis());
                    v.setBackgroundDrawable(new BitmapDrawable(getResources(), blur));
                }, ex -> {

                });
    }


    private void startAnimation() {
        LogUtils.d(TAG, "clicked");
        currentIndex++;
        if (currentIndex >= userList.size() && !isLoading) {
            fetchUser();
            currentIndex = 0;
        }
        ObjectAnimator a1 = ObjectAnimator.ofFloat(mCard, "TranslationY", mTranslationY, 0)
                .setDuration(500);
        ObjectAnimator a2 = ObjectAnimator.ofFloat(mCard, "RotationX", 0, 180).setDuration(500);
        a2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCard.setAvatarSize();
                float value = (float) animation.getAnimatedValue();
                LogUtils.d(TAG, value + "");
                if (preValue < 90 && value >= 90) {
                    mCard.turnOver();
                    if (userList.size() != 0) {
                        mCard.showUser(userList.get(currentIndex));
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
                    mCard.setAvatarSize();
                    float value = (float) animation.getAnimatedValue();

                    if (preValue > 90 && value <= 90) {
                        mCard.turnOver();
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

    private void fetchUser() {
        if (getRecommondUser != null) {
            getRecommondUser.unsubscribe();
        }
        isLoading = true;
        getRecommondUser = Services.userService()
                .getRecommendUser(new UserService.GetRecommendUser(StrUtils.token()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getRecommendUser: " + resp.toString());
                    isLoading = false;
                    if (resp.getResult() != null) {
                        userList.clear();
                        userList.addAll(resp.getResult());
                        if (userList.size() > 0) {
                            mCard.showUser(userList.get(0));
                        }
                    }
                }, ex -> {
                    Log.d(TAG, "getRecommendUser: " + ex.toString());
                    isLoading = false;
                    Toast.makeText(AtyDiscovery.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCard.stopMedia();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getRecommondUser != null) {
            getRecommondUser.unsubscribe();
        }
    }
}
