package com.codebind;

import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.swing.JOptionPane.NO_OPTION;


public class App {
    private JPanel panelMain;
        private JPanel panelTree;
            private JPanel panelTreeLocal;
            private JPanel panelTreeRemote;
        private JPanel panelCtrl;


    //tree objects:
    private JTree treeLocal;
    private JTree treeRemote;


    //buttons:
    private JButton butConnect;
    private JButton butCheckStatus;
    private JButton butGetTree;
    private JButton butDoNothing;
    private JButton butDisconnect;


    //Static labels:
    private JLabel labelStatus;
    private JLabel labelIP;
    private JLabel labelUsername;
    private JLabel labelPassword;
    private JLabel labelRootCat;

    //dynamic labels:
    private JLabel labelRootCatVal;
    private JLabel labelStatusVal;
    private JTextField textFieldIPVal;
    private JTextField textFieldUsernameVal;
    private JPasswordField passwordFieldPWVal;

    private JLabel pingLabel;

    private String rootFolder = "/home/vue95/backupDir/";
    private String IPAddr = "127.0.0.1";
    private int port = 8080;
    //private String rootFolder = "C:\\Users\\Dominik\\Desktop\\Poli\\sem7\\OPA\\AppSwing\\AppSwing\\files";
    private String user = "dominik";

