package space.weme.remix.ui.intro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import space.weme.remix.ui.TextValidator;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.util.StrUtils;
import space.weme.remix.widgt.CountDownButton;

/**
 * Created by Liujilong on 2016/3/5.
 * liujilong.me@gmail.com
 */
public class AtyForget extends BaseActivity {
    private static final String TAG = "AtyForget";

    Pattern phone = Pattern.compile(StrUtils.PHONE_PATTERN);

    CountDownButton mCountDown;

    ProgressDialog mProgressDialog;

    List<Subscription> mSubscriptions = new ArrayList<>();

    @BindView(R.id.phone)
    EditText etPhone;

    @BindView(R.id.verification_code)
    EditText etCode;

    @BindView(R.id.login_password)
    EditText etPass;

    @BindView(R.id.login_copy_password)
    EditText etPass2;

    @BindView(R.id.gain_verification_code)
    Button btnCode;

    @BindView(R.id.reset_button)
    TextView tvReset;

    @BindView(R.id.aty_reset_error)
    TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_forget);
        ButterKnife.bind(this);

        mCountDown = new CountDownButton(btnCode, btnCode.getText().toString(), 60, 1);
        etPhone.addTextChangedListener(new TextValidator(etPhone) {
            @Override
            public void validate(TextView textView, String text) {
                validateInput();
            }
        });
        etPass.addTextChangedListener(new TextValidator(etPass) {

            @Override
            public void validate(TextView textView, String text) {
                validateInput();
            }
        });
        etPass2.addTextChangedListener(new TextValidator(etPass2) {
            @Override
            public void validate(TextView textView, String text) {
                validateInput();
            }
        });
        etCode.addTextChangedListener(new TextValidator(etCode) {
            @Override
            public void validate(TextView textView, String text) {
                validateInput();
            }
        });
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
    void onGainVerificationCodeClick() {
        sendCode();
        mCountDown.start();
    }

    @OnClick(R.id.reset_button)
    void onResetClick() {
        resetPassword();
    }

    private void sendCode() {
        String phone = etPhone.getText().toString();
        Subscription sub = Services.userService()
                .sendSmsCode(new UserService.SendSmsCode(phone, "2"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "sendSmsCode: " + resp.toString());
                    if ("successful".equals(resp.getState())) {
                        Toast.makeText(AtyForget.this, R.string.send_code_complete, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(AtyForget.this, resp.getReason(), Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "sendSmsCode: " + ex.getMessage());
                    Toast.makeText(AtyForget.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
        mSubscriptions.add(sub);
    }

    private void validateInput() {
        if (!phone.matcher(etPhone.getText()).matches()) {
            tvReset.setEnabled(false);
            tvError.setText(R.string.please_input_phone);
            btnCode.setEnabled(false);
            return;
        } else {
            btnCode.setEnabled(true);
        }
        if (etCode.getText().length() == 0) {
            tvReset.setEnabled(false);
            tvError.setText(R.string.code_length);
            return;
        }
        if (etPass.getText().length() < 6) {
            tvReset.setEnabled(false);
            tvError.setText(R.string.password_long_6);
            return;
        }
        if (!etPass.getText().toString().equals(etPass2.getText().toString())) {
            tvReset.setEnabled(false);
            tvError.setText(R.string.password_not_equal);
            return;
        }
        tvReset.setEnabled(true);
        tvError.setText("");
    }

    private void resetPassword() {
        String phone = etPhone.getText().toString();
        String password = StrUtils.md5(etPass.getText().toString());
        String code = etCode.getText().toString();
        mProgressDialog = ProgressDialog.show(AtyForget.this, null, "正在重置密码");
        Subscription sub = Services.userService()
                .resetPassword(new UserService.ResetPassword(phone, password, code))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "resetPassword: " + resp.toString());
                    mProgressDialog.dismiss();
                    if ("successful".equals(resp.getState())) {
                        SharedPreferences sp = getSharedPreferences(StrUtils.SP_USER, MODE_PRIVATE);
                        sp.edit()
                                .putString(StrUtils.SP_USER_ID, resp.getId())
                                .putString(StrUtils.SP_USER_TOKEN, resp.getToken())
                                .apply();
                        Toast.makeText(AtyForget.this,
                                "重置密码成功，正在登陆中",
                                Toast.LENGTH_SHORT)
                                .show();
                        rx.Observable.timer(500, TimeUnit.MILLISECONDS)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(dummy -> {
                                    Intent i = new Intent(AtyForget.this, AtyLogin.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    i.putExtra(AtyLogin.INTENT_CLEAR, true);
                                    startActivity(i);
                                });
                    } else {
                        Toast.makeText(AtyForget.this,
                                resp.getReason(),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }, ex -> {
                    Log.e(TAG, "resetPassword: " + ex.getMessage());
                    mProgressDialog.dismiss();
                    Toast.makeText(AtyForget.this,
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
        mSubscriptions.add(sub);
    }

}
