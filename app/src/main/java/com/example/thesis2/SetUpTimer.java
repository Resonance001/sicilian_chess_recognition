package com.example.thesis2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.webianks.library.scroll_choice.ScrollChoice;

import java.util.ArrayList;
import java.util.List;

/**
 * Setup Timer Module.
 *
 * Creates a customized time control with user input.
 */
@SuppressWarnings("ALL")
public class SetUpTimer extends AppCompatActivity implements View.OnClickListener {
    TextView textViewHour, textViewMinutes, textViewSeconds;
    EditText custom, increment;
    CheckBox checkIncrement;
    Button confirm, back;
    Toolbar toolbar;

    ScrollChoice scrollChoiceHour, scrollChoiceMinutes, scrollChoiceSeconds;
    List<String> values = new ArrayList<>();
    int hour, minutes, seconds;
    int button_index_num;
    char button_index_char;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_timer);

        initializeViews();
        initializeIndex();

        loadHourData();

        hour = SharedPref.getIntPref(this, SharedPref.INDEX_HOUR, 0);
        scrollChoiceHour.addItems(values, hour);

        scrollChoiceHour.setOnItemSelectedListener(new ScrollChoice.OnItemSelectedListener(){
            @Override
            public void onItemSelected(ScrollChoice scrollChoice, int position, String name){
                textViewHour.setText(getString(R.string.hour));
                textViewHour.append(name);
                hour = Integer.parseInt(name);
            }
        });

        loadMinSecData();

        minutes = SharedPref.getIntPref(this, SharedPref.INDEX_MINUTES, 0);
        scrollChoiceMinutes.addItems(values, minutes);
        seconds = SharedPref.getIntPref(this, SharedPref.INDEX_SECONDS, 0);
        scrollChoiceSeconds.addItems(values, seconds);

        scrollChoiceMinutes.setOnItemSelectedListener(new ScrollChoice.OnItemSelectedListener(){
            @Override
            public void onItemSelected(ScrollChoice scrollChoice, int position, String name){
                textViewMinutes.setText(getString(R.string.minutes));
                textViewMinutes.append(name);
                minutes = Integer.parseInt(name);
            }
        });
        scrollChoiceSeconds.setOnItemSelectedListener(new ScrollChoice.OnItemSelectedListener(){
            @Override
            public void onItemSelected(ScrollChoice scrollChoice, int position, String name){
                textViewSeconds.setText(getString(R.string.seconds));
                textViewSeconds.append(name);
                seconds = Integer.parseInt(name);
            }
        });

        int currIncrement =  SharedPref.getIntPref(this, SharedPref.getFormatIncrement(button_index_char), 0);
        if (currIncrement != 0) {
            checkIncrement.setChecked(true);
            increment.setVisibility(View.VISIBLE);
            increment.setText(String.valueOf(currIncrement));
        }

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("Customize Timer");
    }

    private void initializeViews(){
        toolbar = findViewById(R.id.toolbar);

        textViewHour = findViewById(R.id.text_view_hour);
        textViewMinutes = findViewById(R.id.text_view_minutes);
        textViewSeconds = findViewById(R.id.text_view_seconds);

        custom = findViewById(R.id.text_button_name);

        scrollChoiceHour = findViewById(R.id.scroll_choice_hour);
        scrollChoiceMinutes = findViewById(R.id.scroll_choice_minutes);
        scrollChoiceSeconds = findViewById(R.id.scroll_choice_seconds);

        increment = findViewById(R.id.text_increment);
        checkIncrement = findViewById(R.id.checkbox);

        confirm = findViewById(R.id.button_confirm);
        back = findViewById(R.id.button_back);

        checkIncrement.setOnClickListener(this);
        confirm.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private void initializeIndex(){
        button_index_num = SharedPref.getIntPref(this, SharedPref.BTN_INDEX, 0);
        button_index_char = (char) button_index_num;

        custom.setText(SharedPref.getStrPref(this,SharedPref.getFormatName(button_index_char)));

        textViewHour.setText(getString(R.string.hour));
        textViewMinutes.setText(getString(R.string.minutes));
        textViewSeconds.setText(getString(R.string.seconds));

        textViewHour.append(Integer.toString(SharedPref.getIntPref(this, SharedPref.INDEX_HOUR, 0)));
        textViewMinutes.append(Integer.toString(SharedPref.getIntPref(this, SharedPref.INDEX_MINUTES, 0)));
        textViewSeconds.append(Integer.toString(SharedPref.getIntPref(this, SharedPref.INDEX_SECONDS, 0)));
    }

    private void loadHourData(){
        for(int i = 0; i < 24; i++){
            values.add(String.valueOf(i));
        }
    }

    private void loadMinSecData(){
        for(int i = 24; i < 61; i++){
            values.add(String.valueOf(i));
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.checkbox:
                if (checkIncrement.isChecked())
                    increment.setVisibility(View.VISIBLE);
                else {
                    increment.setVisibility(View.INVISIBLE);
                    SharedPref.setIntPref(this, SharedPref.getFormatIncrement(button_index_char), 0);
                }
                break;
            case R.id.button_confirm:
                SharedPref.setStrPref(this,
                        SharedPref.getFormatName(button_index_char), custom.getText().toString());
                SharedPref.setIntPref(this, SharedPref.BTN_INDEX, button_index_num);

                SharedPref.setLongPref(this, SharedPref.getFormatKey(button_index_char), getTime());
                SharedPref.setLongPref(this, SharedPref.MAIN_TIME, getTime());

                if(checkIncrement.isChecked()){
                    int num;
                    try {
                        num = Integer.parseInt(increment.getText().toString());
                    } catch (NumberFormatException e) {
                        num = 0;
                    }
                    SharedPref.setIntPref(this, SharedPref.getFormatIncrement(button_index_char), num);
                }
                startActivity(new Intent(this, SelectTimer.class));
                break;
            case R.id.button_back:
                startActivity(new Intent(this, SelectTimer.class));
                break;
        }
    }
    private long getTime(){

        long hour_long = hour;
        long minutes_long = minutes;
        long seconds_long = seconds;

        hour_long *= 3600000;
        minutes_long *= 60000;
        seconds_long *= 1000;

        return hour_long + minutes_long + seconds_long;
    }
}
