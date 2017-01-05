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
import okhttp3.MediaType;
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
 * 回复Post
 */
public class AtyPostReply extends AtyImage {
    private static final String TAG = "AtyPostReply";
    public static final String INTENT_ID = "intent_id";
    public static final String INTENT_CONTENT = "intent_content";
    protected static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/*");

    @BindView(R.id.send)
    TextView mSend;

    @BindView(R.id.main_editor)
    EditText mEditor;
    ProgressDialog mProgressDialog;

    @BindView(R.id.add_image)
    SimpleDraweeView mDrawAddImage;

    @BindView(R.id.select_image_view)
    GridLayout mImageGrids;

    private String mPostID;

    private AtomicInteger mSendImageResponseNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_post_aty_reply);
        ButterKnife.bind(this);
        mPostID = getIntent().getStringExtra(INTENT_ID);
        String content = getIntent().getStringExtra(INTENT_CONTENT);

        mDrawAddImage.setImageURI(Uri.parse("res:/" + R.mipmap.add_image));
        mDrawAddImage.setOnClickListener(mListener);
        mEditor.setText(content);

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
                SimpleDraweeView image = new SimpleDraweeView(AtyPostReply.this);
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


    @OnClick(R.id.send)
    public void onSendClick() {
        if (mEditor.getText().length() == 0) {
            return;
        }
        mProgressDialog = ProgressDialog.show(AtyPostReply.this, null, getResources().getString(R.string.commenting));
        Services.postService()
                .commentToPost(new PostService.CommentToPost(StrUtils.token(), mPostID, mEditor.getText().toString()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, resp.toString());
                    if (!resp.containsKey("id")) {
                        mProgressDialog.dismiss();
                        Toast.makeText(AtyPostReply.this, R.string.comment_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String commendId = resp.get("id");
                    if (mChosenPicturePathList.size() == 0) {
                        setResult(RESULT_OK);
                        finish();
                        return;
                    }
                    ArrayMap<String, String> p = new ArrayMap<>();
                    p.put("token", StrUtils.token());
                    p.put("type", "-7");
                    p.put("commentid", commendId);
                    mSendImageResponseNum.set(0);
                    for (int number = 0; number < mChosenPicturePathList.size(); number++) {
                        p.put("number", String.format("%d", number));
                        String path = mChosenPicturePathList.get(number);

                        OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL, p, path, MEDIA_TYPE_JPG, TAG, new OkHttpUtils.SimpleOkCallBack() {
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
                    mProgressDialog.dismiss();
                    Toast.makeText(AtyPostReply.this, R.string.comment_failed, Toast.LENGTH_SHORT).show();
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
