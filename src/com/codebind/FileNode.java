package com.codebind;

import com.sun.org.apache.regexp.internal.RE;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;

public class FileNode extends TextNode {
    static String rootDefault = "/home/vue95/backupDir/";

    String root;

    File f;

    FileNode getTwins( DefaultTreeModel targetModel){
        int reachedDepth = 0;
        System.out.println();
        String relPath = getRelativePath();
        String[] paths = relPath.split("/");
        System.out.println("relPath: " + relPath);
        FileNode tempNode = (FileNode)targetModel.getRoot();
        System.out.println("depth:"+paths.length);
        for(int iNodeDepth = 1; iNodeDepth<paths.length;iNodeDepth++){
            System.out.println("layer: "+iNodeDepth + " ChildCount: "+tempNode.getChildCount());
            for(int iChild = 0; iChild< tempNode.getChildCount(); iChild++){
                if(((FileNode)tempNode.getChildAt(iChild)).f.getName().contains(paths[iNodeDepth])) {
                    tempNode = (FileNode) tempNode.getChildAt(iChild);
                    reachedDepth = iNodeDepth;
                    break;
                }
            }
        }
        if (reachedDepth == paths.length-1) return tempNode;
        else return null;
    }

    //FileNode getTwin( DefaultTreeModel targetModel){
    //    DefaultMutableTreeNode targetRootNode = (DefaultMutableTreeNode) targetModel.getRoot();
    //    getPathToRoot();
    //}






    String getRelativePath(){
        FileNode fileNode = (FileNode)getRoot();
        //System.out.println("path of tree root: "+ fileNode.f.getPath());
        return f.getPath().replace(fileNode.f.getPath(),"");
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
