package space.weme.remix.ui.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
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

import org.json.JSONObject;

import java.util.EnumMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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
import space.weme.remix.util.OkHttpUtils;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 16/1/24.
 * liujilong.me@gmail.com
 */
public class FgtMe extends BaseFragment {

    private static final String TAG = "FgtMe";

    @BindView(R.id.fgt_me_avatar)
    SimpleDraweeView mDraweeAvatar;

    @BindView(R.id.fgt_me_layout)
    LinearLayout llLayout;

    @BindView(R.id.fgt_me_name)
    TextView mTvName;

    @BindView(R.id.fgt_me_count)
    TextView mTvCount;

    View qrCodeView;
    boolean isQrCodeShowing = false;

    User me;
    Bitmap mQRBitmap;

    public static FgtMe newInstance() {
        Bundle args = new Bundle();
        final FgtMe fragment = new FgtMe();
        fragment.setArguments(args);
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                LogUtils.i(TAG, "in Idle Handler");
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            fragment.drawQRCode();
                        } catch (WriterException e) {
                            e.printStackTrace();
                        }
                    }
                }.run();
                return false;
            }
        });
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fgt_me, container, false);
        ButterKnife.bind(this, rootView);

        RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
        roundingParams.setRoundAsCircle(true);
        mDraweeAvatar.getHierarchy().setRoundingParams(roundingParams);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        fetchUnreadMessage();
        fetchNameInfo();
        mDraweeAvatar.setImageURI(Uri.parse(StrUtils.thumForID(StrUtils.id() + "")));
    }

    private void fetchUnreadMessage() {
        ArrayMap<String, String> param = new ArrayMap<>();
        param.put("token", StrUtils.token());
        OkHttpUtils.post(StrUtils.GET_UNREAD_MESSAGE_URL, param, TAG, new OkHttpUtils.SimpleOkCallBack() {
            @Override
            public void onResponse(String s) {
                //LogUtils.i(TAG, s);
                JSONObject j = OkHttpUtils.parseJSON(getActivity(), s);
                if (j == null) {
                    return;
                }
                String number = j.optString("number");
                int count;
                try {
                    count = Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    return;
                }
                if (count <= 0) {
                    mTvCount.setVisibility(View.GONE);
                    return;
                }
                mTvCount.setVisibility(View.VISIBLE);
                if (count < 10) {
                    mTvCount.setText(number);
                    mTvCount.setTextSize(16);
                } else if (count < 100) {
                    mTvCount.setText(number);
                    mTvCount.setTextSize(14);
                } else {
                    mTvCount.setTextSize(12);
                    mTvCount.setText(R.string.more_than_99);
                }
            }
        });
    }

    private void fetchNameInfo() {
        Services.userService()
                .getProfile(new UserService.GetProfile(StrUtils.token()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    Log.d(TAG, "getProfile: " + resp.toString());
                    me = resp;
                    mTvName.setText(me.getName());
                    showQRCodeUserInfo();
                }, ex -> {
                    Log.e(TAG, "getProfile: " + ex.toString());
                    Toast.makeText(getActivity(),
                            R.string.network_error,
                            Toast.LENGTH_SHORT).show();
                });
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

    private void showQRCode() {
        LogUtils.i(TAG, "showQRCode");
        final Dialog dialog = new Dialog(getActivity(), R.style.DialogTransparent);
        qrCodeView = LayoutInflater.from(getActivity()).inflate(R.layout.qrcode_user, llLayout, false);
        final int size = DimensionUtils.getDisplay().widthPixels * 4 / 5 - DimensionUtils.dp2px(32);
        final ImageView qr_code = (ImageView) qrCodeView.findViewById(R.id.qr_code);
        ViewGroup.LayoutParams param = qr_code.getLayoutParams();
        param.width = size;
        param.height = size;

        if (mQRBitmap != null) {
            qr_code.setImageBitmap(mQRBitmap);
        } else {
            drawQRCodeWithCallBack(qr_code);
        }

        SimpleDraweeView avatar = (SimpleDraweeView) qrCodeView.findViewById(R.id.avatar);
        avatar.setImageURI(Uri.parse(StrUtils.thumForID(StrUtils.id())));

        showQRCodeUserInfo();


        qrCodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isQrCodeShowing = false;
            }
        });


        dialog.setContentView(qrCodeView);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();
        wmlp.gravity = Gravity.CENTER;
        dialog.show();
        isQrCodeShowing = true;
    }

    private void showQRCodeUserInfo() {
        if (qrCodeView != null && isQrCodeShowing) {
            TextView name = (TextView) qrCodeView.findViewById(R.id.name);
            name.setText(me.getName());
            TextView school = (TextView) qrCodeView.findViewById(R.id.school);
            school.setText(me.getSchool());
            ImageView iv = (ImageView) qrCodeView.findViewById(R.id.gender);
            boolean male = getResources().getString(R.string.male).equals(me.getGender());
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

    private void drawQRCodeWithCallBack(final ImageView iv) {
        new Thread() {
            @Override
            public void run() {
                try {
                    drawQRCode();
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            iv.setImageBitmap(mQRBitmap);
                        }
                    });
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        }.run();
    }
}
