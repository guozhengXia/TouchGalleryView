
package com.fighting.touchgalleryview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.fighting.touchgalleryview.view.GalleryItemView;
import com.fighting.touchgalleryview.view.GalleryViewPager;
import java.util.List;

/**
 * Created by fighting on 2017/7/28.
 */
public class VpGallgeryAdapter extends PagerAdapter {
    private  GalleryViewPager.OnItemClickListener mOnItemClickListener;
    protected  List<String> mResources;
	protected  List<Bitmap> mBitmaps;
    protected final Context mContext;
    protected int mCurrentPosition = -1;

    public VpGallgeryAdapter(Context context, List<String> resources, List<Bitmap> bitmaps, GalleryViewPager.OnItemClickListener listener){
        this.mResources = resources;
        this.mBitmaps = bitmaps;
        this.mOnItemClickListener = listener;
        this.mContext = context;
    }


    @Override
    public int getCount()
    {
        return mResources.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, final int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (mCurrentPosition == position) return;
        GalleryViewPager galleryContainer = ((GalleryViewPager)container);
        if (galleryContainer.mCurrentView != null) {
            galleryContainer.mCurrentView.resetScale();
        }
        mCurrentPosition = position;
        galleryContainer.mCurrentView = ((GalleryItemView)object).touchImageView;
    }
    @Override
    public View instantiateItem(ViewGroup collection, final int position){
        GalleryItemView galleryItemView = new GalleryItemView(mContext);
        if(mResources != null){
            galleryItemView.setImageUrl(mResources.get(position));//加载网络图片
        }
        if(mBitmaps != null){
            galleryItemView.setImageBitmap(mBitmaps.get(position));//加载网络图片
        }
        if(mOnItemClickListener != null){
            galleryItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(position);
                }
            });
        }


        collection.addView(galleryItemView);
        return galleryItemView;
    }
    @Override
    public void destroyItem(ViewGroup collection, int position, Object view){
        collection.removeView((View) view);
    }
    @Override
    public boolean isViewFromObject(View view, Object object){
        return view.equals(object);
    }

    public void setOnItemClickListener(GalleryViewPager.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}