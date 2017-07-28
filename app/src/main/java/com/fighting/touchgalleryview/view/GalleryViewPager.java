
package com.fighting.touchgalleryview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.fighting.touchgalleryview.R;
import java.util.List;

/**
 * Created by fighting on 2017/7/28.
 */
public class GalleryViewPager extends ViewPager {
    PointF last;
    /**当前摸动当ImageView*/
    public TouchImageView mCurrentView;
    public static int errorImageResId = R.drawable.ic_launcher;

    public GalleryViewPager(Context context) {
        super(context);
    }
    public GalleryViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private float[] handleMotionEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                last = new PointF(event.getX(0), event.getY(0));
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                PointF curr = new PointF(event.getX(0), event.getY(0));
                return new float[]{curr.x - last.x, curr.y - last.y};
        }
        return null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        float [] difference = handleMotionEvent(event);
        if (mCurrentView.pagerCanScroll()) {
            return super.onInterceptTouchEvent(event);
        }else {
            if (difference != null && mCurrentView.onRightSide && difference[0] < 0){
                return super.onInterceptTouchEvent(event);
            }
            if (difference != null && mCurrentView.onLeftSide && difference[0] > 0){
                return super.onInterceptTouchEvent(event);
            }
            if (difference == null && ( mCurrentView.onLeftSide || mCurrentView.onRightSide)){
                return super.onInterceptTouchEvent(event);
            }
        }
        return false;
    }

    /**
     * 加载网络图片
     * */
    public void setUrlList(List<String> urls){
        this.setAdapter(new VpGallgeryAdapter(getContext(),urls,null,mOnItemClickListener));
        this.setOffscreenPageLimit(3);
    }
    /**
     * 直接加载bitmap图片
     *
     * 注意：图片过大导致内溢出
     * */
    public void setBitmapList(List<Bitmap> bitmaps){
        this.setAdapter(new VpGallgeryAdapter(getContext(),null,bitmaps,mOnItemClickListener));
        this.setOffscreenPageLimit(3);
    }
    /**
     * 设置图片加载失败后的图片
     * */
    public void setErrorImageResource(int ResId){
        errorImageResId = ResId;
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        PagerAdapter adapter = getAdapter();
        if(adapter != null){
            ((VpGallgeryAdapter)adapter).setOnItemClickListener(mOnItemClickListener);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    public OnItemClickListener mOnItemClickListener;
}