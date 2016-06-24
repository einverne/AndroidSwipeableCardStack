package com.wenchao.cardstack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;


public class CardStack extends RelativeLayout {
    private int mColor = -1;
    private int mIndex = 0;
    private int mNumVisible = 4;
    private boolean canSwipe = true;
    private ArrayAdapter<?> mAdapter;
    private OnTouchListener mOnTouchListener;
    private CardAnimator mCardAnimator;
    //private Queue<View> mIdleStack = new Queue<View>;

    //ArrayList
    ArrayList<View> viewCollection = new ArrayList<View>();

    private CardEventListener mEventListener = new DefaultStackEventListener(300);
    private int mContentResource = 0;               // Card res

    public interface CardEventListener {
        //section
        // 0 | 1
        //--------
        // 2 | 3
        // swipe distance, most likely be used with height and width of a view ;

        /**
         * swipe end callback, if return true the dismiss animation will be triggered
         * if return false, the card will move back to stack
         * distance is finger swipe distance in dp
         *
         * @param section  direction
         * @param distance distance of finger swipe in dp
         * @return true, dismiss animation triggered; false, move back to stack
         */
        boolean swipeEnd(CardUtils.SwipeDirection section, float distance);

        /**
         * swipe start callback
         *
         * @param section  direction
         * @param distance distance
         * @return true; false
         */
        boolean swipeStart(CardUtils.SwipeDirection section, float distance);

        /**
         * swipe continue callback
         *
         * @param section   direction
         * @param distanceX distance of x axis
         * @param distanceY distance of y axis
         * @return true ; false
         */
        boolean swipeContinue(CardUtils.SwipeDirection section, float distanceX, float distanceY);

        /**
         * this callback invoked when dismiss animation is finished.
         *
         * @param mIndex
         * @param direction the direction of a card when dismiss
         */
        void discarded(int mIndex, CardUtils.SwipeDirection direction);

        /**
         * click on top Card callback
         */
        void topCardTapped();
    }

