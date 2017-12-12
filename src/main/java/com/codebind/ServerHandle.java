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
import network.ConnectionInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ServerHandle implements Runnable {
    private BlockingQueue<Message> inQueue;
    private ConnectionHandler connectionHandler;

    private int port;

    ServerHandle(int port){
        this.port = port;
        this.inQueue = new ArrayBlockingQueue<Message>(256);
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
            connectionHandler = new ConnectionHandler(inQueue);
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
            ChannelFuture f = b.connect("192.168.43.35", port).sync();

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


    //file a nie byte[], moze date a nie int
    byte[] getRemoteFile(String name, int version){
        System.out.println("getRemoteFile call: file "+name);

        MsgGetFile msg = new MsgGetFile(name, user);
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
        MsgAddFile msg = new MsgAddFile(file.getRelativePath(), user, f.length());
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
}
