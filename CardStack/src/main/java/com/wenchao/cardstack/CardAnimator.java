package com.wenchao.cardstack;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.wenchao.animation.RelativeLayoutParamsEvaluator;

import java.util.ArrayList;
import java.util.HashMap;

import static com.wenchao.cardstack.CardUtils.cloneParams;
import static com.wenchao.cardstack.CardUtils.getMoveParams;
import static com.wenchao.cardstack.CardUtils.move;
import static com.wenchao.cardstack.CardUtils.moveFrom;
import static com.wenchao.cardstack.CardUtils.scale;
import static com.wenchao.cardstack.CardUtils.scaleFrom;

public class CardAnimator {
    private static final String DEBUG_TAG = "CardAnimator";
    private static final int REMOTE_DISTANCE = 1000;
    private int mBackgroundColor;
    public ArrayList<View> mCardCollection;     // all card views
    private float mRotation = 0f;
    private HashMap<View, RelativeLayout.LayoutParams> mLayoutsMap;         // view and LayoutParams
    private RelativeLayout.LayoutParams[] mRemoteLayouts = new RelativeLayout.LayoutParams[4];
    private RelativeLayout.LayoutParams baseLayout;
    private int mStackMargin = 20;              // margin between cards

    public CardAnimator(ArrayList<View> viewCollection, int backgroundColor) {
        mCardCollection = viewCollection;
        mBackgroundColor = backgroundColor;
        setup();

    }

    private void setup() {
        mLayoutsMap = new HashMap<View, RelativeLayout.LayoutParams>();

        for (View v : mCardCollection) {
            //setup basic layout
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            params.width = LayoutParams.MATCH_PARENT;
            params.height = LayoutParams.MATCH_PARENT;

            if (mBackgroundColor != -1) {
                v.setBackgroundColor(mBackgroundColor);
            }

            v.setLayoutParams(params);
        }

        baseLayout = (RelativeLayout.LayoutParams) mCardCollection.get(0).getLayoutParams();
        baseLayout = cloneParams(baseLayout);

        initLayout();

        for (View v : mCardCollection) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            RelativeLayout.LayoutParams paramsCopy = cloneParams(params);
            mLayoutsMap.put(v, paramsCopy);
        }

