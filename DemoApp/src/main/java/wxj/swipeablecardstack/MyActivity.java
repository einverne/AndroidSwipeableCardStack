package wxj.swipeablecardstack;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wenchao.cardstack.CardStack;
import com.wenchao.cardstack.CardUtils;
import com.wenchao.cardstack.CardsDataAdapter;


public class MyActivity extends Activity implements View.OnClickListener{
    private static final String TAG = "EV_TAG";
    private CardStack mCardStack;
    private CardsDataAdapter mCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        mCardStack = (CardStack)findViewById(R.id.container);
        findViewById(R.id.btn_dislike).setOnClickListener(this);
        findViewById(R.id.btn_like).setOnClickListener(this);

        mCardStack.setContentResource(R.layout.card_content);
        mCardStack.setStackMargin(20);

        mCardAdapter = new CardsDataAdapter(getApplicationContext());
        mCardAdapter.add("test1");
        mCardAdapter.add("test2");
        mCardAdapter.add("test3");
        mCardAdapter.add("test4");
        mCardAdapter.add("test5");
        mCardAdapter.add("test6");
        mCardAdapter.add("test7");
        mCardAdapter.add("test8");
        mCardAdapter.add("test9");
        mCardAdapter.add("test10");


        mCardStack.setAdapter(mCardAdapter);

        if(mCardStack.getAdapter() != null) {
            Log.i("MyActivity", "Card Stack size: " + mCardStack.getAdapter().getCount());
        }

        mCardStack.setListener(new CardStack.CardEventListener() {
            @Override
            public boolean swipeEnd(CardUtils.SwipeDirection section, float distance) {
                Log.d(TAG, "swipeEnd");

                return (distance>300)? true : false;
            }

            @Override
            public boolean swipeStart(CardUtils.SwipeDirection section, float distance) {
                Log.d(TAG, "swipeStart");
                return true;
            }

            @Override
            public boolean swipeContinue(CardUtils.SwipeDirection section, float distanceX, float distanceY) {
                Log.d(TAG, "swipeContinue");

                return true;
            }

            @Override
            public void discarded(int mIndex, CardUtils.SwipeDirection direction) {
                Log.d(TAG, "discarded " + mIndex + " direction " + direction);

            }

            @Override
            public void topCardTapped() {
                Log.d(TAG, "topCardTapped");

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dislike:
                mCardStack.discardTop(CardUtils.SwipeDirection.DIRECTION_BOTTOM_LEFT);

                break;
            case R.id.btn_like:
                mCardStack.discardTop(CardUtils.SwipeDirection.DIRECTION_BOTTOM_RIGHT);

                break;
        }
    }
}
