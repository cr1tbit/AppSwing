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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class App {
    private JPanel panelMain;
    private JButton butConnect;
    private JPanel panelCtrl;
    private JPanel panelTree;
    private JLabel labelStatus;
    private JPanel panelTreeLocal;
    private JPanel panelTreeRemote;
    private JPanel panelCtrlIFace;
    private JPanel panelCtrlIpField;
    private JTextField ipTextField;
    private JTree treeLocal;
    private JLabel labelRootCat;
    private JTree treeRemote;
    private JLabel pingLabel;


    public App() {

        ServerHandle handle = new ServerHandle();

        JPopupMenu myPopupMenu = new JPopupMenu();
        myPopupMenu.setPreferredSize(new Dimension(150, 80));

        JMenuItem menuBackup = new JMenuItem("Backup this item");
        menuBackup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                FileNode node = (FileNode)treeLocal.getLastSelectedPathComponent();
                if (node == null) return;
                JOptionPane.showMessageDialog(null,node.f.getName());
            }
        });
        myPopupMenu.add(menuBackup);
        myPopupMenu.addSeparator();


        treeLocal.setInheritsPopupMenu(true);
        treeLocal.setComponentPopupMenu(myPopupMenu);

        myPopupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {

                myPopupMenu.add(menuBackup);

                new Task_getRemoteVersions(handle,myPopupMenu,"lel").execute();


            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                myPopupMenu.removeAll();
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {

            }
        });
        /*
        butConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JOptionPane.showMessageDialog(null,"kek");
            }
        });

        treeLocal.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                FileNode node = (FileNode)treeLocal.getLastSelectedPathComponent();
                if (node == null) return;
                JOptionPane.showMessageDialog(null,node.f.getName());
            }
        });*/


        FileNode rootNodeLocal = new FileNode("");
        TextNode rootNodeRemote = new TextNode("/");


        DefaultTreeModel treeModelLocal = new DefaultTreeModel(rootNodeLocal);
        DefaultTreeModel treeModelRemote = new DefaultTreeModel(rootNodeRemote);

        new MySwingWorker(
                () -> handle.getServerTree(),
                strings -> {
                    for(Object s:strings){
                        TextNode.populate(treeModelRemote,(String)s);
                    }
                    treeModelRemote.reload();
                } ).execute();

        new MySwingWorker(
                () -> handle.ping(),
                ping -> pingLabel.setText("ping: " + (ping.get(0)).toString()))
                    .execute();


        treeLocal.setModel(treeModelLocal);
        treeRemote.setModel(treeModelRemote);


        treeLocal.addMouseListener(new MouseAdapter() {
        });
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

    static class MySwingWorker extends SwingWorker<Void, Integer>{

        static JProgressBar myBar;

        Supplier<List> getFunc;
        Consumer<List> setIFace;

        List list;

        static void setMyBar(JProgressBar bar){
            myBar = bar;
        }

        MySwingWorker(Supplier<List> getFunc, Consumer<List> setIFace ){
            this.getFunc = getFunc;
            this.setIFace = setIFace;
        }



        @Override
        protected Void doInBackground() throws Exception {
            list = getFunc.get();
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
            setIFace.accept(list);

        }

    }


    static class Task_getRemoteVersions extends SwingWorker<Void, Integer> {

        JPopupMenu jPopupMenu;
        String fileName;
        ServerHandle handle;
        String versions[];

        public Task_getRemoteVersions(ServerHandle handle, JPopupMenu jPopupMenu, String fileName) {
            this.jPopupMenu = jPopupMenu;
            this.fileName = fileName;
            this.handle=handle;
        }


        @Override
        protected Void doInBackground() throws Exception {
            versions = handle.getRemoteVersions(fileName);
            return null;
        }
        @Override
        protected void done() {
            for(int iVersions = 0; iVersions < versions.length; iVersions++){
                JMenuItem remoteFileMenuItem = new JMenuItem(versions[iVersions]);
                jPopupMenu.add(remoteFileMenuItem);
                //jPopupMenu.doLayout();
            }
            //jPopupMenu.setSize((int)jPopupMenu.getSize().getWidth(),(int)jPopupMenu.getSize().getHeight()*5);

            jPopupMenu.revalidate();
            jPopupMenu.repaint();

        }

    }

    /*static class Task_getServerTree extends SwingWorker<Void,Integer> {

        JPopupMenu jPopupMenu;
        String fileName;
        ServerHandle handle;
        String fileList[];

        public Task_getServerTree(ServerHandle handle,DefaultTreeModel treeModel) {
            this.handle=handle;
        }

        @Override
        protected void process(List<Integer> chunks) {
            int i = chunks.get(chunks.size()-1);
            //jpb.setValue(i); // The last value in this array is all we care about.
            System.out.println("process: " + i);
            //label.setText("Loading " + i + " of " + max);
        }

        @Override
        protected Void doInBackground() throws Exception {
            fileList = handle.getServerTree();
            return null;
        }
        @Override
        protected void done() {
            DefaultMutableTreeNode node = treeMode
            for(int iFileList = 0; iFileList < fileList.length; iFileList++){
                JMenuItem remoteFileMenuItem = new JMenuItem(versions[iVersions]);
                jPopupMenu.add(remoteFileMenuItem);
                //jPopupMenu.doLayout();
            }
            //jPopupMenu.setSize((int)jPopupMenu.getSize().getWidth(),(int)jPopupMenu.getSize().getHeight()*5);

            jPopupMenu.revalidate();
            jPopupMenu.repaint();

        }

    }*/



}

