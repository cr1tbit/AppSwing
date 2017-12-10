package com.codebind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerHandle {


    ServerHandle(){}

    int superDelay2000 = 1;

    void delay(int ms) {
        if (superDelay2000 == 1) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    int getPing(){
        return ping().get(0);
    }

    List<Integer> ping(){
        delay(2000);
        return new ArrayList<Integer>(Arrays.asList(69));
    }

    List<Integer> login(String user, String pass){
        delay(2000);
        return new ArrayList<Integer>(Arrays.asList(0));
    }


    List<Integer> uploadFile(FileNode file){

        return new ArrayList<Integer>(Arrays.asList(0));
    }

    byte[] getRemoteFile(String name, int version){
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
    };

    String[] getRemoteVersions(String name){
        delay(2000);
        String[] r =  {"xd1","xd2"};
        return (r);
    }
}
