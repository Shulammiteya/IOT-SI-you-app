package com.harrysoft.androidbluetoothserial.demoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    private Button settingBtn, insoleBtn;
    private String account, passwd, identity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_menu);

        account = getIntent().getStringExtra("account");
        passwd = getIntent().getStringExtra("passwd");
        identity = getIntent().getStringExtra("identity");

        Intent intent = new Intent();
        settingBtn = findViewById(R.id.btn_setting);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent.setClass(MenuActivity.this, SettingActivity.class);
                intent.putExtra("account", account);
                intent.putExtra("passwd", passwd);
                intent.putExtra("identity", identity);
                startActivity(intent);
            }
        });
        insoleBtn = findViewById(R.id.btn_start);
        insoleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(identity.equals("使用者"))
                    intent.setClass(MenuActivity.this, InsoleActivity.class);
                else
                    intent.setClass(MenuActivity.this, ChartActivity.class);
                intent.putExtra("account", account);
                intent.putExtra("passwd", passwd);
                intent.putExtra("identity", identity);
                startActivity(intent);
                if(identity.equals("使用者"))
                    finish();
            }
        });
    }
}