    public void fileWriteFromBytes(String name, byte[] data){

        boolean writeFile = true;//perform action only if this equals true

        //Check if file exists, check how old it is, how big it is, ask user
        File f = new File(name);
        if (f.exists()){
            if (JOptionPane.showConfirmDialog(
                    null,
                    "Do you want to overwrite existing file?\n" +
                            "local file size: "+ f.length() +
                            "\nremote file size: "+ data.length,
                    "? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ? ?",
                    JOptionPane.YES_NO_OPTION) == NO_OPTION) writeFile = false;
        }

        if (f.isDirectory()){
            JOptionPane.showMessageDialog(null,"cannot write folder.");
            writeFile = false;
        }
        //write to file
        if (writeFile == true) {
            FileOutputStream stream =
                    null;
            try {
                stream = new FileOutputStream(name);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                stream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void saveSettings(){
        try {
            Path path = Paths.get("settings.conf");
            Path tempPath = Paths.get("settings.conf.temp");
            BufferedWriter bw = Files.newBufferedWriter(tempPath);
            bw.write("IPAddr:" + IPAddr + System.lineSeparator());
            bw.write("port:" + port + System.lineSeparator());
            bw.write("rootFolder:" + rootFolder + System.lineSeparator());
            bw.write("user:" + user + System.lineSeparator());
            bw.close();
            Files.move(tempPath, path, REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSettings() {
        Map<String, String> settings = new HashMap<String, String>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get("settings.conf"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String parts[] = line.split(":");
                settings.put(parts[0], parts[1]);
            }

            port = Integer.parseInt(settings.get("port"));
            rootFolder = settings.get("rootFolder");
            IPAddr = settings.get("IPAddr");
            user = settings.get("user");

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public App() {

        //load settings from a file
        File settingsFile = new File("settings.conf");
        if (settingsFile.exists()){
            loadSettings();
        } else {
        //Or else default values are loaded, and the file is created.
            saveSettings();
        }



        //Setup server handle
        ServerHandle handle = new ServerHandle(port);
        Thread serverHandleThread = new Thread(handle);
        serverHandleThread.start();


        labelRootCatVal.setText(rootFolder);

        handle.setupStatusLabel(labelStatusVal);
        handle.setStatusText("App init");

        /*********************
         * GUI SETUP
         *********************/

        FileNode rootNodeLocal = new FileNode("",rootFolder);
        TextNode rootNodeRemote = new TextNode("/");

        DefaultTreeModel treeModelLocal = new DefaultTreeModel(rootNodeLocal);
        DefaultTreeModel treeModelRemote = new DefaultTreeModel(rootNodeRemote);

        treeLocal.setModel(treeModelLocal);
        treeRemote.setModel(treeModelRemote);

        ///////////////////
        //Button listeners:
        ///////////////////

        butCheckStatus.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handle.IPAddr = textFieldIPVal.getText();
                new MySwingWorker<Integer>(
                        () -> handle.ping(),
                        ping -> pingLabel.setText("ping: " + ping))
                        .execute();
            }
        });

        butConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                handle.IPAddr = textFieldIPVal.getText();
                String user = textFieldUsernameVal.getText();
                String pass = passwordFieldPWVal.getText();
                new MySwingWorker<Integer>(
                        () -> handle.login(user,pass),
                        stat -> {})
                        .execute();
            }
        });

        butGetTree.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new MySwingWorker<List>(
                        () -> handle.getServerTree(),
                        strings -> {
                            for(Object s:strings){
                                TextNode.populate(treeModelRemote,(String)s);
                            }
                            treeModelRemote.reload();
                        } ).execute();
            }
        });

        butDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                new MySwingWorker<>(
                        () -> handle.disconnect(),
                        strings -> {}
                         ).execute();
            }
        });

        //////////////////
        //Local tree popup:
        //////////////////

        //setup popup menu bound to right clicking on a file:
        JPopupMenu popupTreeLocal = new JPopupMenu();
        popupTreeLocal.setPreferredSize(new Dimension(250, 80));

        //add menu entry to backup file
        JMenuItem menuBackup = new JMenuItem("Just back my file up");
        menuBackup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileNode node = (FileNode)treeLocal.getLastSelectedPathComponent();
                if (node == null) return;
                new MySwingWorker<Integer>(
                        () -> handle.backupThisFile(node),
                        status -> treeModelRemote.reload()).execute();
            }
        });

        //add menu entries to download each versions retrieved from server
        popupTreeLocal.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                popupTreeLocal.removeAll();
                popupTreeLocal.add(menuBackup);
                popupTreeLocal.add(new JSeparator());
                FileNode node = (FileNode)treeLocal.getLastSelectedPathComponent();
                if (node == null) return;
                System.out.println("relative path: "+ node.getRelativePath());
                new MySwingWorker<List>(
                        () -> handle.getRemoteVersions(node.getRelativePath()),
                        strings -> {
                            for(int iStrings = 0; iStrings<strings.size();iStrings++)   {
                                final int versionIndex = iStrings;
                                JMenuItem menuItem =
                                        new JMenuItem("Download: "+node.name+ " - " + (String)strings.get(iStrings));
                                menuItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent actionEvent) {
                                        new MySwingWorker<File>(
                                                () -> handle.getRemoteFile((String)strings.get(versionIndex),versionIndex),
                                                bytes -> {}//fileWriteFromBytes(rootFolder+node.getRelativePath(),bytes)
                                        ).execute();
                                    }
                                });
                                popupTreeLocal.add(menuItem);
                            }
                            popupTreeLocal.revalidate();
                            popupTreeLocal.repaint();
                        } ).execute();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {

                popupTreeLocal.removeAll();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {}
        });

        treeLocal.setInheritsPopupMenu(true);
        treeLocal.setComponentPopupMenu(popupTreeLocal);

        ////////////////////
        //Remote tree popup:
        ////////////////////

        //popup bound to right click on remote tree
        JPopupMenu popupTreeRemote = new JPopupMenu();
        popupTreeRemote.setPreferredSize(new Dimension(250, 80));

        //Dummy menu entry, DELET THIS
        JMenuItem menuDummy = new JMenuItem("Rick and morty fan");

        //dynamically add menu entries to download/delete versions
        popupTreeRemote.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                popupTreeRemote.removeAll();
                popupTreeRemote.add(menuDummy);
                TextNode node = (TextNode)treeRemote.getLastSelectedPathComponent();
                if (node == null) return;
                System.out.println("relative path: "+ node.getRelativePath());
                new MySwingWorker<List>(
                        () -> handle.getRemoteVersions(node.getRelativePath()),
                        strings -> {
                            for(int iStrings = 0; iStrings<strings.size();iStrings++)   {
                                final int versionIndex = iStrings;

                                //Menu item with download option:
                                JMenuItem menuItem =
                                        new JMenuItem("Download: "+node.name+ " - " + (String)strings.get(iStrings));
                                menuItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent actionEvent) {
                                        new MySwingWorker<File>(
                                                () -> handle.getRemoteFile((String)strings.get(versionIndex),versionIndex),
                                                bytes -> {}//fileWriteFromBytes(rootFolder+node.getRelativePath(),bytes)
                                        ).execute();
                                    }
                                });
                                popupTreeRemote.add(menuItem);
                                JMenuItem menuItem2 =
                                        new JMenuItem("Delete: "+node.name+ " - " + (String)strings.get(iStrings));
                                menuItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent actionEvent) {
                                        new MySwingWorker<>(
                                                () -> handle.deleteRemoteFile((String)strings.get(versionIndex),versionIndex),
                                                bytes -> {
                                                    new MySwingWorker<List>(
                                                            () -> handle.getServerTree(),
                                                            strings -> {
                                                                for(Object s:strings){
                                                                    TextNode.populate(treeModelRemote,(String)s);
                                                                }
                                                                treeModelRemote.reload();
                                                            } ).execute();
                                                }
                                        ).execute();

                                    }
                                });
                                popupTreeRemote.add(menuItem2);

                            }
                            popupTreeRemote.revalidate();
                            popupTreeRemote.repaint();
                        } ).execute();
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {

                popupTreeRemote.removeAll();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {}
        });

        treeRemote.setInheritsPopupMenu(true);
        treeRemote.setComponentPopupMenu(popupTreeRemote);

    }


    public static void main(String[] args) {
        JFrame frame = new JFrame("App");
        frame.setSize(500,500);

        App app = new App();

        frame.setContentPane(app.panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }



    static class MySwingWorker<T> extends SwingWorker<Void, Integer>{

        //can be bound to see progress. Not very useful atm.
        static JProgressBar myBar;

        //lambda that takes a long time to complete
        // - probably an server request
        Supplier<T> getFunc;

        //lambda executed at the end of the workers'
        //life - probably an interface modification
        //command/
        Consumer<T> setIFace;

        //basic data handle for this worker. Care has
        //to be taken of, as data type is defined rather
        //in lambda call than inside of this class.
        T data;

        static void setMyBar(JProgressBar bar){
            myBar = bar;
        }

        MySwingWorker(Supplier<T> getFunc, Consumer<T> setIFace ){
            this.getFunc = getFunc;
            this.setIFace = setIFace;
        }

        @Override
        protected Void doInBackground() throws Exception {
            data = getFunc.get();
            publish(50);
            return null;
        }

        @Override
        protected void process(List<Integer> chunks) {
            int i = chunks.get(chunks.size()-1);
            if(myBar != null)
                myBar.setValue(i); // The last value in this array is all we care about.
            else
                System.out.println("Bar not referenced! Prog: " + i);
        }

        @Override
        protected void done() {
            setIFace.accept(data);

        }
    }
}

