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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;

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


    public App() {

        ServerHandle handle = new ServerHandle(8080);
        Thread serverHandleThread = new Thread(handle);
        serverHandleThread.start();


        labelRootCatVal.setText(rootFolder);

        handle.setupStatusLabel(labelStatusVal);
        handle.setStatusText("App init");

        FileNode rootNodeLocal = new FileNode("",rootFolder);
        TextNode rootNodeRemote = new TextNode("/");

        DefaultTreeModel treeModelLocal = new DefaultTreeModel(rootNodeLocal);
        DefaultTreeModel treeModelRemote = new DefaultTreeModel(rootNodeRemote);

        //Button listeners:
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


        //setup popup menu bound to right clicking on a file:
        JPopupMenu popupTreeLocal = new JPopupMenu();
        popupTreeLocal.setPreferredSize(new Dimension(150, 80));

        JMenuItem menuBackup = new JMenuItem("Backup this item");
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
        popupTreeLocal.add(menuBackup);

        popupTreeLocal.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
                popupTreeLocal.add(menuBackup);
                FileNode node = (FileNode)treeLocal.getLastSelectedPathComponent();
                if (node == null) return;
                new MySwingWorker<List>(
                        () -> handle.getRemoteVersions(node.getRelativePath()),
                        strings -> {
                            for(Object s:strings){//add menu entity for every file version
                                JMenuItem menuItem = new JMenuItem((String)s);
                                menuItem.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent actionEvent) {
                                        new MySwingWorker<File>(
                                                () -> handle.getRemoteFile((String)s,0),
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

        treeLocal.setModel(treeModelLocal);
        treeRemote.setModel(treeModelRemote);

        //treeLocal.addMouseListener(new MouseAdapter() {
        //});

        /*
        treeLocal.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                FileNode node = (FileNode)treeLocal.getLastSelectedPathComponent();
                if (node == null) return;
                JOptionPane.showMessageDialog(null,node.f.getName());
            }
        });*/



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

