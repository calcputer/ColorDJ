package com.relight.colordj;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class MainActivity extends ActionBarActivity implements ColorDJColorInterface {

    private boolean record = false;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ColorDJView cdjView = (ColorDJView) findViewById(R.id.colorDJPanel);
        cdjView.receiveSender(this);
        //ImageView img = (ImageView) findViewById(R.id.red_bubble);
        //img.
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        if(){  //record button
            record = true;
            //do i need to return something
        }

        return super.onOptionsItemSelected(item);
    }

    public void colorChanged(int color){
        //send via tasker
        //record
        //compare accuracy
        Log.d(TAG, "#CDJ: color = " + Integer.toHexString(color));

        if(record){
            //put the color in a data structure
        }
    }

}
