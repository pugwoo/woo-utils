package com.pugwoo.wooutils.tree;

import com.pugwoo.wooutils.collect.ListUtils;

import java.util.*;

/**
 * 将以list存储的扁平的树形数据，转换成内存中树形数据结构
 * @author sapluk
 * 2018-05-02
 */
public class TreeUtils {

    /**
     * list列表转成树形结构
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
	public static <T extends ITreeNode> List<T> genTreeNode(List<T> list) {
        List<T> result = new ArrayList<>();
        if(list.isEmpty()) {return result;}

        Map<String, ITreeNode> map = new HashMap<>();
        for(ITreeNode l : list) {
            map.put(l.getNodeId(), l);
        }

        Set<String> linked = new HashSet<>();
        for(Map.Entry<String, ITreeNode> entry : map.entrySet()) {
            String parentId = entry.getValue().getNodeParentId();
            if(parentId != null) {
                ITreeNode treeVO = map.get(parentId);
                if(treeVO != null) {
                    linked.add(entry.getKey());
                    if(treeVO.getChildren() == null) {
                        treeVO.initChildren();
                    }
					List<ITreeNode> children = (List<ITreeNode>) treeVO.getChildren();
                    children.add(entry.getValue());
                }
            }
        }

        for(Map.Entry<String, ITreeNode> entry : map.entrySet()) {
            ITreeNode treeVO = entry.getValue();
            if(treeVO.getChildren() != null) {
                ListUtils.sortAscNullLast(treeVO.getChildren(), o -> o.getSeq());
            }
            if(!linked.contains(entry.getKey())) {
                result.add((T)treeVO);
            }
        }

        ListUtils.sortAscNullLast(result, o -> o.getSeq());
        return result;
    }
}
