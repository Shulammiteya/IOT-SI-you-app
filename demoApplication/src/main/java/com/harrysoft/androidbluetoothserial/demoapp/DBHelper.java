package com.harrysoft.androidbluetoothserial.demoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    private Context nowContext;
    private final String uID = "ID";
    private final String account = "Account";
    private final String identity = "Identity";
    private final String passwd = "Passwd";

    private static final String databaseName = "LocalDB";
    private static final int databaseVersion = 1;
    private final String tableName = "SIyouAccountBook";

    private final String createTableSQL ="CREATE TABLE IF NOT EXISTS " + this.tableName + "( " + this.uID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + this.account + " VARCHAR(255), " + this.identity + " VARCHAR(255), " + this.passwd + " VARCHAR(255) ) ;" ;
    private final String deleteTableSQL="DROP TABLE IF EXISTS "+this.tableName+" ;";

    public  DBHelper(Context context) {
        super(context, databaseName,null, databaseVersion);
        this.nowContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            sqLiteDatabase.execSQL(this.createTableSQL);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        try {
            sqLiteDatabase.execSQL(this.deleteTableSQL);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertToLocalDB(String acc, String passwd, String id) {
        SQLiteDatabase myLocalDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(this.account, acc);
        contentValues.put(this.identity, id);
        contentValues.put(this.passwd, passwd);
        long nowID = myLocalDB.insert(this.tableName, null, contentValues);

        /*
        final String successlog = "新增成功!\n\n編號 : " + nowID + "\n帳戶 : " + acc + "\n密碼 : " + passwd + "\n身分 : " + id;
        Toast.makeText(this.nowContext, successlog, Toast.LENGTH_LONG).show();
        */
    }

    public void updateToLocalDB(int id, String newAcc, String newPasswd, String newId) {
        SQLiteDatabase myLocalDB=this.getWritableDatabase();
        ContentValues contentValues=new ContentValues();
        contentValues.put(this.account, newAcc);
        contentValues.put(this.identity, newId);
        contentValues.put(this.passwd, newPasswd);

        String[] argu = { String.valueOf(id) };
        int affectRow = myLocalDB.update(this.tableName, contentValues,this.uID + " = ? ", argu);

        if(affectRow == 0) {
            Toast.makeText(this.nowContext, "更新失敗，請重新再試", Toast.LENGTH_LONG).show();
        }
        /*else {
            final String successlog = "更新成功!\n\n編號 : " + id + "\n帳戶 : " + newAcc + "\n密碼 : " + newPasswd + "\n身分 : " + newId;
            Toast.makeText(this.nowContext, successlog, Toast.LENGTH_LONG).show();
        }*/
    }

    public ArrayList<AccountInfo> getTotalAccountInfo() {
        ArrayList<AccountInfo> totalAccountInfo = new ArrayList<AccountInfo>();

        SQLiteDatabase myLocalDB = this.getReadableDatabase();
        String[] myColumn = {this.uID, this.account, this.identity, this.passwd};
        Cursor myCursor = myLocalDB.query(this.tableName, myColumn,null,null,null,null, uID);
        while(myCursor.moveToNext()) {
            int id = myCursor.getInt(myCursor.getColumnIndex(this.uID));
            String acc = myCursor.getString(myCursor.getColumnIndex(this.account));
            String identity = myCursor.getString(myCursor.getColumnIndex(this.identity));
            String passwd = myCursor.getString(myCursor.getColumnIndex(this.passwd));
            AccountInfo eachAccountInfo = new AccountInfo();
            eachAccountInfo.init(id, acc, identity, passwd);
            totalAccountInfo.add(eachAccountInfo);
        }
        return totalAccountInfo;
    }

}
