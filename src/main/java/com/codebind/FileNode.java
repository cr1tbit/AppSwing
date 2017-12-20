package com.codebind;


import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;

public class FileNode extends TextNode {
    static String rootDefault = "/home/vue95/backupDir/";

    String root;

    File f;

    String getRelativePath(){
        FileNode rootNode = (FileNode)getRoot();
        //System.out.println("path of tree root: "+ fileNode.f.getPath());
        return f.getPath().replace(rootNode.f.getPath()+"/","");
    }

    FileNode(String name){
        this (name, rootDefault);
    }

    FileNode(String name, String rootFol){
        super(name);
        root = rootFol;
        f = new File(root+name);
        System.out.println(f.getPath());
        if (f.exists())
            System.out.println("file exists!");
        if (f.isDirectory())
            System.out.println("file is dir!");
        createChildren();
    }

    int createChildren(){
        if (f.isDirectory()){
            String filenames[]=f.list();
            FileNode[] children = new FileNode[filenames.length];
            for(int i=0; i<filenames.length;i++){
                children[i] = new FileNode(filenames[i],f.getPath()+"/");
                add(children[i]);

            }
                return 0;
        }
        else
            return (-1);
    }
    //killChildren();

}
