package space.weme.remix.ui.intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.ui.base.BaseActivity;
import space.weme.remix.ui.main.MainActivity;
import space.weme.remix.util.BitmapUtils;
import space.weme.remix.util.DimensionUtils;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 16/1/20.
 * liujilong.me@gmail.com
 */
public class AtyWelcome extends BaseActivity {
    private static final String TAG = "AtyWelcome";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_welcome);

        final SimpleDraweeView iv = (SimpleDraweeView) findViewById(R.id.background);
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME) // "res"
                .path(String.valueOf(R.mipmap.splash_background))
                .build();
        int width = DimensionUtils.getDisplay().widthPixels;
        int height = DimensionUtils.getDisplay().heightPixels;
        BitmapUtils.showResizedPicture(iv, uri, width, height);

        rx.Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    SharedPreferences sp = getSharedPreferences(StrUtils.SP_USER, MODE_PRIVATE);
                    String token = sp.getString(StrUtils.SP_USER_TOKEN, "");
                    if (token.equals("")) {
                        loginIn();
                        overridePendingTransition(0, 0);
                    } else {
                        main();
                        overridePendingTransition(0, 0);
                    }
                    finish();
                }, ex -> {

                });
    }

    private void loginIn() {
        Intent i = new Intent(AtyWelcome.this, AtyLogin.class);
        i.putExtra(AtyLogin.INTENT_UPDATE, true);
        startActivity(i);
    }

    private void main() {
        Intent i = new Intent(AtyWelcome.this, MainActivity.class);
        i.putExtra(MainActivity.INTENT_UPDATE, true);
        startActivity(i);
    }


    @Override
    protected String tag() {
        return TAG;
    }
}
