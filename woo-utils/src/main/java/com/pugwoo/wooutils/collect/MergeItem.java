package com.pugwoo.wooutils.collect;

public interface MergeItem {

    /**
     * 获得排序对象的序号，不能是null，否则程序将抛出空指针异常。
     * seq值最好保证都不同，不然有跳过数据的可能。
     * @return
     */
    Comparable getSeq();

}
