package com.wenchao.cardstack;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class CardUtils {

    public enum SwipeDirection {
        DIRECTION_TOP_LEFT(0), DIRECTION_TOP_RIGHT(1), DIRECTION_BOTTOM_LEFT(2), DIRECTION_BOTTOM_RIGHT(3);

        private final int value;

        private SwipeDirection(int value) {
            this.value = value;
        }
    }

    public final static int DIRECTION_TOP_LEFT = 0;
    public final static int DIRECTION_TOP_RIGHT = 1;
    public final static int DIRECTION_BOTTOM_LEFT = 2;
    public final static int DIRECTION_BOTTOM_RIGHT = 3;

    /**
     * scale view with pixel set
     *
     * @param v     the view will scale
     * @param pixel pixel scale
     */
    public static void scale(View v, int pixel) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.leftMargin -= pixel;
        params.rightMargin -= pixel;
        params.topMargin -= pixel;
        params.bottomMargin -= pixel;
        v.setLayoutParams(params);
    }

    public static LayoutParams getMoveParams(View v, int upDown, int leftRight) {
        RelativeLayout.LayoutParams original = (RelativeLayout.LayoutParams) v.getLayoutParams();
        //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(original);
        RelativeLayout.LayoutParams params = cloneParams(original);
        params.leftMargin += leftRight;
        params.rightMargin -= leftRight;
        params.topMargin -= upDown;
        params.bottomMargin += upDown;
        return params;
    }

    /**
     * move view
     *
     * @param v         the view will be moved
     * @param upDown    pixel to move down
     * @param leftRight pixel to move left or right
     */
    public static void move(View v, int upDown, int leftRight) {
        RelativeLayout.LayoutParams params = getMoveParams(v, upDown, leftRight);
        v.setLayoutParams(params);
    }

    /**
     * scale of view
     *
     * @param v      the view will be scaled
     * @param params the LayoutParams from
     * @param pixel  the pixel will be scale
     * @return the params after scale
     */
    public static LayoutParams scaleFrom(View v, LayoutParams params, int pixel) {
        Log.d("pixel", "onScroll: " + pixel);
        params = cloneParams(params);
        params.leftMargin -= pixel;
        params.rightMargin -= pixel;
        params.topMargin -= pixel;
        params.bottomMargin -= pixel;
        Log.d("pixel", "onScroll: " + pixel);
        v.setLayoutParams(params);

        return params;
    }

    /**
     * move view from LayoutParams
     *
     * @param v         the view will be moved
     * @param params    origin LayoutParams
     * @param leftRight left or right pixel
     * @param upDown    up or down pixel
     * @return the params after move
     */
    public static LayoutParams moveFrom(View v, LayoutParams params, int leftRight, int upDown) {
        params = cloneParams(params);
        params.leftMargin += leftRight;
        params.rightMargin -= leftRight;
        params.topMargin -= upDown;
        params.bottomMargin += upDown;
        v.setLayoutParams(params);

        return params;
    }

    //a copy method for RelativeLayout.LayoutParams for backward compartibility
    public static RelativeLayout.LayoutParams cloneParams(RelativeLayout.LayoutParams params) {
        RelativeLayout.LayoutParams copy = new RelativeLayout.LayoutParams(params.width, params.height);
        copy.leftMargin = params.leftMargin;
        copy.topMargin = params.topMargin;
        copy.rightMargin = params.rightMargin;
        copy.bottomMargin = params.bottomMargin;
        int[] rules = params.getRules();
        for (int i = 0; i < rules.length; i++) {
            copy.addRule(i, rules[i]);
        }
        //copy.setMarginStart(params.getMarginStart());
        //copy.setMarginEnd(params.getMarginEnd());

        return copy;
    }

    public static float distance(float x1, float y1, float x2, float y2) {

        return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public static SwipeDirection direction(float x1, float y1, float x2, float y2) {
        if (x2 > x1) {//RIGHT
            if (y2 > y1) {//BOTTOM
                return SwipeDirection.DIRECTION_BOTTOM_RIGHT;
            } else {//TOP
                return SwipeDirection.DIRECTION_TOP_RIGHT;
            }
        } else {//LEFT
            if (y2 > y1) {//BOTTOM
                return SwipeDirection.DIRECTION_BOTTOM_LEFT;
            } else {//TOP
                return SwipeDirection.DIRECTION_TOP_LEFT;
            }
        }
    }


}
