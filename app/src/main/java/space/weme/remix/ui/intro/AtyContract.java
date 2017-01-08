package space.weme.remix.ui.intro;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;

/**
 * Created by Liujilong on 16/2/3.
 * liujilong.me@gmail.com
 */
public class AtyContract extends Activity {

    private static final String TAG = AtyContract.class.getSimpleName();

    @BindView(R.id.aty_contract_text)
    TextView tvContract;

    @BindView(R.id.aty_contract_return)
    TextView tvReturn;

    @OnClick(R.id.aty_contract_return)
    public void onContractReturnClick() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_contract);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.aty_contract_toolbar);
        toolbar.setTitle(R.string.user_contract);
        toolbar.setTitleTextColor(Color.WHITE);
        Observable
                .<String>create(subscriber -> {
                    InputStream in = getResources().openRawResource(R.raw.contract);
                    try {
                        int length = in.available();
                        byte[] buffer = new byte[length];
                        in.read(buffer);
                        String result = new String(buffer, "utf-8");
                        in.close();
                        subscriber.onNext(result);
                    } catch (IOException ex) {
                        subscriber.onError(ex);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contract -> {
                    tvContract.setText(contract);
                }, ex -> {
                    Log.e(TAG, "Contract: " + ex.getMessage());
                });
    }
}
