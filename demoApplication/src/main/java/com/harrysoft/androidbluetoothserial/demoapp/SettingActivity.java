package com.harrysoft.androidbluetoothserial.demoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    private TextView textName, textPasswd, textList;
    private ArrayAdapter listAdapter;
    private ListView listView;

    private DBHelper myDBHelper;
    private FirebaseFirestore db;
    private CollectionReference collectionRef;
    private DocumentReference documentRef;

    private String account, passwd, identity;
    private List<String> selectedNameList, nameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setting);

        account = getIntent().getStringExtra("account");
        passwd = getIntent().getStringExtra("passwd");
        identity = getIntent().getStringExtra("identity");

        myDBHelper = new DBHelper(this);
        findObject();
        initial();
        getSelectedList();
        if(identity.equals("?????????"))
            getList();
    }

    public void findObject() {
        textName = findViewById(R.id.textView_name);
        textPasswd = findViewById(R.id.textView_passwd);
        textList = findViewById(R.id.textView_list);
        listView = findViewById(R.id.listView);
    }

    private void initial() {
        db = FirebaseFirestore.getInstance();
        textName.setText("????????? " + account);
        textPasswd.setMovementMethod(LinkMovementMethod.getInstance());
        textPasswd.setText(setSpannableString());
        textPasswd.setHighlightColor(Color.TRANSPARENT);
        textList.setText(identity.equals("?????????") ? "??????/???????????????" : "??????????????????");
        nameList = new ArrayList<>();
    }

    public void getSelectedList() {
        selectedNameList = new ArrayList<>();
        collectionRef = db.collection(identity).document(account).collection("????????????");
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        String dName = document.getString("??????");
                        selectedNameList.add(dName);
                    }
                    if(selectedNameList.size() == 0)
                        selectedNameList.add("???");
                    listAdapter = new ArrayAdapter(SettingActivity.this, android.R.layout.simple_list_item_1, selectedNameList);
                    listView.setAdapter(listAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                            if(identity.equals("?????????"))
                                openListDialog();
                        }
                    });
                } else {
                    Toast.makeText(SettingActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void getList() {
        collectionRef = db.collection("???????????????");
        collectionRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                    for(QueryDocumentSnapshot document : task.getResult())
                        nameList.add(document.getString("??????"));
                else
                    Toast.makeText(SettingActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openListDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("??????????????? ?????????????????????");

        String[] names = new String[nameList.size()];
        boolean[] checkedItems = new boolean[nameList.size()];
        for(int i = 0 ; i < nameList.size(); i++) {
            names[i] = nameList.get(i);
            checkedItems[i] = selectedNameList.contains(names[i]);
        }
        dialog.setMultiChoiceItems(names, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) { }
        });
        dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialoginterface, int index) {
                for(int i = 0 ; i < nameList.size(); i++) {
                    if (checkedItems[i] && !selectedNameList.contains(names[i]))
                        addFirebaseList(names[i]);
                    else if(!checkedItems[i] && selectedNameList.contains(names[i]))
                        removeFirebaseList(names[i]);
                }
                getSelectedList();
            }
        });
        dialog.setNegativeButton("??????", null);
        dialog.show();
    }

    private void removeFirebaseList(String name) {
        db.collection(identity).document(account).collection("????????????").document(name).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        db.collection("???????????????").document(name).collection("????????????").document(account).delete()
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SettingActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void addFirebaseList(String name) {
        Map<String, Object> note = new HashMap<>();
        note.put("??????", name);
        db.collection(identity).document(account).collection("????????????").document(name).set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> note = new HashMap<>();
                        note.put("??????", account);
                        db.collection("???????????????").document(name).collection("????????????").document(account).set(note)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SettingActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                    }
                });
    }

    public SpannableString setSpannableString() {
        SpannableString spannableString = new SpannableString("????????? ??????");
        ClickableSpan clickablespan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                openPasswdDialog();
            }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                if (textPasswd.isPressed())
                    ds.setColor(Color.DKGRAY);
                textPasswd.invalidate();
            }
        };
        spannableString.setSpan(clickablespan, 4, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private void openPasswdDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.passwd_dialog, null, false);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("????????????");
        builder.setView(dialogView);
        builder.setCancelable(true);
        builder.setPositiveButton("??????", null);
        builder.setNegativeButton("??????", null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText oldPasswd = dialogView.findViewById(R.id.old_passwd),
                        newPasswd = dialogView.findViewById(R.id.new_passwd),
                        confirmPasswd = dialogView.findViewById(R.id.confirm_passwd);
                String oldStr = oldPasswd.getText().toString(),
                        newStr = newPasswd.getText().toString(),
                        confirmStr = confirmPasswd.getText().toString();
                documentRef = db.collection(identity).document(account);
                documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult().exists()) {
                            String passwdAuth = (String) task.getResult().get("??????");
                            if (oldStr.equals(passwdAuth)) {
                                if(newStr.equals(confirmStr)) {
                                    if(!newStr.equals((""))) {
                                        Map<String, Object> note = new HashMap<>();
                                        note.put("??????", account);
                                        note.put("??????", newStr);
                                        note.put("??????", identity);
                                        db.collection(identity).document(account).set(note)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        myDBHelper.updateToLocalDB(1, account, newStr, identity);
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(SettingActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                    else
                                        Toast.makeText(SettingActivity.this, "?????????????????????", Toast.LENGTH_LONG).show();
                                }
                                else
                                    Toast.makeText(SettingActivity.this, "???????????????????????????", Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(SettingActivity.this, "?????????????????????", Toast.LENGTH_LONG).show();
                        }
                        else
                            Toast.makeText(SettingActivity.this, "??????????????????", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}