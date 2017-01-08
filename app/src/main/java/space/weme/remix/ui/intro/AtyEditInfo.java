package space.weme.remix.ui.intro;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.service.Services;
import space.weme.remix.service.UserService;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.ui.community.DatePickerFragment;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.OkHttpUtils;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.WSwitch;

/**
 * Created by Liujilong on 16/2/3.
 * liujilong.me@gmail.com
 */
public class AtyEditInfo extends BaseActivity {
    private static final String TAG = "AtyEditInfo";

    public static final String INTENT_EDIT = "intent_edit";
    public static final String INTENT_INFO = "intent_info";
    private boolean mEdit;
    private UserService.GetProfileByUserIdResp mUser;
    //private boolean

    private static final int REQUEST_IMAGE = 0xef;
    private static final int REQUEST_CITY = 0xff;
    private final int REQUEST_CROP = 400;

    private String mAvatarPath;

    private String education;

    @BindView(R.id.aty_editinfo_avatar)
    SimpleDraweeView mDrawAvatar;

    @BindView(R.id.aty_editinfo_edittext_name)
    EditText etName;

    @BindView(R.id.aty_editinfo_birth)
    TextView tvBirth;

    @BindView(R.id.aty_editinfo_phone)
    EditText etPhone;

    @BindView(R.id.aty_editinfo_school)
    TextView tvSchool;

    @BindView(R.id.aty_editinfo_education)
    Spinner spEducation;

    @BindView(R.id.aty_editinfo_major)
    EditText etMajor;

    @BindView(R.id.aty_editinfo_wechat)
    EditText etWeChat;

    @BindView(R.id.aty_editinfo_qq)
    EditText etQQ;

    @BindView(R.id.aty_editinfo_home)
    EditText etHome;

    @BindView(R.id.aty_editinfo_commit)
    TextView etCommit;

    @BindView(R.id.aty_editinfo_switch_gender)
    WSwitch wSwitch;

    ProgressDialog mProgressDialog;

    @OnClick(R.id.aty_editinfo_avatar)
    public void onAvatarClick() {
        Intent intent = new Intent(AtyEditInfo.this, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    @OnClick(R.id.aty_editinfo_birth)
    public void onBirthClick() {
        final DatePickerFragment datePicker = new DatePickerFragment();
        datePicker.setDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                tvBirth.setText(String.format("%4d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
            }
        });
        datePicker.show(getFragmentManager(), "DatePicker");
    }

    @OnClick(R.id.aty_editinfo_school)
    public void onSchoolClick() {
        Intent i = new Intent(AtyEditInfo.this, AtySearchCity.class);
        startActivityForResult(i, REQUEST_CITY);
    }

    @OnClick(R.id.aty_editinfo_commit)
    public void onCommitClick() {
        commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_editinfo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.aty_editinfo_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        ButterKnife.bind(this);
        bindViews();

        mEdit = getIntent().getBooleanExtra(INTENT_EDIT, false);
        if (mEdit) {
            String info = getIntent().getStringExtra(INTENT_INFO);
            if (info != null) {
                mUser = (new Gson()).fromJson(info, UserService.GetProfileByUserIdResp.class);
                showUserInfo();
            } else {
                String userId = StrUtils.id();
                String token = StrUtils.token();
                Services.userService()
                        .getProfileByUserId(new UserService.GetProfileByUserId(token, userId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(resp -> {
                            Log.d(TAG, "getProfileByUserId: " + resp);
                            if (resp.getId() == 0) {
                                finish();
                            } else {
                                mUser = resp;
                                showUserInfo();
                            }
                        }, ex -> {
                            Log.e(TAG, "getProfileByUserId: " + ex.toString());
                            Toast.makeText(AtyEditInfo.this,
                                    R.string.network_error,
                                    Toast.LENGTH_SHORT).show();
                        });
            }
        }
    }

    private void bindViews() {

        final String[] items = new String[]{
                getString(R.string.please_choose_education),
                getString(R.string.benke),
                getString(R.string.master),
                getString(R.string.doctor)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spEducation.setAdapter(adapter);
        spEducation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    education = null;
                    return;
                }
                education = items[position];
                LogUtils.i(TAG, education);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                education = null;
            }
        });
    }

