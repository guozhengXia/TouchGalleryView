package com.fighting.touchgalleryview;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

import com.fighting.touchgalleryview.view.GalleryViewPager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GalleryViewPager galleryViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.hide();
        }
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        galleryViewPager = (GalleryViewPager)findViewById(R.id.gallery_view_pager);

        String[] urls = {
                "http://imgsrc.baidu.com/baike/pic/item/a6efce1b9d16fdfac666d143b08f8c5494ee7b10.jpg",
                "http://img.mp.sohu.com/upload/20170721/24645b7518034e5dbbd67842566c0dc2_th.png",
                "http://pic1.win4000.com/wallpaper/6/51491afe9fd66.jpg",
                "http://img02.tooopen.com/downs/images/2010/9/10/sy_20100910113804636024.jpg",
                "http://img4.duitang.com/uploads/item/201508/16/20150816012032_nFxw2.jpeg",
                "http://img.popo.cn/uploadfile/2016/0414/1460620338888463.png",
                "http://img.mp.itc.cn/upload/20161009/7b27b60a9858448b8421f6106c6b4378_th.jpg",
                "http://errorDemo.jpg"
        };
        List<String> items = new ArrayList<>();
        Collections.addAll(items, urls);
        galleryViewPager.setUrlList(items);//设置URL
        galleryViewPager.setErrorImageResource(R.drawable.error_iamge);//设置加载失败的image
        //设置条目点击事件
        galleryViewPager.setOnItemClickListener(new GalleryViewPager.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Toast.makeText(getApplicationContext(),"click："+position,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
