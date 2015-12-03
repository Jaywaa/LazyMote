package com.jaywaa.lazymote;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.os.Vibrator;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;


public class TouchPad extends ActionBarActivity {

    public static final String VERSION="1.0";
    private float prevX;
    private float prevY;

    private Link link;

    private Vibrator vibrator;

    private InputMethodManager imm;

    private long click;
    private long doubleTap=0;

    private boolean holdLeftClick;

    private SharedPreferences prefs;

    private String HOST;
    private int PORT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch_pad);

        prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        HOST = prefs.getString("host_ip", "localhost");
        PORT = Integer.parseInt(prefs.getString("host_port", "53366"));

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        link = new Link(HOST,PORT);

        Toast.makeText(getApplicationContext(), "Sending on "+HOST+":"+PORT, Toast.LENGTH_SHORT).show();

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        final MousePad mousePad = (MousePad)findViewById(R.id.mousePad);

        Button leftClick = (Button)findViewById(R.id.btnLeft);
        leftClick.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                link.sendLeftPress();
                vibrator.vibrate(500);
                holdLeftClick = true;
                return true;
            }
        });

        mousePad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent ev) {

                final int action = ev.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    click = System.nanoTime();

                    if (System.nanoTime() - doubleTap < 200000000)
                    {
                        holdLeftClick = true;
                        link.sendLeftPress();
                    }
                    doubleTap = System.nanoTime();
                    prevX = ev.getX();
                    prevY = ev.getY();
                }
                if (action == MotionEvent.ACTION_UP) {

                    mousePad.setTouchUp();

                    if (System.nanoTime() - click < 200000000) {

                        if (holdLeftClick)
                        {
                            link.sendLeftRelease();
                            holdLeftClick = false;
                        } else {
                            link.sendLeftClick();
                            vibrator.vibrate(50);
                        }
                    }
                    if (holdLeftClick) {
                        link.sendLeftRelease();
                        holdLeftClick = false;
                    }
                }
                if (action == MotionEvent.ACTION_MOVE) {

                    if (Float.isNaN(prevX))
                        prevX = ev.getX();
                    if (Float.isNaN(prevY))
                        prevY = ev.getY();

                    // left as an exercise for the reader
                    final float xDiff = ev.getX() - prevX;
                    final float yDiff = ev.getY() - prevY;

                    prevX = ev.getX();
                    prevY = ev.getY();

                    link.sendMovement((int) xDiff, (int) yDiff);
                    mousePad.setTouchLocation(prevX, prevY);
                   // mousePad.setTouchLocation(prevX,prevY);

                    // Touch slop should be calculated using ViewConfiguration
                    // constants.
                    if (Math.abs(xDiff) > 10 || Math.abs(yDiff) > 10) {

                        click = 0; // cancel the click
                    }
                }
                return true;
            }
        });
    }

    public void leftButton(View v)
    {
        if (holdLeftClick) {
            link.sendLeftRelease();
            holdLeftClick = false;
        } else {
            link.sendLeftClick();
            vibrator.vibrate(50);
        }
    }
    public void rightButton(View v)
    {
        link.sendRightClick();
        vibrator.vibrate(50);
        if (holdLeftClick) {
            link.sendLeftRelease();
            holdLeftClick = false;
        }
    }

    public void toggleKeyboard(View v)
    {
        if (imm.isAcceptingText())
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        else
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,0);

    }
    public void actionButton(View v)
    {
        Button btnAction = (Button)v;

        switch (btnAction.getId()){

            case R.id.btnVolIncr:{
                link.sendIncreaseVolume();
                vibrator.vibrate(50);
                break;
            }
            case R.id.btnVolDecr:{
                link.sendDecreaseVolume();
                vibrator.vibrate(50);
                break;
            }
            case R.id.btnVolMute:{
                link.sendToggleMute();
                vibrator.vibrate(50);
                break;
            }
        }

    }
    @Override
    public void onResume()
    {
        super.onResume();

        /* Check if preferences have been changed */
        String host = prefs.getString("host_ip","localhost");
        int port = Integer.parseInt(prefs.getString("host_port","53366"));
        if (!host.equals(HOST) || port != PORT)
        {
            HOST = host;
            PORT = port;
            if (link != null)
                link.close();
            link = new Link(HOST, PORT);
            Toast.makeText(getApplicationContext(), "Sending on "+HOST+":"+PORT, Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int keyaction = event.getAction();

        if (keyaction == KeyEvent.ACTION_DOWN)
        {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DEL)
                link.sendUnicodePress(8);
            else if (event.getUnicodeChar() != 0)
                link.sendUnicodePress(event.getUnicodeChar());
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_touch_pad, menu);
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
