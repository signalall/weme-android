package space.weme.remix.ui.intro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.service.Services;
import space.weme.remix.service.UserService;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.CountDownButton;

/**
 * Created by Liujilong on 16/2/3.
 * liujilong.me@gmail.com
 */
public class AtyRegister extends BaseActivity {
    private static final String TAG = "AtyRegister";

    Pattern phone = Pattern.compile(StrUtils.PHONE_PATTERN);

    List<Subscription> mSubscriptions = new ArrayList<>();

    CountDownButton mCountDown;

    ProgressDialog mProgressDialog;

    TextWatcher mTextWatcher;

    @BindView(R.id.phone)
    EditText etName;

    @BindView(R.id.login_password)
    EditText etPass;

    @BindView(R.id.login_copy_password)
    EditText etPass2;

    @BindView(R.id.verification_code)
    EditText etCode;

    @BindView(R.id.gain_verification_code)
    Button btnCode;

    @BindView(R.id.aty_register_contract)
    TextView tvContract;

    @BindView(R.id.register)
    TextView tvRegister;

    @BindView(R.id.aty_register_error)
    TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_register);
        ButterKnife.bind(this);

        mCountDown = new CountDownButton(btnCode, btnCode.getText().toString(), 60, 1);
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkText();
            }
        };
        etName.addTextChangedListener(mTextWatcher);
        etPass.addTextChangedListener(mTextWatcher);
        etPass2.addTextChangedListener(mTextWatcher);
        etCode.addTextChangedListener(mTextWatcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        for (Subscription sub : mSubscriptions) {
            sub.unsubscribe();
        }
    }

    @Override
    protected String tag() {
        return TAG;
    }

    @OnClick(R.id.gain_verification_code)
    public void onBtnCodeClick() {
        sendSmsCode();
        mCountDown.start();
    }

    @OnClick(R.id.aty_register_contract)
    public void onButtonContractClick() {
        Intent i = new Intent(AtyRegister.this, AtyContract.class);
        startActivity(i);
    }

    @OnClick(R.id.register)
    public void onTextEditRegisterClick() {
        register();
    }

    private void sendSmsCode() {
        String phone = etName.getText().toString();
        Subscription sub = Services.userService()
                .sendSmsCode(new UserService.SendSmsCode(phone, "1"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if ("successful".equals(resp.getState())) {
                        Toast.makeText(AtyRegister.this, R.string.send_code_complete, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(AtyRegister.this, R.string.phone_registered, Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "sendSmsCode: " + ex.getMessage());
                    Toast.makeText(AtyRegister.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT)
                            .show();
                });
        mSubscriptions.add(sub);
    }

    private void checkText() {
        if (!phone.matcher(etName.getText()).matches()) {
            tvRegister.setEnabled(false);
            tvError.setText(R.string.please_input_phone);
            btnCode.setEnabled(false);
            return;
        } else {
            btnCode.setEnabled(true);
        }
        if (etCode.getText().length() == 0) {
            tvRegister.setEnabled(false);
            tvError.setText(R.string.code_length);
            return;
        }
        if (etPass.getText().length() < 6) {
            tvRegister.setEnabled(false);
            tvError.setText(R.string.password_long_6);
            return;
        }
        if (!etPass.getText().toString().equals(etPass2.getText().toString())) {
            tvRegister.setEnabled(false);
            tvError.setText(R.string.password_not_equal);
            return;
        }
        tvRegister.setEnabled(true);
        tvError.setText("");
    }


    private void register() {
        Log.d(TAG, "register");
        String name = etName.getText().toString();
        String passwordMd5 = StrUtils.md5(etPass.getText().toString());
        String code = etCode.getText().toString();
        mProgressDialog = ProgressDialog.show(AtyRegister.this, null, "正在注册");

        Subscription sub = Services.userService()
                .registerPhone(new UserService.RegisterPhone(name, passwordMd5, code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "registerPhone: " + resp);
                    mProgressDialog.dismiss();
                    if ("successful".equals(resp.getState())) {
                        String userId = resp.getUserId();
                        String token = resp.getToken();
                        SharedPreferences sp = getSharedPreferences(StrUtils.SP_USER, MODE_PRIVATE);
                        sp.edit()
                                .putString(StrUtils.SP_USER_ID, userId)
                                .putString(StrUtils.SP_USER_TOKEN, token).apply();

                        Toast.makeText(AtyRegister.this,
                                "注册成功，请编辑个人信息",
                                Toast.LENGTH_SHORT)
                                .show();

                        // 1 second later
                        rx.Observable.timer(500, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(then -> {
                                    Intent i = new Intent(AtyRegister.this, AtyEditInfo.class);
                                    startActivity(i);
                                    finish();
                                }, ex -> {

                                });
                    } else { // failed
                        Toast.makeText(AtyRegister.this,
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "registerPhone: " + ex.getMessage());
                    mProgressDialog.dismiss();
                    Toast.makeText(AtyRegister.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT)
                            .show();
                });
        mSubscriptions.add(sub);
    }
}