    private void showUserInfo() {
        mDrawAvatar.setImageURI(Uri.parse(StrUtils.thumForID(mUser.getId() + "")));
        etName.setText(mUser.getName());
        boolean male = getResources().getString(R.string.male).equals(mUser.getGender());
        wSwitch.setOn(male);
        tvBirth.setText(mUser.getBirthday());
        etPhone.setText(mUser.getPhone());
        tvSchool.setText(mUser.getSchool());
        switch (mUser.getDegree()) {
            case "本科":
                spEducation.setSelection(1);
                break;
            case "硕士":
                spEducation.setSelection(2);
                break;
            case "博士":
                spEducation.setSelection(3);
                break;
        }
        etMajor.setText(mUser.getDepartment());
        etWeChat.setText(mUser.getWechat());
        etQQ.setText(mUser.getQq());
        etHome.setText(mUser.getHometown());
    }

    private void commit() {
        //LogUtils.d(TAG, swGender.isChecked()+"");
        if (etName.getText().toString().length() == 0) {
            makeToast(R.string.name_not_empty);
            return;
        }
        if (tvBirth.getText().toString().length() == 0) {
            makeToast(R.string.birth_not_empty);
            return;
        }
        if (etPhone.getText().toString().length() == 0) {
            makeToast(R.string.phone_not_empty);
            return;
        }
        if (tvSchool.getText().toString().length() == 0) {
            makeToast(R.string.school_not_empty);
            return;
        }
        if (education == null) {
            makeToast(R.string.choose_degree);
            return;
        }

        mProgressDialog = ProgressDialog.show(AtyEditInfo.this, null, getResources().getString(R.string.commenting));

        String gender = (wSwitch.isOn() ? getResources().getString(R.string.male) : getResources().getString(R.string.female));
        UserService.EditProfile ep = new UserService.EditProfile(
                StrUtils.token(),
                etName.getText().toString(),
                tvBirth.getText().toString(),
                education,
                etMajor.getText().toString(),
                gender,
                etHome.getText().toString(),
                etPhone.getText().toString(),
                etQQ.getText().toString(),
                tvSchool.getText().toString(),
                etWeChat.getText().toString()
        );
        Services.userService()
                .editProfile(ep)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "editProfile: " + resp.toString());
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                    if ("successful".equals(resp.getState())) {
                        if (mAvatarPath == null) {
                            uploadImageReturned();
                        } else {
                            uploadAvatar();
                        }
                    }
                }, ex -> {
                    Log.e(TAG, "editProfile: " + ex.getMessage());
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                    Toast.makeText(AtyEditInfo.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadAvatar() {
        ArrayMap<String, String> p = new ArrayMap<>();
        p.put("token", StrUtils.token());
        p.put("type", "0");
        p.put("number", "0");
        OkHttpUtils.uploadFile(StrUtils.UPLOAD_AVATAR_URL, p, StrUtils.cropFilePath, StrUtils.MEDIA_TYPE_IMG, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onFailure(IOException e) {
                uploadImageReturned();
            }

            @Override
            public void onResponse(String s) {
                if (mUser != null) {
                    ImagePipeline imagePipeline = Fresco.getImagePipeline();
                    imagePipeline.evictFromCache(Uri.parse(StrUtils.thumForID(mUser.getId() + "")));
                    imagePipeline.evictFromCache(Uri.parse(StrUtils.avatarForID(mUser.getId() + "")));
                    imagePipeline.evictFromCache(Uri.parse(StrUtils.cardForID(mUser.getId() + "")));
                }
                uploadImageReturned();
            }
        });
    }

    private void uploadImageReturned() {
        SharedPreferences sp = getSharedPreferences(StrUtils.SP_USER, MODE_PRIVATE);
        String gender = wSwitch.isOn() ? getResources().getString(R.string.male) : getResources().getString(R.string.female);
        sp.edit().putString(StrUtils.SP_USER_GENDER, gender).apply();
        if (mEdit) {
            finish();
        } else {
            Intent i = new Intent(AtyEditInfo.this, AtyLogin.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra(AtyLogin.INTENT_CLEAR, true);
            startActivity(i);
        }
    }

    private void makeToast(int string_id) {
        Toast.makeText(this, string_id, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_IMAGE) {
            List<String> paths = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
            mAvatarPath = paths.get(0);
            performCrop(mAvatarPath);
            //mDrawAvatar.setImageURI(Uri.parse("file://"+mAvatarPath));
        } else if (requestCode == REQUEST_CITY) {
            String name = data.getStringExtra(AtySearchCity.INTENT_UNIVERSITY);
            tvSchool.setText(name);
        } else if (requestCode == REQUEST_CROP) {
            Fresco.getImagePipeline().evictFromCache(Uri.parse("file://" + StrUtils.cropFilePath));
            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            mDrawAvatar.getHierarchy().setRoundingParams(roundingParams);
            mDrawAvatar.setImageURI(Uri.parse("file://" + StrUtils.cropFilePath));
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

    @Override
    protected String tag() {
        return TAG;
    }
}
