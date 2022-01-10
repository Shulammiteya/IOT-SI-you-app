package com.harrysoft.androidbluetoothserial.demoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends AppCompatActivity {

    private Button settingBtn, insoleBtn;
    private String account, passwd, identity;

    private FirebaseFirestore db;
    private CollectionReference collectionRef;
    private List<String> nameList;

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
                if(identity.equals("使用者")) {
                    intent.setClass(MenuActivity.this, InsoleActivity.class);
                    intent.putExtra("account", account);
                    intent.putExtra("passwd", passwd);
                    intent.putExtra("identity", identity);
                    startActivity(intent);
                    finish();
                }
                else
                    openListDialog();
            }
        });

        db = FirebaseFirestore.getInstance();
        nameList = new ArrayList<>();
        if(!identity.equals("使用者"))
            getList();
    }

    public void getList() {
        collectionRef = db.collection("醫生或家屬").document(account).collection("權限名單");
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                    for(QueryDocumentSnapshot document : task.getResult())
                        nameList.add(document.getString("名字"));
                else
                    Toast.makeText(MenuActivity.this, "無法獲取名單", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openListDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("請選擇您欲察看的使用者");

        String[] names = new String[nameList.size()];
        final String[] patient = {""};
        for(int i = 0 ; i < nameList.size(); i++)
            names[i] = nameList.get(i);
        dialog.setSingleChoiceItems(names, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                patient[0] = names[which];
            }
        });
        dialog.setPositiveButton("確認", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialoginterface, int index) {
                if(!patient[0].equals("")) {
                    Intent intent = new Intent();
                    intent.setClass(MenuActivity.this, ChartActivity.class);
                    intent.putExtra("account", account);
                    intent.putExtra("passwd", passwd);
                    intent.putExtra("identity", identity);
                    intent.putExtra("patient", patient[0]);
                    startActivity(intent);
                }
            }
        });
        dialog.setNegativeButton("取消", null);
        dialog.show();
    }
}