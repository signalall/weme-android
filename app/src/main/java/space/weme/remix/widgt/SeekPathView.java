package space.weme.remix.widgt;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import space.weme.remix.R;
import space.weme.remix.util.DimensionUtils;

/**
 * Created by Liujilong on 2016/2/21.
 * liujilong.me@gmail.com
 */
public class SeekPathView extends FrameLayout implements View.OnClickListener {
    public interface OnFoodClickListener {
        void onFoodClick();
    }

    public interface OnFriendClickListener {
        void onFriendClick();
    }

    OnFoodClickListener onFoodClickListener;
    OnFriendClickListener onFriendClickListener;

    int mStrokeSize;

    int mWidth;
    int mHeight;

    Path mFoodPath;
    Path mFriendPath;
    Path mCirclePath;
    Paint mPathPaint;

    ImageView mEarthImageView;
    ImageView mFriendImageView;
    ImageView mFoodImageView;
    ImageView mPlaneImageView;

    LinearLayout mFoodLinearLayout;
    LinearLayout mFriendLinearLayout;

    Path mFoodFullPath;
    Path mFriendFullPath;
    Path mCircleFullPath;
    PathMeasure mFullFoodPathMeasure;
    PathMeasure mFullFriendPathMeasure;
    PathMeasure mFullImagePathMeasure;

    ValueAnimator mAnimator;

