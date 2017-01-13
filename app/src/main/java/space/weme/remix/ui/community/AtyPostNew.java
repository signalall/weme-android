package space.weme.remix.ui.community;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.service.PostService;
import space.weme.remix.service.Services;
import space.weme.remix.ui.base.AtyImage;
import space.weme.remix.util.BitmapUtils;
import space.weme.remix.util.OkHttpUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.GridLayout;

/**
 * Created by Liujilong on 2016/1/31.
 * liujilong.me@gmail.com
 */
public class AtyPostNew extends AtyImage {
    private static final String TAG = "AtyPostNew";
    public static final String INTENT_ID = "topicID";

    private String mTopicID;
    private AtomicInteger mSendImageResponseNum;

    @BindView(R.id.aty_post_new_title)
    EditText mTitle;

    @BindView(R.id.aty_post_new_content)
    EditText mContent;

    @BindView(R.id.aty_post_new_add_image)
    SimpleDraweeView mDrawAddImage;

    @BindView(R.id.aty_post_new_images)
    GridLayout mImageGrids;

    @BindView(R.id.aty_post_new_send)
    TextView mSend;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_post_new);
        ButterKnife.bind(this);
        mTopicID = getIntent().getStringExtra(INTENT_ID);

        mDrawAddImage.setImageURI(Uri.parse("res:/" + R.mipmap.add_image));
        mDrawAddImage.setOnClickListener(mListener);

        mChosenPicturePathList = new ArrayList<>();
        mSendImageResponseNum = new AtomicInteger();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE && resultCode == Activity.RESULT_OK) {
            List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            mChosenPicturePathList.clear();
            mChosenPicturePathList.addAll(paths);
            mImageGrids.removeAllViews();
            for (String path : mChosenPicturePathList) {
                SimpleDraweeView image = new SimpleDraweeView(AtyPostNew.this);
                int size = mImageGrids.getCellSize();
                BitmapUtils.showResizedPicture(image, Uri.parse("file://" + path), size, size);
                mImageGrids.addView(image);
                image.setOnClickListener(mListener);
            }
            if (mImageGrids.getChildCount() < 9) {
                mImageGrids.addView(mDrawAddImage);
            }
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * 发布Post
     */
    @OnClick(R.id.aty_post_new_send)
    public void onSendClick() {
        String title = mTitle.getText().toString();
        String content = mContent.getText().toString();
        if (title.length() == 0) {
            return;
        }
        mProgressDialog = ProgressDialog.show(AtyPostNew.this, null, getResources().getString(R.string.posting));
        Services.postService()
                .publishPost(new PostService.PublishPost(StrUtils.token(), mTopicID, title, content))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "publishPost: " + resp.toString());
                    mProgressDialog.dismiss();
                    if (!resp.containsKey("id")) {
                        Toast.makeText(AtyPostNew.this, R.string.post_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String postId = resp.get("id");
                    if (mChosenPicturePathList.size() == 0) {
                        setResult(RESULT_OK);
                        finish();
                        return;
                    }
                    ArrayMap<String, String> p = new ArrayMap<>();
                    p.put("token", StrUtils.token());
                    p.put("type", "-4");
                    p.put("postid", postId);
                    mSendImageResponseNum.set(0);
                    for (int number = 0; number < mChosenPicturePathList.size(); number++) {
                        p.put("number", String.valueOf(number));
                        String path = mChosenPicturePathList.get(number);
                        OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL, p, path, StrUtils.MEDIA_TYPE_IMG, TAG, new OkHttpUtils.SimpleOkCallBack() {
                            @Override
                            public void onFailure(IOException e) {
                                uploadImageReturned();
                            }

                            @Override
                            public void onResponse(String s) {
                                uploadImageReturned();
                            }
                        });
                    }
                }, ex -> {
                    Log.e(TAG, "publishPost: " + ex.getMessage());
                    mProgressDialog.dismiss();
                    Toast.makeText(AtyPostNew.this, R.string.post_failed, Toast.LENGTH_SHORT).show();
                });
    }


    private void uploadImageReturned() {
        int num = mSendImageResponseNum.incrementAndGet();
        if (num == mChosenPicturePathList.size()) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
