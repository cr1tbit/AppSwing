package com.codebind;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class TextNode extends DefaultMutableTreeNode {

    String name;

    TextNode(String nodeName){
        super(nodeName);
        name = nodeName;
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