    public SeekPathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_seek_path, this);
        setupViews();
    }

    public SeekPathView(Context context) {
        this(context, null);
        LayoutInflater.from(context).inflate(R.layout.view_seek_path, this);
        setupViews();
    }

    private void setupViews() {
        mEarthImageView = (ImageView) findViewById(R.id.earth_image_view);
        mFoodImageView = (ImageView) findViewById(R.id.food_image_view);
        mFriendImageView = (ImageView) findViewById(R.id.friend_image_view);
        mFoodLinearLayout = (LinearLayout) findViewById(R.id.food_linear_layout);
        mFriendLinearLayout = (LinearLayout) findViewById(R.id.friend_linear_layout);
        mPlaneImageView = (ImageView) findViewById(R.id.plane_image_view);

        setWillNotDraw(false);

        mPathPaint = new Paint();
        mPathPaint.setColor(Color.WHITE);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(10);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setColor(0x7fffffff);

        mStrokeSize = DimensionUtils.dp2px(2);

        int size = DimensionUtils.getDisplay().widthPixels / 6;

        ViewGroup.LayoutParams params = mEarthImageView.getLayoutParams();
        params.width = size;
        params.height = size;
        mEarthImageView.setLayoutParams(params);

        params = mFoodImageView.getLayoutParams();
        params.height = size;
        params.width = size;
        mFoodImageView.setLayoutParams(params);

        params = mFriendImageView.getLayoutParams();
        params.height = size;
        params.width = size;
        mFriendImageView.setLayoutParams(params);

        params = mPlaneImageView.getLayoutParams();
        params.width = size / 2;
        params.height = size / 2;
        mPlaneImageView.setLayoutParams(params);

        mFoodLinearLayout.setOnClickListener(this);
        mFriendLinearLayout.setOnClickListener(this);

        startAnimation();
    }

    public void setSeekingGirlFriend() {
        mFriendImageView.setImageResource(R.mipmap.find_girl);
    }

    public void setSeekingBoyFriend() {
        mFriendImageView.setImageResource(R.mipmap.find_boy);
    }

    public void startAnimation() {
        ObjectAnimator a1 = ObjectAnimator.ofFloat(this, "Angle", 0, 360).setDuration(20000);
        a1.setInterpolator(new LinearInterpolator());
        a1.start();

        mAnimator = ValueAnimator.ofFloat(0, 359).setDuration(20000);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updatePosition(animation);
            }
        });
        mAnimator.setRepeatMode(ValueAnimator.RESTART);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.start();
    }

    public void stopAnimation() {
        clearAnimation();
        mAnimator.cancel();
    }

    public void setAngle(float angle) {
        int size = Math.min(mWidth, mHeight);

        // Fiend path
        mFriendPath = new Path();
        RectF rect = new RectF(mWidth / 2 - size / 2, mHeight / 2 - size / 4, mWidth / 2 + size / 2, mHeight / 2 + size / 4);
        mFriendPath.addArc(rect, 0, angle);
        Matrix mMatrix1 = new Matrix();
        mMatrix1.postRotate(45, rect.centerX(), rect.centerY());
        mFriendPath.transform(mMatrix1);

        // Food path
        mFoodPath = new Path();
        mFoodPath.addArc(rect, 0, angle);
        Matrix mMatrix2 = new Matrix();
        mMatrix2.postRotate(-45, rect.centerX(), rect.centerY());
        mFoodPath.transform(mMatrix2);

        // Circle path
        mCirclePath = new Path();
        int radius = size * 3 / 25;
        RectF r = new RectF(mWidth / 2 - radius, mHeight / 2 - radius, mWidth / 2 + radius, mHeight / 2 + radius);
        mCirclePath.addArc(r, 0, angle);

        mPathPaint.setStrokeWidth(mStrokeSize * angle / 360);

        invalidate();
    }

    public void updatePosition(ValueAnimator animator) {
        if (mFullFoodPathMeasure == null) {
            setupSize(DimensionUtils.getDisplay().widthPixels, DimensionUtils.getDisplay().heightPixels);
        }
        float value = (float) animator.getAnimatedValue();
        //coordinates will be here
        float pos[] = {0f, 0f};
        //get coordinates of the middle point
        mFullFoodPathMeasure.getPosTan(mFullFoodPathMeasure.getLength() * value / 359, pos, null);
        mFoodLinearLayout.setX(pos[0] - mFoodLinearLayout.getWidth() / 2);
        mFoodLinearLayout.setY(pos[1] - mFoodLinearLayout.getHeight() / 2);

        mFullFriendPathMeasure.getPosTan(mFullFriendPathMeasure.getLength() * value / 359, pos, null);
        mFriendLinearLayout.setX(pos[0] - mFriendLinearLayout.getWidth() / 2);
        mFriendLinearLayout.setY(pos[1] - mFriendLinearLayout.getHeight() / 2);

        mFullImagePathMeasure.getPosTan(mFullImagePathMeasure.getLength() * value / 359, pos, null);
        mPlaneImageView.setX(pos[0] - mPlaneImageView.getWidth() / 2);
        mPlaneImageView.setY(pos[1] - mPlaneImageView.getHeight() / 2);
        mPlaneImageView.setRotation(value);

        mEarthImageView.setRotation(value);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(mFoodPath, mPathPaint);
        canvas.drawPath(mFriendPath, mPathPaint);
        canvas.drawPath(mCirclePath, mPathPaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setupSize(w, h);
    }

    private void setupSize(int w, int h) {
        mWidth = w;
        mHeight = h;
        setAngle(0);
        int size = Math.min(mWidth, mHeight);

        // Friend path
        mFriendFullPath = new Path();
        RectF rect = new RectF(mWidth / 2 - size / 2, mHeight / 2 - size / 4, mWidth / 2 + size / 2, mHeight / 2 + size / 4);
        mFriendFullPath.addArc(rect, 0, 360);
        Matrix mMatrix1 = new Matrix();
        mMatrix1.postRotate(45, rect.centerX(), rect.centerY());
        mFriendFullPath.transform(mMatrix1);

        // Food path
        mFoodFullPath = new Path();
        mFoodFullPath.addArc(rect, 0, 360);
        Matrix mMatrix2 = new Matrix();
        mMatrix2.postRotate(-45, rect.centerX(), rect.centerY());
        mFoodFullPath.transform(mMatrix2);

        // Circle path
        mCircleFullPath = new Path();
        int radius = size * 3 / 25;
        RectF re = new RectF(mWidth / 2 - radius, mHeight / 2 - radius, mWidth / 2 + radius, mHeight / 2 + radius);
        mCircleFullPath.addArc(re, 0, 360);

        mFullFoodPathMeasure = new PathMeasure(mFoodFullPath, false);
        mFullFriendPathMeasure = new PathMeasure(mFriendFullPath, false);
        mFullImagePathMeasure = new PathMeasure(mCircleFullPath, false);
    }


    public OnFoodClickListener getOnFoodClickListener() {
        return onFoodClickListener;
    }

    public void setOnFoodClickListener(OnFoodClickListener onFoodClickListener) {
        this.onFoodClickListener = onFoodClickListener;
    }

    public OnFriendClickListener getOnFriendClickListener() {
        return onFriendClickListener;
    }

    public void setOnFriendClickListener(OnFriendClickListener onFriendClickListener) {
        this.onFriendClickListener = onFriendClickListener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.food_linear_layout:
                if (onFoodClickListener != null)
                    onFoodClickListener.onFoodClick();
                break;
            case R.id.friend_linear_layout:
                if (onFriendClickListener != null)
                    onFriendClickListener.onFriendClick();
                break;
        }
    }
}
