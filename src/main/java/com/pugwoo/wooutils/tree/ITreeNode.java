package com.pugwoo.wooutils.tree;

import java.util.List;

/**
 * 树形节点接口
 * @author sapluk
 * 2018-05-02
 */
public interface ITreeNode {
	
    /**id，不允许重复*/
    String getNodeId();

    /**获取父级id*/
    String getNodeParentId();

    /**获取子节点*/
    List<? extends ITreeNode> getChildren();
    
    /**初始化children，一般是给它一个新list*/
    void initChildren();

    /**获取排序，如无seq字段，返回null*/
    default Integer getSeq(){
        return null;
    }
}
