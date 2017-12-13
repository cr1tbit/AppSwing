package com.codebind;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

public class TextNode extends DefaultMutableTreeNode {

    String name;

    String relPath;

    TextNode(String nodeName){
        super(nodeName);
        name = nodeName;
    }

    String getRelativePath(){

        //Higher beings, these words are for you only

        //our pure vessel has ascended

        //below lies only regret

        //we shall no longer enter this place

        TextNode node = this;
        int fIsRoot = 0;
        int iString = 0;
        String out = new String();

        String nodeNames[] = new String[100];
        while( fIsRoot != 1){
            nodeNames[iString++] = node.name;
            if (node.getParent() != null){
                node = (TextNode)node.getParent();

            }
            else fIsRoot = 1;
        }

        iString --;
        //System.out.println("depth: "+iString);

        for(;iString > 0;){
            out += "/";
            out += nodeNames[--iString];
        }

        //System.out.println("path: " + out);

        //for (TextNode node:nodes ){
         //   out += "/" + node.name;
        //}

        //System.out.println("path of tree root: "+ fileNode.f.getPath());
        return out;
    }

    static int populate(DefaultTreeModel treeModel, String fileName){
        TextNode curNode = (TextNode)treeModel.getRoot();
        System.out.println("CURNODE_NAME:" + curNode.name);
        String[] filePath = fileName.split("/");
        for(int iDepth = 1; iDepth<filePath.length; iDepth++){
            int fNodeFound = 0;
            for( int iChildCount = 0; iChildCount<curNode.getChildCount(); iChildCount++){
                if(((TextNode)curNode.getChildAt(iChildCount)).name.equals(filePath[iDepth])){
                    System.out.println("NODE FOUND:" + curNode.name);

                    fNodeFound = 1;
                    curNode = (TextNode)curNode.getChildAt(iChildCount);
                    iChildCount = 2137; //who keeps 2137 files in one folder? Break from for
                }
            }
            if (fNodeFound == 0){
                TextNode node = new TextNode(filePath[iDepth]);
                System.out.println("ADDING NODE:" + node.name);
                curNode.add(node);
                curNode = node;
            }
        }
        return 0;

    }

}