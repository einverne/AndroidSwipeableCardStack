package com.wenchao.cardstack;

public class DefaultStackEventListener implements CardStack.CardEventListener {

    private float mThreshold;

    public DefaultStackEventListener(int i) {
        mThreshold = i;
    }

    @Override
    public boolean swipeEnd(CardUtils.SwipeDirection section, float distance) {
        return distance > mThreshold;
    }

    @Override
    public boolean swipeStart(CardUtils.SwipeDirection section, float distance) {

        return false;
    }

    @Override
    public boolean swipeContinue(CardUtils.SwipeDirection section, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void discarded(int mIndex, CardUtils.SwipeDirection direction) {

    }

    @Override
    public void topCardTapped() {

    }


}
