package space.weme.remix.ui.find;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import space.weme.remix.R;
import space.weme.remix.ui.base.BaseFragment;
import space.weme.remix.util.BitmapUtils;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 2016/3/3.
 * liujilong.me@gmail.com
 */
public class FgtAddFood extends BaseFragment {


    interface OnAddPictureButtonClickListener {
        void onAddPictureButtonClick();
    }

    interface OnAddPriceButtonClickListener {
        void onAddPriceButtonClick();
    }

    interface OnAddLocationButtonClickListener {
        void onAddLocationButtonClick();
    }

    private static final String TAG = "FgtAddFood";

    private Context mContext;

    String price;

    boolean pictureChosen = false;

    PoiItem poiItem;

    @BindView(R.id.fgt_add_food_picture)
    SimpleDraweeView pictureDrawee;

    @BindView(R.id.edit_text_food_name)
    EditText etTitle;

    @BindView(R.id.edit_text_comment)
    EditText etComment;

    @BindView(R.id.fgt_add_food_location)
    LinearLayout llLocation;

    @BindView(R.id.fgt_add_food_price)
    LinearLayout llPrice;

    @BindView(R.id.fgt_add_text_location)
    TextView tvLocation;

    @BindView(R.id.fgt_add_text_price)
    TextView tvPrice;

    public static FgtAddFood newInstance() {
        Bundle args = new Bundle();
        final FgtAddFood fragment = new FgtAddFood();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @OnClick(R.id.fgt_add_food_picture)
    public void onPictureDraweeClick() {
        try {
            ((OnAddPictureButtonClickListener) mContext).onAddPictureButtonClick();
        } catch (ClassCastException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @OnClick(R.id.fgt_add_food_price)
    public void onPriceClick() {
        try {
            ((OnAddPriceButtonClickListener) mContext).onAddPriceButtonClick();
        } catch (ClassCastException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @OnClick(R.id.fgt_add_food_location)
    public void onLocationClick() {
        try {
            ((OnAddLocationButtonClickListener) mContext).onAddLocationButtonClick();
        } catch (ClassCastException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fgt_add_food, container, false);
        ButterKnife.bind(this, v);
        mContext = getActivity();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        rx.Observable.timer(0, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resp -> {
                    if (price != null) {
                        tvPrice.setText(price);
                    }
                    if (pictureChosen) {
                        int width = pictureDrawee.getWidth();
                        int height = pictureDrawee.getHeight();
                        if (width > 0 && height > 0)
                            BitmapUtils.showResizedPicture(pictureDrawee, Uri.parse("file://" + StrUtils.cropFilePath), width, height);
                    }
                    if (poiItem != null) {
                        String location = poiItem.getTitle() + poiItem.getSnippet();
                        tvLocation.setText(location);
                    }
                });

    }

    public void setPicture() {
        Fresco.getImagePipeline().evictFromCache(Uri.parse("file://" + StrUtils.cropFilePath));
        int width = pictureDrawee.getWidth();
        int height = pictureDrawee.getHeight();
        BitmapUtils.showResizedPicture(pictureDrawee, Uri.parse("file://" + StrUtils.cropFilePath), width, height);
        pictureChosen = true;
    }

    void setPrice(String price) {
        this.price = price;
    }

    @Override
    protected String tag() {
        return TAG;
    }
}
