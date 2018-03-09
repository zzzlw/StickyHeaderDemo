package com.test.stickyheader.pull;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.test.stickyheader.R;


/**
 * Created by Oleksii Shliama on 22/12/2014.
 * https://dribbble.com/shots/1650317-Pull-to-Refresh-Rentals
 */
public class EarthRefreshView extends Drawable implements Animatable, Drawable.Callback {

    private static final int ANIMATION_DURATION = 1000;

    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator ACC_DEC_INTERPOLATOR = new AccelerateDecelerateInterpolator();

    private PullToRefreshView mParent;
    private Context mContext;

    private Matrix mMatrix;
    private Animation mEarthAnimation;
    private Animation mAnimation;

    private int mTop;
    private int mScreenWidth;
    private float mRotate = 0.0f;
    private float mPercent = 0.0f;
    private float mJetOffset = 0.0f;

    private Bitmap mJet;
    private Bitmap mEarth;

    private int mJetWidthCenter;
    private int mEarthWidthCenter;

    private boolean isRefreshing = false;
    private boolean isRefreshingEnd = false;

    public EarthRefreshView(Context context, final PullToRefreshView parent) {
        mContext = context;
        mParent = parent;
        mMatrix = new Matrix();

        setupAnimations();

        ViewTreeObserver vto2 = parent.getViewTreeObserver();
        vto2.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                parent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                initiateDimens(parent.getWidth());
            }
        });

    }

    public void initiateDimens(int viewWidth) {
        if (viewWidth <= 0 || viewWidth == mScreenWidth) return;

        mScreenWidth = viewWidth;
        mTop = -mParent.getTotalDragDistance();
        createBitmaps();
    }

    private void createBitmaps() {
        mJetWidthCenter = mJet.getWidth() / 2;
        mEarthWidthCenter = mEarth.getWidth() / 2;
        setDarkTheme(false);
    }

    public void setDarkTheme(boolean dark) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
//        mEarth = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_pull_refresh_earth, options);
//        mJet = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_pull_refresh_airplane, options);
    }

    public void setPercent(float percent, boolean invalidate) {
        setPercent(percent);
        if (invalidate) setRotate(percent);
    }

    public void offsetTopAndBottom(int offset) {
        mTop += offset;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (mScreenWidth <= 0) return;

        final int saveCount = canvas.save();

        canvas.translate(0, mTop);
        canvas.clipRect(0, -mTop, mScreenWidth, mParent.getTotalDragDistance());

        drawJet(canvas);
        drawEarth(canvas);

        canvas.restoreToCount(saveCount);
    }

    private void drawEarth(Canvas canvas) {
        Matrix matrix = mMatrix;
        matrix.reset();

        float offsetX = mScreenWidth / 2.0f;
        float offsetY = mParent.getTotalDragDistance() / 2;

        matrix.setTranslate(offsetX - mEarthWidthCenter, offsetY);
        matrix.postRotate(360 * mRotate, offsetX, offsetY + mEarthWidthCenter);

        canvas.drawBitmap(mEarth, matrix, null);
    }

    private void drawJet(Canvas canvas) {
        Matrix matrix = mMatrix;
        matrix.reset();

        if (isRefreshingEnd) {
            mPercent = Math.max(2f - mPercent, 1.0f);
        } else {
            mPercent = Math.min(mPercent, 1.0f);
        }
        if (isRefreshing) {
            matrix.setTranslate(mScreenWidth / 2 - mJetWidthCenter, mParent.getTotalDragDistance() / 2 - mJetWidthCenter * 2 - mJetOffset);
        } else {
            matrix.setTranslate(mScreenWidth / 2 - mJetWidthCenter, mParent.getTotalDragDistance() / 2 - mJetWidthCenter * 2);
        }
        matrix.postRotate(90 - 90 * mPercent, mScreenWidth / 2, mParent.getTotalDragDistance() / 2 + mEarthWidthCenter);

        canvas.drawBitmap(mJet, matrix, null);
    }

    public float getPercent() {
        return mPercent;
    }

    public void setPercent(float percent) {
        mPercent = percent;
    }

    public void setRotate(float rotate) {
        mRotate = rotate;
        invalidateSelf();
    }

    public void resetOriginals() {
        setPercent(0);
        setRotate(0);
        mJetOffset = 0.0f;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void start() {
        mEarthAnimation.reset();
        mAnimation.reset();
        isRefreshing = true;
        mParent.startAnimation(mEarthAnimation);
        mParent.getChildAt(0).startAnimation(mAnimation);
    }

    @Override
    public void stop() {
        mParent.clearAnimation();
        mParent.getChildAt(0).clearAnimation();
        isRefreshing = false;
        resetOriginals();
    }

    private void setupAnimations() {
        mEarthAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setRotate(interpolatedTime);
            }
        };

        mEarthAnimation.setRepeatCount(Animation.INFINITE);
        mEarthAnimation.setRepeatMode(Animation.RESTART);
        mEarthAnimation.setInterpolator(LINEAR_INTERPOLATOR);
        mEarthAnimation.setDuration(1500);

        mAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, @NonNull Transformation t) {
                mJetOffset = 15 * interpolatedTime;
            }
        };
        mAnimation.setRepeatCount(Animation.INFINITE);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(ACC_DEC_INTERPOLATOR);
        mAnimation.setDuration(ANIMATION_DURATION);
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        final Callback callback = getCallback();
        if (callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    public void setRefreshingEnd(boolean refreshingEnd) {
        isRefreshingEnd = refreshingEnd;
    }
}
