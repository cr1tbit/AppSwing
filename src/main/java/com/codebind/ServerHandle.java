package com.codebind;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import message.*;
import network.ConnectionHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ServerHandle implements Runnable {
    private BlockingQueue<Message> inQueue;
    private ConnectionHandler connectionHandler;
    //private String rootFolder = "C:\\Users\\Dominik\\Desktop\\Poli\\sem7\\OPA\\AppSwing\\AppSwing\\files";
    private String rootFolder;
    String IPAddr;
    boolean logged;


    private int port;

    ServerHandle(String rootFolder, String ipAddr, int port){
        this.rootFolder = rootFolder;
        this.IPAddr = ipAddr;
        this.port = port;
        this.inQueue = new ArrayBlockingQueue<Message>(256);
        this.logged = false;
    }

    public void run() {
        try {
            launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void launch() throws Exception{

        //EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            connectionHandler = new ConnectionHandler(inQueue, rootFolder);
            Bootstrap b = new Bootstrap();
            //b.group(bossGroup, workerGroup)
            b.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ObjectEncoder(),
                                    new ObjectDecoder(ClassResolvers.weakCachingConcurrentResolver(Message.class.getClassLoader())),
                                    connectionHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128);

            // Connect
            ChannelFuture f = b.connect(IPAddr, port).sync();

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    int superDelay2000 = 1;
    int status = 0;

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

        MsgPing ping = new MsgPing();
        connectionHandler.send(ping);
        try {
            Message reply = inQueue.take();
            if (reply instanceof MsgReply)
                ;//Success
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 69;
    }

    Integer login(String user, String pass){
        this.user=user;
        this.pass=pass;
        System.out.println("Logging in... IP: " + IPAddr +
                "|user: "+this.user +
                "|pass: "+this.pass);

        MsgLogin msg = new MsgLogin(user, pass);
        connectionHandler.send(msg);
        int isConnected = 0;
        try {
            Message reply = inQueue.take();
            if (reply instanceof MsgOk)
                isConnected = 1;
            else if (reply instanceof MsgError)
                ;//String error = ((MsgError) reply).getError();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isConnected == 1){
            setStatusText("Connected.");
            logged = true;
        }
        else{
            setStatusText("Connection error.");
        }
        return isConnected;
    }

    //Moze dac zeby te co maja kilka wersji sie jakos inaczej wyswietlaly
    //Pobieram plik z lista plikow i zapisuje do ..\filelist.list
    //Kazda linijka to "nazwapliku:arch", gdzie arch - bool czy archiwizowac stare wersje
    List<String> getServerTree(){
        if (!logged){
            System.out.println("Not logged in!");
            return new ArrayList<String>();
        }
        System.out.println("Getting server tree...");

        MsgList msg = new MsgList(user);
        connectionHandler.getList(msg);
        int success = 0;
        try {
            Message reply = inQueue.take();
            if (reply instanceof MsgOk)
                success = 1;
            else if (reply instanceof MsgError)
                System.out.println(((MsgError) reply).getString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Splitting list...");
        String[] result;
        String input = "";
        if (!Paths.get("filelist.list").toFile().exists())
            return new ArrayList<String>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get("filelist.list"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String parts[] = line.split(";");
                input = input + ";/" + parts[0];
            }
            br.close();
            Files.move(Paths.get("filelist.list"), Paths.get("filelist"), REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        result = input.split(";");

        List<String> list = Arrays.asList(result);
        System.out.println(list);
        return (list);
    }

    List<String> getRemoteVersions(String name){
        if (!logged){
            System.out.println("Not logged in!");
            return new ArrayList<String>();
        }

        String relPath = name.replace(rootFolder, "");
        System.out.println("getting backupped versions of: "+relPath);
        MsgGetFileVer msg = new MsgGetFileVer(relPath, user);
        connectionHandler.getFileVer(msg);
        try {
            Message reply = inQueue.take();
            System.out.println(reply.toString());
            if (!(reply instanceof MsgFileVer))
                throw new Exception("Didnt get FileVer");
            MsgFileVer fileVer = (MsgFileVer) reply;
            String[] parts = fileVer.getDates().split(";");
            List<String> list = new ArrayList<String>(Arrays.asList(parts));
            System.out.println(list);
            return (list);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        /*String[] r =  {"xd1","xd2"};
        List<String> list = Arrays.asList(r);
        return (list);*/
    }


    //file a nie byte[], moze date a nie int
    File getRemoteFile(String name, String date){
        if (!logged){
            System.out.println("Not logged in!");
            return null;
        }
        System.out.println("getRemoteFile call: file "+name +" of version " + date);
        String dateTemp = date.replace(":","-");
        dateTemp = dateTemp.replace(" ", "_");
        dateTemp = dateTemp.replace("/", ".");



        MsgGetFile msg = new MsgGetFile(name, dateTemp, user);
        connectionHandler.getFile(msg);
        int success = 0;
        try {
            Message reply = inQueue.take();
            if (reply instanceof MsgOk)
                success = 1;
            else if (reply instanceof MsgError)
                ;//String error = ((MsgError) reply).getError();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (success == 1){
            //String a = "FILE CONTENT LOL";
            File f = new File(msg.getPath());
            return f;
        }
        else
            return null;

    }

    int deleteRemoteFile(String name, String date){
        System.out.println("Delete!");
        if (!logged){
            System.out.println("Not logged in!");
            return 0;
        }
        System.out.println("Delete: file "+name +" of version " + date);
        String dateTemp = date.replace(":","-");
        dateTemp = dateTemp.replace(" ", "_");
        dateTemp = dateTemp.replace("/", ".");

        MsgDelete msg = new MsgDelete(name, dateTemp, user);
        connectionHandler.send(msg);
        try {
            Message reply = inQueue.take();
            if (reply instanceof MsgOk)
                return 1;//Success
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    int backupThisFile(FileNode file){
        if (!logged){
            System.out.println("Not logged in!");
            return 0;
        }
        boolean history = false; // czy zapisywać zeszle wersje pliku
        File f = file.f;
        String relPath = file.getRelativePath();
        relPath = relPath.replace(rootFolder, "");

        System.out.println("Backing up "+relPath);
        System.out.println("Backing up "+file.getRelativePath());

        Date date = new Date(f.lastModified());
        List<String> list = getRemoteVersions(relPath);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        if (list.contains(sdf.format(date))) {
            System.out.println("Plik znajduje sie na serwerze.");
            return 0;
        }

        MsgAddFile msg = new MsgAddFile(relPath, user, f.length(), date);
        connectionHandler.sendFile(msg);
        int success = 0;
        try {
            Message reply = inQueue.take();
            if (reply instanceof MsgOk)
                success = 1;
            else if (reply instanceof MsgError)
                ;//String error = ((MsgError) reply).getError();
        } catch (Exception e) {
            e.printStackTrace();
        }


        return success;
    }

    //Zamkniecie polaczenia, przy wyjsciu z programu czy kliknieciu czy cos
    int disconnect(){
        connectionHandler.disconnect();
        return 0;
    }
}
