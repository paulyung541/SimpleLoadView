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
    private Context mContext;

    //加载中，加载失败，重新加载公用View
    private ImageView centerImage;
    private TextView textView;

    private Button button;

    ////////
    private int errorDrawableRes;//加载错误显示的图片
    private String errorText;//加载错误显示的文字
    private boolean haveReloadButton;
    private String reloadButtonText;
    private int reloadButtonBackgroundRes;//重新加载按钮的背景
    private int loadingDrawableRes;//loading图片
    private int loadingAnimation;//loading动画，如果设置了图片，则动画无效
    private String loadingText;

    private boolean haveAnimation;
    private AnimationDrawable ad;
    private Animation mAnim;//Tween动画
    private OnReloadListener mListener;

    /**
     * 显示错误的状态 和 显示重新加载 二者用其一就行了
     */

    public SimpleLoadView(Context context) {
        this(context, null);
        mContext = context;
    }

    public SimpleLoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        if (attrs == null) {
            return;
        }

        TypedArray t = mContext.obtainStyledAttributes(attrs, R.styleable.SimpleLoadView);
        errorDrawableRes = t.getResourceId(R.styleable.SimpleLoadView_error_drawable, Integer.MIN_VALUE);
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
            reloadButtonBackgroundRes = t.getResourceId(R.styleable.SimpleLoadView_reload_button_background, Integer.MIN_VALUE);
        }
        loadingDrawableRes = t.getResourceId(R.styleable.SimpleLoadView_loading_drawable, Integer.MIN_VALUE);
        if (loadingDrawableRes == Integer.MIN_VALUE) {
            loadingAnimation = t.getResourceId(R.styleable.SimpleLoadView_loading_animation, Integer.MIN_VALUE);
            if (loadingAnimation != Integer.MIN_VALUE) {
                haveAnimation = true;
            }
        }
        loadingText = t.getString(R.styleable.SimpleLoadView_loading_text);
        if (loadingText == null) {
            loadingText = "加载中...";
        }
        t.recycle();
        initView();
    }

    private void initView() {
        centerImage = new ImageView(mContext);
        textView = new TextView(mContext);
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
            button = new Button(mContext);
            addView(button);
            params = (LayoutParams) button.getLayoutParams();
            params.addRule(BELOW, R.id.simple_loadview_tv);
            params.addRule(CENTER_HORIZONTAL);
            params.setMargins(0, dip2px(10), 0, 0);
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onReload();
                }
            });
        }

    }

    /**
     * 显示加载错误
     */
    public void showError() {
        if (haveReloadButton) {
            throw new IllegalArgumentException("you must call showReload() method");
        }

        setVisibility(VISIBLE);
        if (haveAnimation) {//停止 Frame 动画
            ad.stop();
        }
        if (mAnim != null) {//停止 Tween动画
            centerImage.clearAnimation();
        }

        centerImage.setBackgroundResource(0);
        centerImage.setImageResource(errorDrawableRes);
        textView.setText(errorText);

    }

    /**
     * 显示单张图片，或者Frame动画
     */
    public void showLoading() {
        setVisibility(VISIBLE);
        if (haveReloadButton) {
            button.setVisibility(GONE);
        }
        if (haveAnimation) {
            centerImage.setImageResource(0);
            centerImage.setBackgroundResource(loadingAnimation);
            ad = (AnimationDrawable) centerImage.getBackground();
            ad.start();
        } else {
            centerImage.setImageResource(loadingDrawableRes);
        }
        textView.setText(loadingText);
    }

    /**
     * 显示 Tween 的loading动画
     */
    public void showLoading(@AnimRes int animRes, @DrawableRes int loadImageRes) {
        setVisibility(VISIBLE);
        if (haveReloadButton) {
            button.setVisibility(GONE);
        }
        if (mAnim == null) {
            mAnim = AnimationUtils.loadAnimation(mContext, animRes);
        }
        centerImage.setImageResource(loadImageRes);
        centerImage.startAnimation(mAnim);
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
        button.setVisibility(VISIBLE);
        if (haveAnimation) {//停止 Frame 动画
            ad.stop();
        }
        if (mAnim != null) {//停止 Tween动画
            centerImage.clearAnimation();
        }

        centerImage.setBackgroundResource(0);
        centerImage.setImageResource(errorDrawableRes);
        textView.setText(errorText);
        button.setText(reloadButtonText);
        if (reloadButtonBackgroundRes != Integer.MIN_VALUE) {
            button.setBackgroundResource(reloadButtonBackgroundRes);
        }
    }

    public void setOnReloadListener(OnReloadListener l) {
        mListener = l;
    }

    //dp 转 px
    private int dip2px(float dipValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public interface OnReloadListener {
        void onReload();
    }
}
