package com.ysy.simpleloadview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.AnimRes;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by yang on 2016/8/9.
 * loading提供了 Tween动画 和 Frame动画两种模式
 * 其中Tween动画只支持代码添加
 */
public class SimpleLoadView extends RelativeLayout {
    private static final int PIC = 0;//a simple picture as loading
    private static final int TWEEN = 1;//tween animation
    private static final int FRAME = 2;//frame animation
    private static int LOADING_MODE = PIC;

    //加载中，加载失败，重新加载公用View
    private ImageView centerImage;
    private TextView textView;
    private Button button;

    ////////
    private int errorDrawableRes;//加载错误显示的图片
    private String errorText;//加载错误显示的文字
    private boolean haveReloadButton;//是否有重新加载按钮
    private String reloadButtonText;//重新加载按钮的文字
    private int reloadButtonBackgroundRes;//重新加载按钮的背景
    private int loadingDrawableRes;//loading图片
    private int loadingAnimation;//loading动画，如果设置了图片，则动画无效
    private String loadingText;//加载时候的文字

    private AnimationDrawable ad;//帧动画
    private Animation mAnim;//Tween动画
    @AnimRes
    private int tweenRes;//tween动画资源
    private int tweenPic;//tween动画需要的图片
    private OnReloadListener mListener;//重新加载监听

    /**
     * 显示错误的状态 和 显示重新加载 二者用其一就行了
     */

    public SimpleLoadView(Context context) {
        this(context, null);
    }

    public SimpleLoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs == null) {
            return;
        }

        TypedArray t = getContext().obtainStyledAttributes(attrs, R.styleable.SimpleLoadView);
        errorDrawableRes = t.getResourceId(R.styleable.SimpleLoadView_error_drawable, 0);
        errorText = t.getString(R.styleable.SimpleLoadView_error_text);
        if (errorText == null) {
            errorText = "加载失败";
        }
        haveReloadButton = t.getBoolean(R.styleable.SimpleLoadView_reload_button, false);
        if (haveReloadButton) {
            reloadButtonText = t.getString(R.styleable.SimpleLoadView_reload_button_text);
            if (reloadButtonText == null) {
                reloadButtonText = "重新加载";
            }
            reloadButtonBackgroundRes = t.getResourceId(R.styleable.SimpleLoadView_reload_button_background, 0);
        }
        loadingDrawableRes = t.getResourceId(R.styleable.SimpleLoadView_loading_drawable, 0);
        if (loadingDrawableRes != 0)
            LOADING_MODE = PIC;

        loadingAnimation = t.getResourceId(R.styleable.SimpleLoadView_loading_animation, 0);
        if (loadingAnimation != 0) {
            LOADING_MODE = FRAME;
        }

        tweenRes = t.getResourceId(R.styleable.SimpleLoadView_tween_anim, 0);
        tweenPic = t.getResourceId(R.styleable.SimpleLoadView_tween_pic, 0);
        if (tweenRes != 0 && tweenPic != 0) {
            LOADING_MODE = TWEEN;
        }

        loadingText = t.getString(R.styleable.SimpleLoadView_loading_text);
        if (loadingText == null) {
            loadingText = "加载中...";
        }
        t.recycle();
        initView();
    }

    private void initView() {
        centerImage = new ImageView(getContext());
        textView = new TextView(getContext());
        //+id
        centerImage.setId(R.id.simple_loadview_img);
        textView.setId(R.id.simple_loadview_tv);

        addView(centerImage);
        addView(textView);
        LayoutParams params = (LayoutParams) textView.getLayoutParams();
        params.addRule(CENTER_IN_PARENT);
        params = (LayoutParams) centerImage.getLayoutParams();
        params.addRule(ABOVE, R.id.simple_loadview_tv);
        params.addRule(CENTER_HORIZONTAL);
        params.setMargins(0, 0, 0, dip2px(10));

        //赋值

        if (haveReloadButton) {
            button = new Button(getContext());
            addView(button);
            params = (LayoutParams) button.getLayoutParams();
            params.addRule(BELOW, R.id.simple_loadview_tv);
            params.addRule(CENTER_HORIZONTAL);
            params.setMargins(0, dip2px(10), 0, 0);
            if (reloadButtonBackgroundRes != 0)
                button.setBackgroundDrawable(getResources().getDrawable(reloadButtonBackgroundRes));
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onReload();
                }
            });
        }

        //init loading animation
        switch (LOADING_MODE) {
            case PIC:
                centerImage.setImageResource(loadingDrawableRes);
                break;
            case TWEEN:
                mAnim = AnimationUtils.loadAnimation(getContext(), tweenRes);
                centerImage.setImageResource(tweenPic);
                mAnim.setInterpolator(new LinearInterpolator());
                break;
            case FRAME:
                centerImage.setImageResource(0);
                centerImage.setBackgroundResource(loadingAnimation);
                ad = (AnimationDrawable) centerImage.getBackground();
        }
    }

    /**
     * 显示加载错误
     */
    public void showError() {
        setVisibility(VISIBLE);
        if (haveReloadButton) {
            throw new IllegalArgumentException("you must call showReload() method");
        }

        setVisibility(VISIBLE);
        if (LOADING_MODE == TWEEN)
            centerImage.clearAnimation();
        if (LOADING_MODE == FRAME)
            ad.stop();

        centerImage.setBackgroundResource(0);
        centerImage.setImageResource(errorDrawableRes);
        textView.setText(errorText);
    }

    public void showLoading() {
        setVisibility(VISIBLE);
        if (haveReloadButton) {
            button.setVisibility(GONE);
        }

        switch (LOADING_MODE) {
            case PIC:
                centerImage.setImageResource(loadingDrawableRes);
                break;
            case TWEEN:
                centerImage.setImageResource(tweenPic);
                centerImage.startAnimation(mAnim);
                break;
            case FRAME:
                centerImage.setImageResource(0);
                centerImage.setBackgroundResource(loadingAnimation);
                ad.start();
        }
        textView.setText(loadingText);
    }


    /**
     * 重新加载
     */
    public void showReload() {
        if (!haveReloadButton) {
            throw new IllegalArgumentException("you must call showError() method");
        }
        setVisibility(VISIBLE);
        if (LOADING_MODE == TWEEN)
            centerImage.clearAnimation();
        if (LOADING_MODE == FRAME)
            ad.stop();

        centerImage.setBackgroundResource(0);
        centerImage.setImageResource(errorDrawableRes);
        button.setVisibility(VISIBLE);
        button.setText(reloadButtonText);
        textView.setText(errorText);
    }

    public void stopAll() {
        if (LOADING_MODE == TWEEN)
            centerImage.clearAnimation();
        if (LOADING_MODE == FRAME)
            ad.stop();
        setVisibility(GONE);
    }

    /**
     * 设置 Tween 动画
     *
     * @param animRes      动画资源
     * @param loadImageRes 动画需要的图片
     */
    private void setTweenAnim(@AnimRes int animRes, @DrawableRes int loadImageRes) {
        LOADING_MODE = TWEEN;
        mAnim = AnimationUtils.loadAnimation(getContext(), animRes);
        centerImage.setImageResource(loadImageRes);
    }

    public void setOnReloadListener(OnReloadListener l) {
        mListener = l;
    }

    //dp 转 px
    private int dip2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public interface OnReloadListener {
        void onReload();
    }
}
