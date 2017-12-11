package com.codebind;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerHandle {


    ServerHandle(){}

    int superDelay2000 = 1;
    int status = 0;

    String IPAddr;
    String user;
    String pass;

    JLabel statusLabel;

    void setupStatusLabel(JLabel label){
        statusLabel = label;
    }

    void setStatusText(String status){
        if(statusLabel != null){
            statusLabel.setText(status);
        }
        else System.out.println("StatusText not ref'd! Status:" + status);
    }

    void delay(int ms) {
        if (superDelay2000 == 1) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    Integer ping(){
        System.out.println("Pinging IP: " + IPAddr);
        delay(2000);
        return 69;
    }

    Integer login(String user, String pass){
        this.user=user;
        this.pass=pass;
        System.out.println("Logging in... IP: " + IPAddr +
                "|user: "+this.user +
                "|pass: "+this.pass);
        delay(2000);
        int isConnected = 1;
        if (isConnected == 1){
            setStatusText("Connected.");
        }
        else{
            setStatusText("Connection error.");
        }
        return isConnected;
    }
    List<String> getServerTree(){
        System.out.println("Getting server tree...");
        delay(2000);
        String[] r =  {"/lol/xD","/lol/xD2","/test.txt","test2.txt"};
        List<String> list = Arrays.asList(r);
        return (list);
    }

    List<String> getRemoteVersions(String name){
        System.out.println("getting backupped versions of: "+name);
        delay(2000);
        String[] r =  {"xd1","xd2"};
        List<String> list = Arrays.asList(r);
        return (list);
    }

    byte[] getRemoteFile(String name, int version){
        System.out.println("getRemoteFile call: file "+name);
        delay(2000);
        int success = 1;
        if (success == 1){
            String a = "FILE CONTENT LOL";
            return a.getBytes();
        }
        else
            return null;

    }

    int deleteRemoteFile(String name, int version){
        delay(2000);
        return 0;
    }

    int backupThisFile(FileNode file){
        File f = file.f;
        System.out.println("Backing up "+f.getName());

        //upload it however you want bby
        delay(2000);
        return 0;
    }
}
