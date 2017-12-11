package com.codebind;

import javax.swing.*;
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

    int getStatus(){
        return status;
    }

    int getPing(){
        return ping();
    }



    Integer ping(){
        delay(2000);
        return 69;
    }

    Integer login(String user, String pass){
        this.user=user;
        this.pass=pass;
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


    List<Integer> uploadFile(FileNode file){

        return new ArrayList<Integer>(Arrays.asList(0));
    }

    byte[] getRemoteFile(String name, int version){
        System.out.println("getRemoteFile call: file "+name);
        delay(2000);
        int success = 1;
        if (success == 1){
            String a = "bepis";
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
        delay(2000);
        return 0;
    }

    List<String> getServerTree(){
        System.out.println("Getting server tree...");
        delay(2000);
        String[] r =  {"/lol/xD","/lol/xD2","/test.txt","test2.txt"};
        List<String> list = Arrays.asList(r);
        return (list);
    }


    List<String> getRemoteVersions(String name){
        delay(2000);
        String[] r =  {"xd1","xd2"};
        List<String> list = Arrays.asList(r);
        return (list);
    }

}
