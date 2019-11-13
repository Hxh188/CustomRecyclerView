package com.hxh.recyclerview_lib.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView垂直分割线
 */
public class RecyclerViewDrawableDivider extends RecyclerView.ItemDecoration {

    private Drawable mDrawable;
    private int marginLeft;
    public RecyclerViewDrawableDivider(Context context, int resId) {
        //在这里我们传入作为Divider的Drawable对象
        mDrawable = context.getResources().getDrawable(resId);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            //以下计算主要用来确定绘制的位置
            final int top = child.getBottom() + params.bottomMargin - 1;
            final int bottom = top + mDrawable.getIntrinsicHeight();
            mDrawable.setBounds(left + marginLeft, top, right, bottom);
            mDrawable.draw(c);
        }
    }
    //垂直布局： mDrawable.getIntrinsicHeight()， 水平布局： mDrawable.getIntrinsicWidth()
    @Override
    public void getItemOffsets(Rect outRect, int position, RecyclerView parent) {
        outRect.set(0, 0, 0, mDrawable.getIntrinsicHeight());
    }
}