    /**
     * move top card, only discard callback will be called
     *
     * @param direction SwipeDirection
     */
    public void discardTop(final CardUtils.SwipeDirection direction) {
        mCardAnimator.discard(direction, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator arg0) {
                mCardAnimator.initLayout();
                mIndex++;
                mEventListener.discarded(mIndex, direction);

                //mIndex = mIndex%mAdapter.getCount();
                loadLast();

                viewCollection.get(0).setOnTouchListener(null);
                viewCollection.get(viewCollection.size() - 1).setOnTouchListener(mOnTouchListener);
            }
        });
    }

    public int getCurrIndex() {
        //sync?
        return mIndex;
    }

    //only necessary when I need the attrs from xml, this will be used when inflating layout
    public CardStack(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CardStack);
            mColor = array.getColor(R.styleable.CardStack_backgroundColor, mColor);
            array.recycle();
        }

        //String sMyValue = attrs.getAttributeValue( "http://schemas.android.com/apk/res/android", "padding" );
        //get attrs assign minVisiableNum
        for (int i = 0; i < mNumVisible; i++) {
            addContainerViews();
        }
        setupAnimation();
    }

    /**
     * add view to viewCollection
     */
    private void addContainerViews() {
        FrameLayout v = new FrameLayout(getContext());
        viewCollection.add(v);
        addView(v);
    }

    /**
     * set margin between cards
     *
     * @param margin margin between cards
     */
    public void setStackMargin(int margin) {
        mCardAnimator.setStackMargin(margin);
        mCardAnimator.initLayout();
    }

    public void setContentResource(int res) {
        mContentResource = res;
    }

    /**
     * set whether can be swiped
     *
     * @param can true can swipe; false not
     */
    public void setCanSwipe(boolean can) {
        this.canSwipe = can;
    }

    /**
     * After changed adapter, reset views and animation
     *
     * @param resetIndex
     */
    public void reset(boolean resetIndex) {
        if (resetIndex) mIndex = 0;
        removeAllViews();
        viewCollection.clear();
        for (int i = 0; i < mNumVisible; i++) {
            addContainerViews();
        }
        setupAnimation();
        loadData();
    }

    public void setVisibleCardNum(int visiableNum) {
        mNumVisible = visiableNum;
        reset(false);
    }

    public void setThreshold(int t) {
        mEventListener = new DefaultStackEventListener(t);
    }

    /**
     * set CardEventListener
     *
     * @param cel CardEventListener
     */
    public void setListener(CardEventListener cel) {
        mEventListener = cel;
    }

    /**
     * set up cardAnimation
     */
    private void setupAnimation() {
        final View cardView = viewCollection.get(viewCollection.size() - 1);
        mCardAnimator = new CardAnimator(viewCollection, mColor);
        mCardAnimator.initLayout();

        final DragGestureDetector dd = new DragGestureDetector(CardStack.this.getContext(), new DragGestureDetector.DragListener() {

            @Override
            public boolean onDragStart(MotionEvent e1, MotionEvent e2,
                                       float distanceX, float distanceY) {
                float x1 = e1.getRawX();
                float y1 = e1.getRawY();
                float x2 = e2.getRawX();
                float y2 = e2.getRawY();
                final CardUtils.SwipeDirection direction = CardUtils.direction(x1, y1, x2, y2);
                float distance = CardUtils.distance(x1, y1, x2, y2);

                if (canSwipe) {
                    mCardAnimator.drag(e1, e2, distanceX, distanceY);
                }
                mEventListener.swipeStart(direction, distance);
                return true;
            }

            @Override
            public boolean onDragContinue(MotionEvent e1, MotionEvent e2,
                                          float distanceX, float distanceY) {
                float x1 = e1.getRawX();
                float y1 = e1.getRawY();
                float x2 = e2.getRawX();
                float y2 = e2.getRawY();
                //float distance = CardUtils.distance(x1,y1,x2,y2);
                final CardUtils.SwipeDirection direction = CardUtils.direction(x1, y1, x2, y2);
                if (canSwipe) {
                    mCardAnimator.drag(e1, e2, distanceX, distanceY);
                }
                mEventListener.swipeContinue(direction, Math.abs(x2 - x1), Math.abs(y2 - y1));
                return true;
            }

            @Override
            public boolean onDragEnd(MotionEvent e1, MotionEvent e2) {
                //reverse(e1,e2);
                float x1 = e1.getRawX();
                float y1 = e1.getRawY();
                float x2 = e2.getRawX();
                float y2 = e2.getRawY();
                float distance = CardUtils.distance(x1, y1, x2, y2);
                final CardUtils.SwipeDirection direction = CardUtils.direction(x1, y1, x2, y2);

                boolean discard = mEventListener.swipeEnd(direction, distance);
                if (discard) {
                    if (canSwipe) {
                        discardTop(direction);
                    }
                } else {
                    if (canSwipe) {
                        mCardAnimator.reverse(e1, e2);
                    }
                }
                return true;
            }

            @Override
            public boolean onTapUp() {
                mEventListener.topCardTapped();
                return true;
            }
        }
        );

        mOnTouchListener = new OnTouchListener() {
            private static final String DEBUG_TAG = "MotionEvents";

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                dd.onTouchEvent(event);
                return true;
            }
        };
        cardView.setOnTouchListener(mOnTouchListener);
    }

    private DataSetObserver mOb = new DataSetObserver() {
        @Override
        public void onChanged() {
            reset(false);
        }
    };

    public CardStack(Context context) {
        super(context);
    }

    public void setAdapter(final ArrayAdapter<?> adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mOb);
        }
        mAdapter = adapter;
        adapter.registerDataSetObserver(mOb);

        loadData();
    }

    public ArrayAdapter getAdapter() {
        return mAdapter;
    }

    private void loadData() {
        for (int i = mNumVisible - 1; i >= 0; i--) {
            ViewGroup parent = (ViewGroup) viewCollection.get(i);
            int index = (mIndex + mNumVisible - 1) - i;
            if (index > mAdapter.getCount() - 1) {
                parent.setVisibility(View.GONE);
            } else {
                View child = mAdapter.getView(index, getContentView(), this);
                parent.addView(child);
                parent.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 加载传入 CardView
     *
     * @return View
     */
    private View getContentView() {
        View contentView = null;
        if (mContentResource != 0) {
            LayoutInflater lf = LayoutInflater.from(getContext());
            contentView = lf.inflate(mContentResource, null);
        }
        return contentView;

    }

    private void loadLast() {
        ViewGroup parent = (ViewGroup) viewCollection.get(0);

        int lastIndex = (mNumVisible - 1) + mIndex;
        if (lastIndex > mAdapter.getCount() - 1) {
            parent.setVisibility(View.GONE);
            return;
        }

        View child = mAdapter.getView(lastIndex, getContentView(), parent);
        parent.removeAllViews();
        parent.addView(child);
    }

    public int getStackSize() {
        return mNumVisible;
    }
}
