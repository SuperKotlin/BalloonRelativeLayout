package com.zhuyong.balloonrelativelayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Random;

/**
 * Created by zhuyong on 2017/7/19.
 */

public class BalloonRelativeLayout extends RelativeLayout {
    private Context mContext;
    private Interpolator[] interpolators;//插值器数组
    private Interpolator linearInterpolator = new LinearInterpolator();// 以常量速率改变
    private Interpolator accelerateInterpolator = new AccelerateInterpolator();//加速
    private Interpolator decelerateInterpolator = new DecelerateInterpolator();//减速
    private Interpolator accelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();//先加速后减速
    private LayoutParams layoutParams;
    private int mHeight;
    private int mWidth;
    private Random random = new Random();//初始化随机数类
    private int mViewHeight = dip2px(getContext(), 50);//默认50dp
    private Drawable[] drawables;

    public BalloonRelativeLayout(Context context) {
        this(context, null);
    }

    public BalloonRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BalloonRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        //初始化显示的图片
        drawables = new Drawable[3];
        Drawable mBalloon = ContextCompat.getDrawable(mContext, R.mipmap.balloon_pink);
        Drawable mBalloon2 = ContextCompat.getDrawable(mContext, R.mipmap.balloon_purple);
        Drawable mBalloon3 = ContextCompat.getDrawable(mContext, R.mipmap.balloon_blue);
        drawables[0] = mBalloon;
        drawables[1] = mBalloon2;
        drawables[2] = mBalloon3;

        //设置view宽高相等，默认都是50dp
        layoutParams = new LayoutParams(mViewHeight, mViewHeight);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM, TRUE);

        // 初始化插值器
        interpolators = new Interpolator[4];
        interpolators[0] = linearInterpolator;
        interpolators[1] = accelerateInterpolator;
        interpolators[2] = decelerateInterpolator;
        interpolators[3] = accelerateDecelerateInterpolator;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    public void addBalloon() {
        final ImageView imageView = new ImageView(getContext());
        //随机选一个
        imageView.setImageDrawable(drawables[random.nextInt(3)]);
        imageView.setLayoutParams(layoutParams);
        addView(imageView);

        Animator animator = getAnimator(imageView);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                //view动画结束后remove掉
                removeView(imageView);
            }
        });
        animator.start();
    }


    private Animator getAnimator(View target) {

        ValueAnimator bezierValueAnimator = getBezierValueAnimator(target);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(bezierValueAnimator);
        animatorSet.setInterpolator(interpolators[random.nextInt(4)]);
        animatorSet.setTarget(target);
        return animatorSet;
    }

    private ValueAnimator getBezierValueAnimator(final View target) {

        //初始化一个自定义的贝塞尔曲线插值器，并且传入控制点
        BezierEvaluator evaluator = new BezierEvaluator(getPointF(), getPointF());

        //传入了曲线起点（左下角）和终点（顶部随机）
        ValueAnimator animator = ValueAnimator.ofObject(evaluator, new PointF(0, getHeight())
                , new PointF(random.nextInt(getWidth()), -mViewHeight));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                //获取到贝塞尔曲线轨迹上的x和y值 赋值给view
                PointF pointF = (PointF) animation.getAnimatedValue();
                target.setX(pointF.x);
                target.setY(pointF.y);
            }
        });
        animator.setTarget(target);
        animator.setDuration(5000);
        return animator;
    }


    /**
     * 自定义曲线的两个控制点，随机在ViewGroup上的任何一个位置
     */
    private PointF getPointF() {
        PointF pointF = new PointF();
        pointF.x = random.nextInt(mWidth);
        pointF.y = random.nextInt(mHeight);
        return pointF;
    }


    /**
     * 自定义插值器
     */

    class BezierEvaluator implements TypeEvaluator<PointF> {

        //途径的两个点
        private PointF pointF1;
        private PointF pointF2;

        public BezierEvaluator(PointF pointF1, PointF pointF2) {
            this.pointF1 = pointF1;
            this.pointF2 = pointF2;
        }

        @Override
        public PointF evaluate(float time, PointF startValue,
                               PointF endValue) {

            float timeOn = 1.0f - time;
            PointF point = new PointF();
            //这么复杂的公式让我计算真心头疼，但是计算机很easy
            point.x = timeOn * timeOn * timeOn * (startValue.x)
                    + 3 * timeOn * timeOn * time * (pointF1.x)
                    + 3 * timeOn * time * time * (pointF2.x)
                    + time * time * time * (endValue.x);

            point.y = timeOn * timeOn * timeOn * (startValue.y)
                    + 3 * timeOn * timeOn * time * (pointF1.y)
                    + 3 * timeOn * time * time * (pointF2.y)
                    + time * time * time * (endValue.y);
            return point;
        }
    }

    /**
     * Dip into pixels
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
