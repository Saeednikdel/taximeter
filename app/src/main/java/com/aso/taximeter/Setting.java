package com.aso.taximeter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;

public class Setting extends AppCompatActivity {

    AppCompatEditText disEdit , timeEdit,enterEdit,baseEdit;
    SwitchCompat orient,map;
    AppCompatCheckBox sound,vibrate;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        sound = (AppCompatCheckBox) findViewById(R.id.checkBox04);
        vibrate = (AppCompatCheckBox) findViewById(R.id.checkBox03);
        orient=(SwitchCompat)findViewById(R.id.switch1);
        map=(SwitchCompat)findViewById(R.id.switch2);
        disEdit=(AppCompatEditText)findViewById(R.id.dis_TextField);
        timeEdit=(AppCompatEditText)findViewById(R.id.time_TextField);
        baseEdit=(AppCompatEditText)findViewById(R.id.baseDis_TextField);
        enterEdit=(AppCompatEditText)findViewById(R.id.enter_TextField);
        SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
        String string_orient = shared.getString("orientation", "1");
        String string_map = shared.getString("map", "1");
        String string_time = shared.getString("time", "0");
        String string_kilo = shared.getString("kilo", "0");
        String string_enter = shared.getString("enter", "0");
        String string_base = shared.getString("base", "0");
        String string_sound = shared.getString("sound", "0");
        String string_vibrate = shared.getString("vibrate", "0");

        disEdit.setText(string_kilo);
        baseEdit.setText(string_base);
        timeEdit.setText(string_time);
        enterEdit.setText(string_enter);
        if (string_orient.equals("1")){
            orient.setChecked(true);
        }else{
            orient.setChecked(false);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (string_map.equals("1")){
            map.setChecked(true);
        }else{
            map.setChecked(false);
        }
        if (string_sound.equals("1")){
            sound.setChecked(true);

        }else{
            sound.setChecked(false);
        }
        if (string_vibrate.equals("1")){
            vibrate.setChecked(true);

        }else{
            vibrate.setChecked(false);
        }

        sound.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("NewApi") @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (sound.isChecked()){
                    SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("sound", "1");
                    editor.apply();}else{
                    SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("sound", "2");
                    editor.apply();
                }

            }

        });
        vibrate.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("NewApi") @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                if (vibrate.isChecked()){
                    SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("vibrate", "1");
                    editor.apply();}else{
                    SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("vibrate", "2");
                    editor.apply();
                }

            }

        });
        orient.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                displayDialog("برای تغییر نحوه نمایش صفحه باید برنامه مجدد راه اندازی شود");
                if (isChecked){
                    SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("orientation","1");
                    editor.apply();
                }else {
                    SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("orientation","0");
                    editor.apply();
                }
            }
        });
        map.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                displayDialog("برای نمایش نقشه باید برنامه مجدد راه اندازی شود");
                if (isChecked){
                    SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("map","1");
                    editor.apply();
                }else {
                    SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = shared.edit();
                    editor.putString("map","0");
                    editor.apply();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();
        if(!( timeEdit.getText().toString()).isEmpty()){
            editor.putString("time", timeEdit.getText().toString());}else{
            editor.putString("time", "0");
        }
        if(!( enterEdit.getText().toString()).isEmpty()){
            editor.putString("enter", enterEdit.getText().toString());}else{
            editor.putString("enter", "0");
        }
        if(!( disEdit.getText().toString()).isEmpty()){
            editor.putString("kilo", disEdit.getText().toString());}else{
            editor.putString("kilo", "0");
        }
        if(!( baseEdit.getText().toString()).isEmpty()){
            editor.putString("base", baseEdit.getText().toString());}else{
            editor.putString("base", "0");
        }
        editor.apply();
        finish();

    }
    private void displayDialog(String string)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(string);
        alertDialog.setNeutralButton("باشه", null);
        AlertDialog alert = alertDialog.create();
        alert.show();
    }
}