        setupRemotes();

    }

    /**
     * 初始化 Layout
     */
    public void initLayout() {
        int size = mCardCollection.size();
        for (View v : mCardCollection) {
            int index = mCardCollection.indexOf(v);
            if (index != 0) {
                index -= 1;
            }
            LayoutParams params = cloneParams(baseLayout);
            v.setLayoutParams(params);

            scale(v, -(size - index - 1) * 5);
            move(v, index * mStackMargin, 0);
            v.setRotation(0);
        }
    }

    /**
     * 设置 LayoutParams
     */
    private void setupRemotes() {
        View topView = getTopView();
        mRemoteLayouts[0] = getMoveParams(topView, REMOTE_DISTANCE, -REMOTE_DISTANCE);
        mRemoteLayouts[1] = getMoveParams(topView, REMOTE_DISTANCE, REMOTE_DISTANCE);
        mRemoteLayouts[2] = getMoveParams(topView, -REMOTE_DISTANCE, -REMOTE_DISTANCE);
        mRemoteLayouts[3] = getMoveParams(topView, -REMOTE_DISTANCE, REMOTE_DISTANCE);

    }

    /**
     * 获取 Top Card View
     *
     * @return top card view
     */
    private View getTopView() {
        return mCardCollection.get(mCardCollection.size() - 1);
    }

    private void moveToBack(View child) {
        final ViewGroup parent = (ViewGroup) child.getParent();
        if (null != parent) {
            parent.removeView(child);
            parent.addView(child, 0);
        }
    }

    private void reorder() {
        View temp = getTopView();
        //RelativeLayout.LayoutParams tempLp = mLayoutsMap.get(mCardCollection.get(0));
        //mLayoutsMap.put(temp,tempLp);
        moveToBack(temp);

        for (int i = (mCardCollection.size() - 1); i > 0; i--) {
            //View next = mCardCollection.get(i);
            //RelativeLayout.LayoutParams lp = mLayoutsMap.get(next);
            //mLayoutsMap.remove(next);
            View current = mCardCollection.get(i - 1);
            //current replace next
            mCardCollection.set(i, current);
            //mLayoutsMap.put(current,lp);

        }
        mCardCollection.set(0, temp);

        temp = getTopView();

    }

    /**
     * discard animation
     *
     * @param direction SwipeDirection
     * @param al        AnimatorListener
     */
    public void discard(CardUtils.SwipeDirection direction, final AnimatorListener al) {
        AnimatorSet as = new AnimatorSet();             // play a set of animator
        ArrayList<Animator> animatorCollection = new ArrayList<Animator>();

        final View topView = getTopView();
        RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) topView.getLayoutParams();
        RelativeLayout.LayoutParams layout = cloneParams(topParams);
        ValueAnimator discardAnim = ValueAnimator.ofObject(new RelativeLayoutParamsEvaluator(), layout, mRemoteLayouts[direction.ordinal()]);

        discardAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator value) {
                topView.setLayoutParams((LayoutParams) value.getAnimatedValue());
            }
        });

        if (mRotation == 0f) {
            if (direction == CardUtils.SwipeDirection.DIRECTION_BOTTOM_LEFT || direction == CardUtils.SwipeDirection.DIRECTION_TOP_LEFT) {
                mRotation = -40f;
            } else {
                mRotation = 40f;
            }
            ValueAnimator rotateAnim = ValueAnimator.ofFloat(mRotation);
            rotateAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    topView.setRotation((Float) animation.getAnimatedValue());
                }
            });
            rotateAnim.setDuration(250);
            animatorCollection.add(rotateAnim);
        }

        discardAnim.setDuration(250);
        animatorCollection.add(discardAnim);

        for (int i = 0; i < mCardCollection.size(); i++) {
            final View v = mCardCollection.get(i);

            if (v == topView) continue;
            final View nv = mCardCollection.get(i + 1);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
            RelativeLayout.LayoutParams endLayout = cloneParams(layoutParams);
            ValueAnimator layoutAnim = ValueAnimator.ofObject(new RelativeLayoutParamsEvaluator(), endLayout, mLayoutsMap.get(nv));
            layoutAnim.setDuration(250);
            layoutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator value) {
                    v.setLayoutParams((LayoutParams) value.getAnimatedValue());
                }
            });
            animatorCollection.add(layoutAnim);
        }

        as.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reorder();
                if (al != null) {
                    al.onAnimationEnd(animation);
                }
                mLayoutsMap = new HashMap<View, RelativeLayout.LayoutParams>();
                for (View v : mCardCollection) {
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                    RelativeLayout.LayoutParams paramsCopy = cloneParams(params);
                    mLayoutsMap.put(v, paramsCopy);
                }

            }

        });
        as.playTogether(animatorCollection);
        as.start();
        mRotation = 0f;
    }

    /**
     * reverse card, when small drag
     *
     * @param e1 MotionEvent
     * @param e2 MotionEvent
     */
    public void reverse(MotionEvent e1, MotionEvent e2) {
        final View topView = getTopView();
        ValueAnimator rotationAnim = ValueAnimator.ofFloat(mRotation, 0f);
        rotationAnim.setDuration(250);
        rotationAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator v) {
                topView.setRotation((Float) (v.getAnimatedValue()));
            }
        });

        rotationAnim.start();

        for (final View v : mCardCollection) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) v.getLayoutParams();
            RelativeLayout.LayoutParams endLayout = cloneParams(layoutParams);
            ValueAnimator layoutAnim = ValueAnimator.ofObject(new RelativeLayoutParamsEvaluator(), endLayout, mLayoutsMap.get(v));
            layoutAnim.setDuration(250);
            layoutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator value) {
                    v.setLayoutParams((LayoutParams) value.getAnimatedValue());
                }
            });
            layoutAnim.start();
        }
        mRotation = 0f;
    }

    /**
     * 拖拽, 设置动画
     *
     * @param e1        MotionEvent
     * @param e2        MotionEvent
     * @param distanceX distance of x axis
     * @param distanceY distance of y axis
     */
    public void drag(MotionEvent e1, MotionEvent e2, float distanceX,
                     float distanceY) {

        View topView = getTopView();

        float rotation_coefficient = 20f;       // 调整旋转角度, 越大旋转越小

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) topView.getLayoutParams();
        RelativeLayout.LayoutParams topViewLayouts = mLayoutsMap.get(topView);
        int x_diff = (int) ((e2.getRawX() - e1.getRawX()));
        int y_diff = (int) ((e2.getRawY() - e1.getRawY()));

        layoutParams.leftMargin = topViewLayouts.leftMargin + x_diff;
        layoutParams.rightMargin = topViewLayouts.rightMargin - x_diff;
        layoutParams.topMargin = topViewLayouts.topMargin + y_diff;
        layoutParams.bottomMargin = topViewLayouts.bottomMargin - y_diff;

        mRotation = (x_diff / rotation_coefficient);
        topView.setRotation(mRotation);
        topView.setLayoutParams(layoutParams);

        //animate secondary views.
        for (View v : mCardCollection) {
            int index = mCardCollection.indexOf(v);
            if (index == mCardCollection.size() - 2) {      // 移动放大第二层
                LayoutParams l = scaleFrom(v, mLayoutsMap.get(v), (int) (Math.abs(x_diff) * 0.05));
                moveFrom(v, l, 0, (int) (Math.abs(x_diff) * 0.1));
            }
            if (index == mCardCollection.size() - 3) {      // 移动放大第三层
                LayoutParams l = scaleFrom(v, mLayoutsMap.get(v), (int) (Math.abs(x_diff) * 0.02));
                moveFrom(v, l, 0, (int) (Math.abs(x_diff) * 0.05));
            }
        }
    }

    /**
     * set margin between cards
     *
     * @param margin margin between cards
     */
    public void setStackMargin(int margin) {
        mStackMargin = margin;
        initLayout();
    }


}
