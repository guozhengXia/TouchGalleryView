package com.fighting.touchgalleryview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import com.fighting.touchgalleryview.R;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by fighting on 2017/7/28.
 */

public class GalleryItemView extends RelativeLayout {
    private final Context mContext;
    private View rootView;
    public TouchImageView touchImageView;
    private ProgressBar mProgressBar;

    public GalleryItemView(Context context) {
        this(context,null);
    }

    public GalleryItemView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public GalleryItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        rootView = View.inflate(mContext, R.layout.view_gallgery_picture,null);
        initView();
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        addView(rootView,layoutParams);
    }

    private void initView() {
        touchImageView = (TouchImageView) rootView.findViewById(R.id.iv_touch_zoom);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pb_wait_gallgery);
    }

    /**
     * 直接加载bitmap图片
     *
     * 注意：图片过大导致内溢出
     * */
    public void setImageBitmap(Bitmap bitmap){
        if (bitmap == null) {
            touchImageView.setScaleType(ImageView.ScaleType.CENTER);
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), GalleryViewPager.errorImageResId);
            touchImageView.setImageBitmap(bitmap);
        } else {
            touchImageView.setScaleType(ImageView.ScaleType.MATRIX);
            touchImageView.setImageBitmap(bitmap);
        }
        touchImageView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    /**
     * 加载网络图片
     * */
    public void setImageUrl(String url){
        new PictureAsyncTask().execute(url);
    }

    /**
     * 设置点击监听器
     * */
    @Override
    public void setOnClickListener(OnClickListener l) {
        touchImageView.setOnClickListener(l);
    }




    /*
    * 注意：此时不能使用Picasso。原因：Picasso底层使用的是setImageDrawable,而TouchImageView需要setImageBitmap方法。
    *
    * 此方法最好，由于底层使用了线程池，首个加载很快
    * */
    public class PictureAsyncTask extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                Thread.sleep(1000);
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(params[0])
                        .build();
                Response response = client.newCall(request).execute();
               return getZoomBitmap(response,320,480);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            setImageBitmap(bitmap);
        }

        /**
         * 压缩图片
         * */
        private Bitmap getZoomBitmap(Response response,int mTargetWidth ,int mTargetHeight) {
            if (response.isSuccessful()) {
                byte[] data = null;
                try {
                    data = response.body().bytes();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(data,0,data.length,options);
                int picWidth = options.outWidth;
                int picHeight = options.outHeight;
                int sampleSize = 1;
                int heightRatio = (int) Math.floor((float) picWidth / (float) mTargetWidth);
                int widthRatio = (int) Math.floor((float) picHeight / (float) mTargetHeight);
                if (heightRatio > 1 || widthRatio > 1){
                    sampleSize = Math.max(heightRatio,widthRatio);
                }
                options.inSampleSize = sampleSize;
                options.inJustDecodeBounds = false;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length,options);
                return bitmap;
            }
            return null;
        }
    }


}













