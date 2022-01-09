package com.harrysoft.androidbluetoothserial.demoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.droidsonroids.gif.GifImageView;

public class StartActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private TextView title;
    private Button loginPageBtn, registerPageBtn, enterBtn;
    private EditText editText_name, editText_passwd;
    private Spinner sp;
    private ObjectAnimator animator1,  animator2, animator3;
    private GifImageView background1;
    private ImageView background2, loginImage, registerImage;

    private DBHelper myDBHelper;
    private ArrayList<AccountInfo> myAccountBook;

    private FirebaseFirestore db;
    private DocumentReference userRef;
    private String account, passwd, identity;
    private Boolean isDatabaseEmpty;
    Map<String,Object> firebaseDataRecord = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        //WindowManager.LayoutParams params = getWindow().getAttributes();
        //params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        //getWindow().setAttributes(params);

        myDBHelper = new DBHelper(this);
        myAccountBook = new ArrayList<AccountInfo>();
        myAccountBook.addAll(myDBHelper.getTotalAccountInfo());
        findObject();
        getAccountSource();
        initial();
        btnClickEvent();
        aniGreeting();
    }

    public void findObject() {
        linearLayout = findViewById(R.id.linearLayout);
        title = findViewById(R.id.textView_title);
        loginPageBtn = findViewById(R.id.btn_login_page);
        registerPageBtn = findViewById(R.id.btn_register_page);
        enterBtn = findViewById(R.id.btn_enter);
        editText_name = findViewById(R.id.editText_name);
        editText_passwd = findViewById(R.id.editText_passwd);
        sp = findViewById(R.id.sp);
        background1 = findViewById(R.id.gifImageView_background1);
        background2 = findViewById(R.id.imageView_background2);
        loginImage = findViewById(R.id.imageView_login);
        registerImage = findViewById(R.id.imageView_register);
    }

    public void getAccountSource() {
        for (int index = 0; index < this.myAccountBook.size(); index++) {
            AccountInfo eachPersonAccountInfo = this.myAccountBook.get(index);
            account = eachPersonAccountInfo.getAccount();
            passwd = eachPersonAccountInfo.getPasswd();
            identity = eachPersonAccountInfo.getIdentity();
        }
        isDatabaseEmpty = account == null;
    }

    private void initial() {
        loginPageBtn.setSelected(true);
        registerPageBtn.setSelected(false);
        loginImage.setImageAlpha(255);
        registerImage.setImageAlpha(0);
        editText_name.setText(account == null ? "" : account);
        editText_passwd.setText(passwd == null? "" : passwd);
        int spId = identity != null && identity.length() > 3 ? 1 : 0;
        sp.setSelection(spId);
        db = FirebaseFirestore.getInstance();
    }

    public void btnClickEvent() {
        loginPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnPage(true, 255, 0, "登入");
            }
        });
        registerPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnPage(false, 0, 255, "註冊");
            }
        });
        enterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account = editText_name.getText().toString();
                passwd = editText_passwd.getText().toString();
                identity = sp.getSelectedItem().toString();

                if(account.equals("") || account.equals("無") || passwd.equals(""))
                    Toast.makeText(StartActivity.this, "請輸入您的資料", Toast.LENGTH_LONG).show();
                else {
                    if(loginPageBtn.isSelected())
                        login(account, passwd, identity);
                    else
                        register(account, passwd, identity);
                }
            }
        });
    }

    public void nextActivity() {
        if (!isDatabaseEmpty)
            myDBHelper.updateToLocalDB(1, account, passwd, identity);
        else
            myDBHelper.insertToLocalDB(account, passwd, identity);

        Intent intent = new Intent();
        intent.setClass(StartActivity.this, MenuActivity.class);
        intent.putExtra("account", account);
        intent.putExtra("passwd", passwd);
        intent.putExtra("identity", identity);
        startActivity(intent);
        finish();
    }

    public void register(String name, String passwd, String identity) {
        userRef = db.collection(identity).document(name);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists())
                    Toast.makeText(StartActivity.this, "該帳號名已被註冊", Toast.LENGTH_LONG).show();
                else {
                    firebaseDataRecord.put("名字", name);
                    firebaseDataRecord.put("密碼", passwd);
                    firebaseDataRecord.put("身分", identity);
                    db.collection(identity).document(name).set(firebaseDataRecord)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    nextActivity();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(StartActivity.this, "無法註冊，請檢查輸入資料", Toast.LENGTH_LONG).show();
                                    Log.d("註冊失敗", e.toString());
                                }
                            });
                }
            }
        });
    }

    public void login(String name, String passwd, String identity) {
        userRef = db.collection(identity).document(name);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String nameAuth = (String) task.getResult().get("名字");
                    String passwdAuth = (String) task.getResult().get("密碼");
                    String identityAuth = (String) task.getResult().get("身分");
                    if (name.equals(nameAuth) && passwd.equals(passwdAuth) && identity.equals(identityAuth))
                        nextActivity();
                    else
                        Toast.makeText(StartActivity.this, "請先註冊或檢查輸入資料", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(StartActivity.this, "請先註冊或檢查輸入資料", Toast.LENGTH_LONG).show();
                    Log.d("登入失敗", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void turnPage(boolean isLoginPage, int loginImageAlpha, int registerImageAlpha, String pageText) {
        loginPageBtn.setSelected(isLoginPage);
        registerPageBtn.setSelected(!isLoginPage);
        loginImage.setImageAlpha(loginImageAlpha);
        registerImage.setImageAlpha(registerImageAlpha);
        linearLayout.setAlpha(0);
        enterBtn.setText(pageText);

        animator1 = ObjectAnimator.ofFloat(linearLayout, "alpha", 0, 1);
        animator1.setDuration(700);
        animator1.start();
    }

    public void aniGreeting() {
        animator1 = ObjectAnimator.ofFloat(background1, "alpha", 1, 0);
        animator1.setDuration(2500);
        animator1.start();

        animator2 = ObjectAnimator.ofFloat(background2, "alpha", 1, 0);
        animator2.setStartDelay(2500);
        animator2.setDuration(5000);
        animator2.start();

        animator3 = ObjectAnimator.ofFloat(title, "alpha", 0, 1);
        animator3.setStartDelay(900);
        animator3.setDuration(500);
        animator3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animator1 = ObjectAnimator.ofFloat(title, "alpha", 1, 0);
                animator1.setStartDelay(1200);
                animator1.setDuration(800);
                animator1.start();
            }
        });
        animator3.start();
    }
}