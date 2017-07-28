# TouchGalleryView

TouchGalleryView是一个图集展示View。可加载网络图片也可以加载本地图片。支持左右滑动切换图片，也支持双手指摸动缩放图片。

## 使用方法

代码比较简单，只有四个Java文件，建议下载后将Java文件拷贝到工程中使用。

## 实现的功能有：

1，类似于ViewPager实现左右滑动切换图片。

2，单个图片可双手摸动缩小放大。

3，可加载本地图片。

4，可加载网络图片，加载网络图片使用了线程池，且实现了图片压缩，规避了内存溢出。

5，实现网络加载等待View，并可设置加载网络图片失败时的提示图片。

6，实现了双击缩放图片，且实现了单击监听器。

## 实现原理

TouchGalleryView是对ViewPager和ImageView的封装。重写ImageView类，添加摸动缩放的功能。重写ViewPager类 ，对touch事件进行分情况处理。

## 使用示例

TouchGalleryView使用非常简单，具体如下；

#### 1，布局文件中

```
    <com.fighting.touchgalleryview.view.GalleryViewPager
        android:id="@+id/gallery_view_pager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```

#### 2，在activity中找到GalleryViewPager对象

首先找到GalleryViewPager对象：

```
galleryViewPager = (GalleryViewPager)findViewById(R.id.gallery_view_pager);
```

最好将Activity设置为全屏：

```
ActionBar actionBar = getSupportActionBar();
if(actionBar != null){
    actionBar.hide();
}
this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN);
```

#### 3，设置URL

```
String[] urls = {
        "http://imgsrc.baidu.com/baike/pic/item/a6efce1b9d16fdfac666d143b08f8c5494ee7b10.jpg",
        "http://img.mp.sohu.com/upload/20170721/24645b7518034e5dbbd67842566c0dc2_th.png",
        "http://pic1.win4000.com/wallpaper/6/51491afe9fd66.jpg"
};
List<String> items = new ArrayList<>();
Collections.addAll(items, urls);
galleryViewPager.setUrlList(items);//设置URL
```

此时图片就显示出来了，并可以左右滑动，双手指摸动缩放。

#### 4，设置加载失败图片

```
galleryViewPager.setErrorImageResource(R.drawable.error_iamge);//设置加载失败的image
```

#### 5，设置条目点击监听器

```
galleryViewPager.setOnItemClickListener(new GalleryViewPager.OnItemClickListener() {
    @Override
    public void onItemClick(int position) {
        Toast.makeText(getApplicationContext(),"click："+position,Toast.LENGTH_SHORT).show();
    }
});
```

## 显示效果如下

![Aaron Swartz](https://github.com/guozhengXia/TouchGalleryView/blob/master/sample01.gif)







