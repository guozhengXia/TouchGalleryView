package com.fighting.touchgalleryview.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by fighting on 2017/7/28.
 */
public class TouchImageView extends android.support.v7.widget.AppCompatImageView {

    static final int TOUCH_MODE_NONE = 0;
    static final int TOUCH_MODE_DRAG = 1;//拖动
    static final int TOUCH_MODE_ZOOM = 2;//缩放
    static final int TOUCH_MODE_CLICK = 10;
    /**当前触摸的模式，是滑动，是缩放，还是什么都没有*/
    int touchMode = TOUCH_MODE_NONE;


    /**当前ImageView的bitmap，此imageView必须以setImageBitmap方法设置图片*/
    Bitmap mbitmap;
    /**当前bitmap的宽度，默认不能为0，默认为0时在图片加载之前不能滑动*/
    float bitmapWidth = 10;
    /**当前bitmap的高度*/
    float bitmapHeight = 10;
    /**图片显示的原始宽度，与bitmap的宽度不一定相等*/
    float origWidth;
    /**图片显示的原始高度*/
    float origHeight;
    /**在onmeasure方法中传递过来的测量规则中的宽度，一般都是充满全屏*/
    float measureSpecWidth;
    /**在onmeasure方法中传递过来的测量规则中的高度，一般都是充满全屏*/
    float measureSpecHeight;



    /**该ImageView的矩阵，操作该ImageView的平移，缩放.此View主要根据矩阵来操作缩放*/
    Matrix mMatrix = new Matrix();
    Matrix savedMatrix = new Matrix();
    /**保存矩阵的9个值*/
    float[] matrixArray;
    /**该ImageView对应的矩阵中平移的x坐标和y坐标*/
    float matrixTranslationX, matrixTranslationY;


    /**手指按下时记录的点，在down的时候第一次赋值，在move时改变值*/
    PointF lastPoint = new PointF();
    /**当两个手指触摸时，两个手指触摸点的中点*/
    PointF middlePoint = new PointF();
    /**手指按下时记录的点,在down的时候一次赋值*/
    PointF startPoint = new PointF();
    /**在move时，记录distanceX和distanceY两个值*/
    PointF lastDelta = new PointF(0, 0);


    /**没有缩放*/
    final float NO_SCALE = 1f;
    /**最小的缩小比例，是一个常量*/
    final float MIN_SCALE = 0.5f;
    /**最大的放大比例，是一个常量*/
    final float MAX_SCALE = 3f;
    /**当前图片大小相对于origin时图片大小当缩放比例*/
    float saveScale = 1f;


    /**双击的可利用时间。当两次当点击事件小于300则认为是双击。*/
    static final long DOUBLE_PRESS_INTERVAL = 300;
    /**速率当衰减率，当drag时，有一个惯性，惯性速率逐渐降低*/
    static final float FRICTION = 0.9f;
    /**移动速率，当摸动时，有一个惯性。*/
    float velocity = 0;


    /**X方向上，空白的距离。假设图片宽度是100，屏幕宽度是320，那么这个宽度就是220，也可能是110。*/
    float redundantXSpace;
    /**Y方向上，空白的距离。*/
    float redundantYSpace;
    /**记录图片的right和bottom的位置，用来判断图片是否充满View*/
    float right, bottom;
    /**两个手指之间的距离*/
    float oldDist = 1f;


    /**上次按下的时间，判断双击时使用*/
    long lastPressTime = 0;
    long lastDragTime = 0;
    /**这个值在down和move时为false，up时为true*/
    boolean allowInert = true;


    private Context mContext;
    /**缩放手势监听器*/
    private Object mScaleDetector;
    /**标记ImageView上下左右是否靠边，靠边时才触发ViewPaer的滑动事件*/
    public boolean onLeftSide = false, onTopSide = false, onRightSide = false, onBottomSide = false;

    public TouchImageView(Context context) {
        super(context);
        super.setClickable(true);
        this.mContext = context;
        init();

    }
    public TouchImageView(Context context, AttributeSet attrs){
        super(context, attrs);
        super.setClickable(true);
        this.mContext = context;
        init();
    }
    
