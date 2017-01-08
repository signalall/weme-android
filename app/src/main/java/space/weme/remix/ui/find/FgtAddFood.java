package space.weme.remix.ui.find;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.facebook.drawee.view.SimpleDraweeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import space.weme.remix.R;
import space.weme.remix.ui.base.BaseFragment;
import space.weme.remix.util.BitmapUtils;
import space.weme.remix.util.StrUtils;

/**
 * Created by Liujilong on 2016/3/3.
 * liujilong.me@gmail.com
 */
public class FgtAddFood extends BaseFragment {
    private static final String TAG = "FgtAddFood";

    AtyAddFood aty;

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

    String price;
    boolean pictureChosen = false;
    PoiItem poiItem;

    public static FgtAddFood newInstance() {
        Bundle args = new Bundle();
        final FgtAddFood fragment = new FgtAddFood();
        fragment.setArguments(args);
        return fragment;
    }

    @OnClick(R.id.fgt_add_food_picture)
    public void onPictureDraweeClick() {
        Intent intent = new Intent(aty, MultiImageSelectorActivity.class);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 1);
        intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_SINGLE);
        aty.startActivityForResult(intent, AtyAddFood.REQUEST_IMAGE);
    }

    @OnClick(R.id.fgt_add_food_price)
    public void onPriceClick() {
        aty.switchToFragment(aty.fgtPrice);
    }

    @OnClick(R.id.fgt_add_food_location)
    public void onLocationClick() {
        aty.switchToFragment(aty.fgtMap);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fgt_add_food, container, false);
        ButterKnife.bind(this, v);
        aty = (AtyAddFood) getActivity();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (price != null) {
            tvPrice.setText(price);
        }
        if (pictureChosen) {
            int width = pictureDrawee.getWidth();
            int height = pictureDrawee.getHeight();
            BitmapUtils.showResizedPicture(pictureDrawee, Uri.parse("file://" + StrUtils.cropFilePath), width, height);
        }
        if (poiItem != null) {
            String location = poiItem.getTitle() + poiItem.getSnippet();
            tvLocation.setText(location);
        }
    }

    void setPrice(String price) {
        this.price = price;
    }

    @Override
    protected String tag() {
        return TAG;
    }
}
