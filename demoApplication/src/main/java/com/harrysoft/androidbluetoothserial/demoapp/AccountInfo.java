package com.harrysoft.androidbluetoothserial.demoapp;

public class AccountInfo {
    private int uID;
    private String account, identity, passwd;

    public void init (int id ,String acc, String identity, String passwd) {
        this.uID=id;
        this.account=acc;
        this.identity=identity;
        this.passwd=passwd;
    }

    public int getUserID() {
        return this.uID;
    }
    public String getAccount(){ return this.account;}
    public String getIdentity(){
        return this.identity;
    }
    public String getPasswd(){
        return this.passwd;
    }
}