	protected void init(){
        mMatrix.setTranslate(1f, 1f);
        matrixArray = new float[9];
        setImageMatrix(mMatrix);
        setScaleType(ScaleType.MATRIX);
        if (Build.VERSION.SDK_INT >= 8){
            mScaleDetector = new ScaleGestureDetector(mContext, new ScaleListener());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!allowInert) return;
        final float deltaX = lastDelta.x * velocity, deltaY = lastDelta.y * velocity;
        if (deltaX > measureSpecWidth || deltaY > measureSpecHeight) {
            return;
        }
        velocity *= FRICTION;//速率逐渐降低
        if (Math.abs(deltaX) < 0.1 && Math.abs(deltaY) < 0.1) return;
        checkAndSetTranslate(deltaX, deltaY);
        setImageMatrix(mMatrix);
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureSpecWidth = MeasureSpec.getSize(widthMeasureSpec);
        measureSpecHeight = MeasureSpec.getSize(heightMeasureSpec);
        float scaleX =  measureSpecWidth / bitmapWidth;
        float scaleY = measureSpecHeight / bitmapHeight;
        float scale = Math.min(scaleX, scaleY);
        mMatrix.setScale(scale, scale);
        setImageMatrix(mMatrix);
        saveScale = 1f;
        // Center the image
        redundantYSpace = measureSpecHeight - (scale * bitmapHeight) ;
        redundantXSpace = measureSpecWidth - (scale * bitmapWidth);
        redundantYSpace /= (float)2;
        redundantXSpace /= (float)2;
        mMatrix.postTranslate(redundantXSpace, redundantYSpace);//设置平移的尺寸
        origWidth = measureSpecWidth - 2 * redundantXSpace;
        origHeight = measureSpecHeight - 2 * redundantYSpace;
        calcPadding();
        setImageMatrix(mMatrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mScaleDetector != null){
            ((ScaleGestureDetector)mScaleDetector).onTouchEvent(event);
        }else {
            throw new RuntimeException("SDK shoud larger 8");
        }
        fillMatrixXY();
        PointF currentPoint = new PointF(event.getX(), event.getY());//当前的点坐标
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                allowInert = false;
                savedMatrix.set(mMatrix);//拷贝矩阵信息
                lastPoint.set(event.getX(), event.getY());
                startPoint.set(lastPoint);
                touchMode = TOUCH_MODE_DRAG;//单手按下是drag
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);//测量两个手指间的距离
                //Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f) {
                    savedMatrix.set(mMatrix);
                    midPoint(middlePoint, event);//设置两个手指触摸点的中点
                    touchMode = TOUCH_MODE_ZOOM;//双手按下是zoom
                }
                break;
            case MotionEvent.ACTION_UP:
                allowInert = true;
                touchMode = TOUCH_MODE_NONE;
                int xDiff = (int) Math.abs(event.getX() - startPoint.x);
                int yDiff = (int) Math.abs(event.getY() - startPoint.y);

                if (xDiff < TOUCH_MODE_CLICK && yDiff < TOUCH_MODE_CLICK) {//按下与抬起的距离小于10是点击
                    handleClickEvent();//处理单击事件和双击事件
                    if (saveScale == NO_SCALE) {
                        scaleMatrixToBounds();
                    }
                }
                if(saveScale < NO_SCALE){//如果在抬起时图片被缩小了，则恢复到未缩放状态。
                    resetScale();
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                touchMode = TOUCH_MODE_NONE;
                velocity = 0;
                savedMatrix.set(mMatrix);
                oldDist = spacing(event);
                break;

            case MotionEvent.ACTION_MOVE:
                allowInert = false;
                if (touchMode == TOUCH_MODE_DRAG) {
                    handlerDragEvent(currentPoint);
                }
                break;
        }
        setImageMatrix(mMatrix);
        invalidate();
        return true;
    }

    /**
     * 处理拖动事件
     * */
    private void handlerDragEvent(PointF currentPoint) {
        float distanceX = currentPoint.x - lastPoint.x;
        float distanceY = currentPoint.y - lastPoint.y;
        long dragTime = System.currentTimeMillis();
        velocity = (float)distanceBetween(currentPoint, lastPoint) / (dragTime - lastDragTime) * FRICTION;
        lastDragTime = dragTime;
        checkAndSetTranslate(distanceX, distanceY);
        lastDelta.set(distanceX, distanceY);
        lastPoint.set(currentPoint.x, currentPoint.y);
    }

    /**
     * 实现单击事件和双击事件
     * */
    private void handleClickEvent() {
        long pressTime = System.currentTimeMillis();
        if (pressTime - lastPressTime < DOUBLE_PRESS_INTERVAL) {//两次抬起的时间差小于600毫秒才是双击
            if (saveScale == NO_SCALE){//如果没有缩放，则放为最大
                final float targetScale = MAX_SCALE / saveScale;
                mMatrix.postScale(targetScale, targetScale, startPoint.x, startPoint.y);//设置缩放比例，并指定缩放中心
                saveScale = MAX_SCALE;
            }else {//如果有缩放，则回复原状
                mMatrix.postScale(NO_SCALE / saveScale, NO_SCALE / saveScale, measureSpecWidth / 2, measureSpecHeight / 2);
                saveScale = NO_SCALE;
            }
            calcPadding();
            checkAndSetTranslate(0, 0);
            lastPressTime = 0;
        }else {
            lastPressTime = pressTime;
            postDelayed(new Runnable() {//300毫秒后执行点击事件。若300秒内又up一次则是双击，不执行点击事件。
                @Override
                public void run() {
                    if(lastPressTime > 0 && mOnClickListener != null){//大于0说明300毫秒内没有双击事件
                        mOnClickListener.onClick(TouchImageView.this);
                    }
                }
            },DOUBLE_PRESS_INTERVAL);
        }
    }

    /**重置缩放，即恢复到缩放前的原始状态*/
    public void resetScale(){
        fillMatrixXY();
        mMatrix.postScale(NO_SCALE / saveScale, NO_SCALE / saveScale, measureSpecWidth / 2, measureSpecHeight / 2);
        saveScale = NO_SCALE;
        calcPadding();
        checkAndSetTranslate(0, 0);
        scaleMatrixToBounds();
        setImageMatrix(mMatrix);
        invalidate();
    }
    /**
     * 当没有缩放时表示可以滚动，即此时的缩放比例为1。
     * */
    public boolean pagerCanScroll(){
        /*
        * 当一个手指down时为drag，当两个手指down时为scale,当手指抬起时为none.
        * */
        if (touchMode != TOUCH_MODE_NONE) return false;
        return saveScale == NO_SCALE;
    }


    /**平移view*/
    private void checkAndSetTranslate(float deltaX, float deltaY){
        float scaleWidth = Math.round(origWidth * saveScale);
        float scaleHeight = Math.round(origHeight * saveScale);
        fillMatrixXY();
        if (scaleWidth < measureSpecWidth) {
            deltaX = 0;
            if (matrixTranslationY + deltaY > 0)
                deltaY = -matrixTranslationY;
            else if (matrixTranslationY + deltaY < -bottom)
                deltaY = -(matrixTranslationY + bottom);
        } else if (scaleHeight < measureSpecHeight) {
            deltaY = 0;
            if (matrixTranslationX + deltaX > 0)
                deltaX = -matrixTranslationX;
            else if (matrixTranslationX + deltaX < -right)
                deltaX = -(matrixTranslationX + right);
        }
        else {
            if (matrixTranslationX + deltaX > 0)
                deltaX = -matrixTranslationX;
            else if (matrixTranslationX + deltaX < -right)
                deltaX = -(matrixTranslationX + right);

            if (matrixTranslationY + deltaY > 0)
                deltaY = -matrixTranslationY;
            else if (matrixTranslationY + deltaY < -bottom)
                deltaY = -(matrixTranslationY + bottom);
        }
        mMatrix.postTranslate(deltaX, deltaY);
        checkSiding();
    }

    /**
     * 检查图片的边界是否靠ImangeView的边界。
     * */
    private void checkSiding(){
        fillMatrixXY();
        //Log.d(TAG, "x: " + matrixTranslationX + " y: " + matrixTranslationY + " left: " + right / 2 + " top:" + bottom / 2);
        float scaleWidth = Math.round(origWidth * saveScale);
        float scaleHeight = Math.round(origHeight * saveScale);
        onLeftSide = onRightSide = onTopSide = onBottomSide = false;
        if (-matrixTranslationX < 10.0f ) onLeftSide = true;
        //Log.d("GalleryViewPager", String.format("ScaleW: %f; W: %f, MatrixX: %f", scaleWidth, measureSpecWidth, matrixTranslationX));
        if ((scaleWidth >= measureSpecWidth && (matrixTranslationX + scaleWidth - measureSpecWidth) < 10) ||
            (scaleWidth <= measureSpecWidth && -matrixTranslationX + scaleWidth <= measureSpecWidth)) onRightSide = true;
        if (-matrixTranslationY < 10.0f) onTopSide = true;
        if (Math.abs(-matrixTranslationY + measureSpecHeight - scaleHeight) < 10.0f) onBottomSide = true;
    }
    private void calcPadding(){
        right = measureSpecWidth * saveScale - measureSpecWidth - (2 * redundantXSpace * saveScale);
        bottom = measureSpecHeight * saveScale - measureSpecHeight - (2 * redundantYSpace * saveScale);
    }
    private void fillMatrixXY(){
        mMatrix.getValues(matrixArray);//复制9个值到数组中
        matrixTranslationX = matrixArray[Matrix.MTRANS_X];
        matrixTranslationY = matrixArray[Matrix.MTRANS_Y];
    }
    private void scaleMatrixToBounds(){
        if (Math.abs(matrixTranslationX + right / 2) > 0.5f)
            mMatrix.postTranslate(-(matrixTranslationX + right / 2), 0);
        if (Math.abs(matrixTranslationY + bottom / 2) > 0.5f)
            mMatrix.postTranslate(0, -(matrixTranslationY + bottom / 2));
    }

    /**得到两点的距离*/
    private double distanceBetween(PointF left, PointF right){
        return Math.sqrt(Math.pow(left.x - right.x, 2) + Math.pow(left.y - right.y, 2));
    }
    /** 计算两个手指之间的距离 */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /** 计算两个手指的中心点坐标 */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        bitmapWidth = bm.getWidth();
        bitmapHeight = bm.getHeight();
    }


    /**点击监听器*/
    private OnClickListener mOnClickListener;
    @Override
    public void setOnClickListener(OnClickListener l) {
        mOnClickListener = l;
    }
    /**
     * 缩放监听器，当手指缩放时调用。缩放的核心代码
     * */
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            touchMode = TOUCH_MODE_ZOOM;
            return true;
        }

        /**当双手指有缩放的动作时被调用，多次被调用，与事件中的move类似。在这个方法中处理缩放事件*/
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = (float)Math.min(Math.max(.95f, detector.getScaleFactor()), 1.05);//此时相对于上一次的比例。
            float origScale = saveScale;
            saveScale *= mScaleFactor;
            if (saveScale > MAX_SCALE) {
                saveScale = MAX_SCALE;
                mScaleFactor = MAX_SCALE / origScale;
            } else if (saveScale < MIN_SCALE) {
                saveScale = MIN_SCALE;
                mScaleFactor = MIN_SCALE / origScale;
            }
            calcPadding();

            if(origWidth * saveScale <= measureSpecWidth && origHeight * saveScale <= measureSpecHeight){//如果高宽都小于屏幕
                mMatrix.postScale(mScaleFactor, mScaleFactor, measureSpecWidth / 2, measureSpecHeight / 2);//缩放操作
            }else if (origWidth * saveScale > measureSpecWidth && origHeight * saveScale > measureSpecHeight){//如果宽高都充满来屏幕
                mMatrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());//缩放操作
                //下面是平移操作
                mMatrix.getValues(matrixArray);
                float x = matrixArray[Matrix.MTRANS_X];
                float y = matrixArray[Matrix.MTRANS_Y];
                   /*
                    * 为什么在缩小时需要平移，而放大时不需要？
                    * 因为当图片放大后，某一边显示在屏幕上，此时缩小，边上就会漏出来，为了防止边上漏出来所以要向边上移动。
                    * */
                if (mScaleFactor < 1) {//当缩小时
                    if (x < -right) mMatrix.postTranslate(-(x + right), 0);
                    else if (x > 0) mMatrix.postTranslate(-x, 0);

                    if (y < -bottom)  mMatrix.postTranslate(0, -(y + bottom));
                    else if (y > 0) mMatrix.postTranslate(0, -y);
                }
            }else if (origWidth * saveScale <= measureSpecWidth || origHeight * saveScale <= measureSpecHeight) {//当宽度和高度有一个没充满全屏时
                mMatrix.postScale(mScaleFactor, mScaleFactor, measureSpecWidth / 2, measureSpecHeight / 2);//缩放操作
                //下面是平移操作，
                if (mScaleFactor < 1) {//当缩小时
                    mMatrix.getValues(matrixArray);
                    float x = matrixArray[Matrix.MTRANS_X];
                    float y = matrixArray[Matrix.MTRANS_Y];
                    if (Math.round(origWidth * saveScale) < measureSpecWidth) {//当宽度没有充满全屏时
                        if (y < -bottom)
                            mMatrix.postTranslate(0, -(y + bottom));
                        else if (y > 0)
                            mMatrix.postTranslate(0, -y);
                    } else {//当高度没有充满全屏时
                        if (x < -right)
                            mMatrix.postTranslate(-(x + right), 0);
                        else if (x > 0)
                            mMatrix.postTranslate(-x, 0);
                    }
                }
            }
            return true;
        }
    }
}