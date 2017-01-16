package space.weme.remix.ui.activity;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;

/**
 * Created by Joyce on 2017/1/16.
 */

/**
 * // http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
 */
public class CollapsingToolbarTitleOnlyCollapsed implements AppBarLayout.OnOffsetChangedListener {

    private final ExpandedTitleListener mExpanded;
    private final CollapsingToolbarLayout collapsingToolbarLayout;
    private boolean isShow = false;
    private int scrollRange = -1;

    public CollapsingToolbarTitleOnlyCollapsed(CollapsingToolbarLayout collapsingToolbarLayout, ExpandedTitleListener expanededd) {
        this.collapsingToolbarLayout = collapsingToolbarLayout;
        this.mExpanded = expanededd;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (scrollRange == -1) {
            scrollRange = appBarLayout.getTotalScrollRange();
        }
        if (scrollRange + verticalOffset == 0) {
            String title = mExpanded.getExpandedTitle();
            collapsingToolbarLayout.setTitle(title);
            isShow = true;
        } else if (isShow) {
            collapsingToolbarLayout.setTitle(" "); //carefull there should a space between double quote otherwise it wont work
            isShow = false;
        }
    }

    public interface ExpandedTitleListener {
        String getExpandedTitle();
    }
}
