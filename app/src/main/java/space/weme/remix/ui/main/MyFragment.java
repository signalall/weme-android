package space.weme.remix.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.Constants;
import space.weme.remix.R;
import space.weme.remix.model.User;
import space.weme.remix.service.Services;
import space.weme.remix.service.UserService;
import space.weme.remix.ui.base.BaseFragment;
import space.weme.remix.ui.user.AtyFriend;
import space.weme.remix.ui.user.AtyInfo;
import space.weme.remix.ui.user.AtyMessage;
import space.weme.remix.ui.user.AtyNearBy;
import space.weme.remix.ui.user.AtySetting;
import space.weme.remix.ui.user.AtyUserActivity;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.LogUtils;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 16/1/24.
 * liujilong.mMe@gmail.com
 */
public class MyFragment extends BaseFragment {

    private static final String TAG = MyFragment.class.getSimpleName();

    @BindView(R.id.fgt_me_avatar)
    SimpleDraweeView mDraweeAvatar;

    @BindView(R.id.fgt_me_layout)
    LinearLayout llLayout;

    @BindView(R.id.fgt_me_name)
    TextView mNameTextView;

    @BindView(R.id.fgt_me_count)
    TextView mUnreadMessageTextView;

    private View mQRCodeView;
    private Bitmap mQRBitmap;
    private User mMe;
    private boolean isQrCodeShowing = false;

    public static MyFragment newInstance() {
        Bundle args = new Bundle();
        final MyFragment fragment = new MyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_my, container, false);
        ButterKnife.bind(this, rootView);
        setupViews();
        drawQRCodeAsync().subscribe(dummy -> {
        }, ex -> {
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUnreadMessage();
        loadProfile();
        mDraweeAvatar.setImageURI(Uri.parse(StrUtils.thumForID(StrUtils.id() + "")));
    }

    @OnClick(R.id.fgt_me_me)
    public void onMeClick() {
        Intent i = new Intent(getActivity(), AtyInfo.class);
        i.putExtra(AtyInfo.ID_INTENT, StrUtils.id());
        getActivity().startActivity(i);
    }

    @OnClick(R.id.fgt_me_friend)
    public void onFriendClick() {
        getActivity()
                .startActivity(new Intent(getActivity(), AtyFriend.class));
    }

    @OnClick(R.id.fgt_me_message)
    public void onMessageClick() {
        getActivity()
                .startActivity(new Intent(getActivity(), AtyMessage.class));
    }

    @OnClick(R.id.fgt_me_activity)
    public void onActivityClick() {
        getActivity()
                .startActivity(new Intent(getActivity(), AtyUserActivity.class));

    }

    @OnClick(R.id.fgt_me_location)
    void onLocationClick() {
        getActivity().startActivity(new Intent(getActivity(), AtyNearBy.class));
    }


    @OnClick(R.id.fgt_me_setting)
    void onSettingClick() {
        getActivity().startActivity(new Intent(getActivity(), AtySetting.class));
    }

    @OnClick(R.id.fgt_me_qrcode)
    public void onQRCodeClick() {
        showQRCode();
    }

    @Override
    protected String tag() {
        return TAG;
    }

    private void setupViews() {
        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        mDraweeAvatar.getHierarchy().setRoundingParams(roundingParams);
    }

    private void loadUnreadMessage() {
        Services.userService()
                .getUnreadMessage(new UserService.GetUnreadMessage(StrUtils.token()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if (Constants.STATE_SUCCESSFUL.equals(resp.getState())) {
                        int number = Integer.parseInt(resp.getNumber());
                        if (number <= 0) {
                            mUnreadMessageTextView.setVisibility(View.GONE);
                            return;
                        }
                        mUnreadMessageTextView.setVisibility(View.VISIBLE);
                        if (number < 10) {
                            mUnreadMessageTextView.setText(number);
                            mUnreadMessageTextView.setTextSize(16);
                        } else if (number < 100) {
                            mUnreadMessageTextView.setText(number);
                            mUnreadMessageTextView.setTextSize(14);
                        } else {
                            mUnreadMessageTextView.setTextSize(12);
                            mUnreadMessageTextView.setText(R.string.more_than_99);
                        }
                    } else {
                        Toast.makeText(getActivity(),
                                resp.getReason(),
                                Toast.LENGTH_SHORT).show();
                    }
                }, ex -> {
                    Log.e(TAG, "getUnreadMessage: " + ex.toString());
                    Toast.makeText(getActivity(),
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadProfile() {
        Services.userService()
                .getProfile(new UserService.GetProfile(StrUtils.token()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getProfile: " + resp.toString());
                    mMe = resp;
                    mNameTextView.setText(mMe.getName());
                    showQRCodeUserInfo();
                }, ex -> {
                    Log.e(TAG, "getProfile: " + ex.toString());
                    Toast.makeText(getActivity(),
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void showQRCode() {
        LogUtils.i(TAG, "showQRCode");
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogTransparent);
        mQRCodeView = LayoutInflater.from(getActivity()).inflate(R.layout.qrcode_user, llLayout, false);
        final int size = DimensionUtils.getDisplay().widthPixels * 4 / 5 - DimensionUtils.dp2px(32);
        final ImageView imageView = (ImageView) mQRCodeView.findViewById(R.id.qr_code);
        ViewGroup.LayoutParams param = imageView.getLayoutParams();
        param.width = size;
        param.height = size;

        if (mQRBitmap != null) {
            imageView.setImageBitmap(mQRBitmap);
        } else {
            drawQRCodeAsync().subscribe(dummy -> {
                imageView.setImageBitmap(mQRBitmap);
            }, ex -> {
            });
        }

        SimpleDraweeView avatar = (SimpleDraweeView) mQRCodeView.findViewById(R.id.avatar);
        avatar.setImageURI(Uri.parse(StrUtils.thumForID(StrUtils.id())));

        showQRCodeUserInfo();

        mQRCodeView.setOnClickListener(v -> {
            dialog.dismiss();
            isQrCodeShowing = false;
        });

        dialog.setContentView(mQRCodeView);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER;
        dialog.show();
        isQrCodeShowing = true;
    }

    private void showQRCodeUserInfo() {
        if (mQRCodeView != null && isQrCodeShowing) {
            TextView name = (TextView) mQRCodeView.findViewById(R.id.name);
            name.setText(mMe.getName());
            TextView school = (TextView) mQRCodeView.findViewById(R.id.school);
            school.setText(mMe.getSchool());
            ImageView iv = (ImageView) mQRCodeView.findViewById(R.id.gender);
            boolean male = getResources().getString(R.string.male).equals(mMe.getGender());
            iv.setImageResource(male ? R.mipmap.boy : R.mipmap.girl);
        }
    }

    private void drawQRCode() throws WriterException {
        if (mQRBitmap != null) {
            return;
        }
        QRCodeWriter writer = new QRCodeWriter();
        String data = "weme://user/" + StrUtils.id();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 0); /* default = 4 */
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        final int size = DimensionUtils.getDisplay().widthPixels * 4 / 5 - DimensionUtils.dp2px(32);
        BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints);
        mQRBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                if (bitMatrix.get(x, y))
                    mQRBitmap.setPixel(x, y, 0x3e5d9e);
                else
                    mQRBitmap.setPixel(x, y, Color.WHITE);
            }
        }
    }

    private Observable<Object> drawQRCodeAsync() {
        return rx.Observable
                .create(subscriber -> {
                    try {
                        drawQRCode();
                        subscriber.onNext("");
                    } catch (WriterException e) {
                        subscriber.onError(e);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
