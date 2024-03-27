package com.example.thesis2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Select Timer Module
 *
 * Prompts the user to select a time control
 */

public class SelectTimer extends AppCompatActivity implements View.OnClickListener{
    private List<Button> buttonLabels;

    private static final int[] BUTTON_LABEL_IDS = {
            R.id.button_classical, R.id.button_rapid, R.id.button_blitz, R.id.button_custom_1,
            R.id.button_custom_2
    };

    private static final int[] BUTTON_OTHER_IDS = {
            R.id.button_edit1, R.id.button_edit2, R.id.button_edit3, R.id.button_edit4,
            R.id.button_edit5, R.id.button_start, R.id.button_back
    };

    private static long mTime;
    private static int mIncrement;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_timer);

        initializeViews();
        updateTextViews();

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Select Timer");
    }

    private void initializeViews(){
        toolbar = findViewById(R.id.toolbar);

        buttonLabels = new ArrayList<>(BUTTON_LABEL_IDS.length);
        for(int id : BUTTON_LABEL_IDS) {
            Button button = findViewById(id);
            button.setOnClickListener(this);
            buttonLabels.add(button);
        }

        for(int id : BUTTON_OTHER_IDS) {
            Button button = findViewById(id);
            button.setOnClickListener(this);
        }
    }

    private void updateTextViews(){
        for(int i = 0; i < buttonLabels.size(); i++){
            if(SharedPref.getStrPref(this, SharedPref.getFormatName(i+49)) != null){
                buttonLabels.get(i).setText(SharedPref.getStrPref(this,
                        SharedPref.getFormatName(i+49)));
            }
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.button_start:
                if (mTime != 0) {
                    SharedPref.setLongPref(this, SharedPref.MAIN_TIME, mTime);
                    SharedPref.setIntPref(this, SharedPref.INCREMENT, mIncrement);
                }
                startActivity(new Intent(this, ChessClock.class));
                break;
            case R.id.button_back:
                startActivity(new Intent(this, ChessClock.class));
                break;
            case R.id.button_classical:
                setSpecificTime(49, 5400000);
                String timer = SharedPref.getStrPref(this,
                        SharedPref.getFormatName(49));
                Toast.makeText(SelectTimer.this, getString(R.string.select_custom, timer),
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_rapid:
                setSpecificTime(50, 1500000);
                timer = SharedPref.getStrPref(this,
                        SharedPref.getFormatName(50));
                Toast.makeText(SelectTimer.this, getString(R.string.select_custom, timer),
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_blitz:
                setSpecificTime(51, 300000);
                timer = SharedPref.getStrPref(this,
                        SharedPref.getFormatName(51));
                Toast.makeText(SelectTimer.this, getString(R.string.select_custom, timer),
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_custom_1:
                setSpecificTime(52, 0);
                timer = SharedPref.getStrPref(this,
                        SharedPref.getFormatName(52));
                Toast.makeText(SelectTimer.this, getString(R.string.select_custom, timer),
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_custom_2:
                setSpecificTime(53, 0);
                timer = SharedPref.getStrPref(this,
                        SharedPref.getFormatName(53));
                Toast.makeText(SelectTimer.this, "Selected " + timer + " Time Control.",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_edit1:
                setButtonIndex(49);
                break;
            case R.id.button_edit2:
                setButtonIndex(50);
                break;
            case R.id.button_edit3:
                setButtonIndex(51);
                break;
            case R.id.button_edit4:
                setButtonIndex(52);
                break;
            case R.id.button_edit5:
                setButtonIndex(53);
                break;
        }
    }

    private void setSpecificTime(int index, long defaultTime){
        char button_index_char = (char) index;
        SharedPref.setIntPref(this, SharedPref.BTN_INDEX, index);
        mTime = SharedPref.getLongPref(this, SharedPref.getFormatKey(button_index_char), defaultTime);
        mIncrement = SharedPref.getIntPref(this, SharedPref.getFormatIncrement(button_index_char), 0);
    }

    private void setButtonIndex(int index){
        SharedPref.setIntPref(this, SharedPref.BTN_INDEX, index);
        setScrollChoiceTime(index);
        startActivity(new Intent(this, SetUpTimer.class));
    }

    private void setScrollChoiceTime(int index){
        char button_index_char = (char) index;
        long longtime = SharedPref.getLongPref(this, SharedPref.getFormatKey(button_index_char), 0);
        int time = (int) longtime;
        int seconds = time/1000;
        int minutes = seconds/60;
        int hour = minutes/60;

        if (minutes > 0)
            seconds = seconds % 60;
        if (hour > 0)
            minutes = minutes % 60;

        SharedPref.setIntPref(this, SharedPref.INDEX_HOUR, hour);
        SharedPref.setIntPref(this, SharedPref.INDEX_MINUTES, minutes);
        SharedPref.setIntPref(this, SharedPref.INDEX_SECONDS, seconds);
    }
}
