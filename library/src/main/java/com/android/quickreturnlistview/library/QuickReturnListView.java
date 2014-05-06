package com.android.quickreturnlistview.library;

/*
 * Copyright 2013 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

public class QuickReturnListView extends ListView implements
        AbsListView.OnScrollListener, ViewTreeObserver.OnGlobalLayoutListener {
    private int mItemOffsetY[];
    private boolean scrollIsComputed = false;
    private int mHeight;

    private View mPlaceHolder;
    private View mQuickReturnView;

    private int mCachedVerticalScrollRange;
    private int mQuickReturnHeight;

    private static final int STATE_ONSCREEN = 0;
    private static final int STATE_OFFSCREEN = 1;
    private static final int STATE_RETURNING = 2;
    private static final int STATE_EXPANDED = 3;
    private int mState = STATE_ONSCREEN;
    private int mMinRawY = 0;
    private int rawY = 0;
    private int translationY = 0;
    private boolean noAnimation = false;
    private boolean mWithAnimation = false;
    private TranslateAnimation anim;

    private OnScrollListener mDelegateScrollListener;

    public QuickReturnListView(Context context) {
        super(context);

        init();
    }

    public QuickReturnListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        super.setOnScrollListener(this);
        getViewTreeObserver().addOnGlobalLayoutListener(this);

        final LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View mHeader = inflater.inflate(R.layout.header, null);
        mPlaceHolder = mHeader.findViewById(R.id.placeholder);

        addHeaderView(mHeader);
    }

    public void setAnimatedReturn(final boolean withAnimation) {
        mWithAnimation = withAnimation;
    }

    public void setQuickReturnView(final View quickReturnView) {
        mQuickReturnView = quickReturnView;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mQuickReturnView.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        params.height = mQuickReturnView.getMeasuredHeight();
        mPlaceHolder.setLayoutParams(params);
    }

    private void computeScrollY() {
        mHeight = 0;
        int mItemCount = getAdapter().getCount();
        mItemOffsetY = new int[mItemCount];

        for (int i = 0; i < mItemCount; ++i) {
            View view = getAdapter().getView(i, null, this);
            view.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            mItemOffsetY[i] = mHeight;
            mHeight += view.getMeasuredHeight();
        }

        scrollIsComputed = true;
    }

    private int getComputedScrollY() {
        int pos, nScrollY, nItemY;
        View view;
        pos = getFirstVisiblePosition();
        view = getChildAt(0);
        nItemY = view.getTop();
        nScrollY = mItemOffsetY[pos] - nItemY;
        return nScrollY;
    }

    @Override
    public void addFooterView(View footerView) {
        // after adding a header/footer the list has to be measured again
        scrollIsComputed = false;

        super.addFooterView(footerView);
    }

    @Override
    public void addHeaderView(View headerView) {
        // after adding a header/footer the list has to be measured again
        scrollIsComputed = false;

        super.addHeaderView(headerView);
    }

    @Override
    public void setOnScrollListener(final OnScrollListener listener) {
        mDelegateScrollListener = listener;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (mQuickReturnView == null)
            return;

        if (mDelegateScrollListener != null) {
            mDelegateScrollListener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
        }

        int mScrollY = 0;
        translationY = 0;

        if (scrollIsComputed) {
            mScrollY = getComputedScrollY();
        }

        rawY = mPlaceHolder.getTop() -
                Math.min(mCachedVerticalScrollRange - getHeight(), mScrollY);

        switch (mState) {
            case STATE_OFFSCREEN:
                if (rawY <= mMinRawY) {
                    mMinRawY = rawY;
                } else {
                    mState = STATE_RETURNING;
                }
                translationY = rawY;
                break;

            case STATE_ONSCREEN:
                if (rawY < -mQuickReturnHeight) {
                    mState = STATE_OFFSCREEN;
                    mMinRawY = rawY;
                }
                translationY = rawY;
                break;

            case STATE_RETURNING:
                if (mWithAnimation) {
                    showWithAnimation();
                } else {
                    showWithoutAnimation();
                }

                break;

            case STATE_EXPANDED:
                if (mWithAnimation) {
                    hideWithAnimation();
                }
                break;
        }

        /** this can be used if the build is below honeycomb **/
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB) {
            anim = new TranslateAnimation(0, 0,
                    translationY, translationY);
            anim.setFillAfter(true);
            anim.setDuration(0);
            mQuickReturnView.startAnimation(anim);
        } else {
            mQuickReturnView.setTranslationY(translationY);
        }
    }

    @Override
    public void onGlobalLayout() {
        if (mQuickReturnView == null)
            return;

        mQuickReturnHeight = mQuickReturnView.getHeight();
        if (!scrollIsComputed)
            computeScrollY();
        mCachedVerticalScrollRange = mHeight;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mDelegateScrollListener != null) {
            mDelegateScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    private void showWithoutAnimation() {
        translationY = (rawY - mMinRawY) - mQuickReturnHeight;
        if (translationY > 0) {
            translationY = 0;
            mMinRawY = rawY - mQuickReturnHeight;
        }

        if (rawY > 0) {
            mState = STATE_ONSCREEN;
            translationY = rawY;
        }

        if (translationY < -mQuickReturnHeight) {
            mState = STATE_OFFSCREEN;
            mMinRawY = rawY;
        }
    }

    private void showWithAnimation() {
        if (translationY > 0) {
            translationY = 0;
            mMinRawY = rawY - mQuickReturnHeight;
        } else if (rawY > 0) {
            mState = STATE_ONSCREEN;
            translationY = rawY;
        } else if (translationY < -mQuickReturnHeight) {
            mState = STATE_OFFSCREEN;
            mMinRawY = rawY;
        } else if (mQuickReturnView.getTranslationY() != 0
                && !noAnimation) {
            noAnimation = true;
            anim = new TranslateAnimation(0, 0, -mQuickReturnHeight, 0);
            anim.setFillAfter(true);
            anim.setDuration(250);
            mQuickReturnView.startAnimation(anim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    noAnimation = false;
                    mMinRawY = rawY;
                    mState = STATE_EXPANDED;
                }
            });
        }
    }

    private void hideWithAnimation() {
        if (rawY < mMinRawY - 2 && !noAnimation) {
            noAnimation = true;
            anim = new TranslateAnimation(0, 0, 0, -mQuickReturnHeight);
            anim.setFillAfter(true);
            anim.setDuration(250);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    noAnimation = false;
                    mState = STATE_OFFSCREEN;
                }
            });
            mQuickReturnView.startAnimation(anim);
        } else if (translationY > 0) {
            translationY = 0;
            mMinRawY = rawY - mQuickReturnHeight;
        } else if (rawY > 0) {
            mState = STATE_ONSCREEN;
            translationY = rawY;
        } else if (translationY < -mQuickReturnHeight) {
            mState = STATE_OFFSCREEN;
            mMinRawY = rawY;
        } else {
            mMinRawY = rawY;
        }
    }
}