package com.pugwoo.wooutils.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;

/**
 * 比对工具对于所有的get*方法的值进行比对
 * 如果类型已经实现了Comparable，则按照Comparable定义进行比对
 * 如果是数组，则按照有序进行比对
 * 如果是List，则按照配置是否有顺序进行比对，默认有序排列
 * 如果是Set或Map，则无需进行比对
 * 如果是Stream流类型，不比对，认为是相等的
 * 如果是用户类型，则往下继续比对
 *
 * 支持指定排除掉的属性，不区分大小写
 * 如果有一个对象是null有一个不是，则认为不相等；如果两个都是null，则认为相等
 * 如果两个对象内存地址相同，则认为相等
 *
 * 支持链式写法进行配置
 */
public class EqualUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EqualUtils.class);

    private List<String> ignoreAttrs = new ArrayList<>();
    private boolean ignoreListOrder = false;

    /**
     * 忽略比对List元素的顺序
     * @param ignore 是否忽略List元素，true为忽略
     */
    public EqualUtils ignoreListOrder(boolean ignore) {
        this.ignoreListOrder = ignore;
        return this;
    }

    /**
     * 忽略指定属性的比对
     * @param attr 要忽略的属性
     */
    public EqualUtils ignoreAttr(String attr) {
        if(attr == null || attr.trim().isEmpty()) { // ignore & pass
            return this;
        }
        attr = attr.trim().toLowerCase();
        if(ignoreAttrs.contains(attr)) {
            return this;
        }
        ignoreAttrs.add(attr);
        return this;
    }

    /**
     * 比对两个对象是否相等
     */
    public boolean isEqual(Object a, Object b) {
        if(a == b) {return true;}
        if(a == null || b == null) {return false;}
        if(!a.getClass().equals(b.getClass())) {return false;}

        if(a instanceof Comparable) {
            return ((Comparable) a).compareTo(b) == 0;
        }

        if(a.getClass().isArray()) {
            return compareArray(a, b);
        }
        if(a instanceof List) {
            return compareList((List<?>) a, (List<?>) b);
        }
        if(a instanceof Map) {
            return compareMap((Map<?, ?>) a, (Map<?, ?>) b);
        }
        if(a instanceof Set) {
            return compareSet((Set<?>) a, (Set<?>) b);
        }

        if(a instanceof Stream || a instanceof InputStream || a instanceof OutputStream) {
            return true;
        }

        // 用户自定义对象，对于每个get方法的值进行比对
        List<Method> methods = getAllGetterMethods(a.getClass());
        try {
            for(Method method : methods) {
                Object aE = method.invoke(a);
                Object bE = method.invoke(b);
                if(!isEqual(aE, bE)) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.error("compare getter method fail", e);
        }

        return true;
    }

    private boolean compareArray(Object a, Object b) {
        if(a instanceof int[]) {
            return Arrays.equals((int[])a, (int[])b);
        }
        if(a instanceof byte[]) {
            return Arrays.equals((byte[])a, (byte[])b);
        }
        if(a instanceof char[]) {
            return Arrays.equals((char[])a, (char[])b);
        }
        if(a instanceof long[]) {
            return Arrays.equals((long[])a, (long[])b);
        }
        if(a instanceof float[]) {
            return Arrays.equals((float[])a, (float[])b);
        }
        if(a instanceof short[]) {
            return Arrays.equals((short[])a, (short[])b);
        }
        if(a instanceof double[]) {
            return Arrays.equals((double[])a, (double[])b);
        }
        if(a instanceof boolean[]) {
            return Arrays.equals((boolean[])a, (boolean[])b);
        }

        // 现在a已经是Object[]
        Object[] arrA = (Object[]) a;
        Object[] arrB = (Object[]) b;
        if(arrA.length != arrB.length) {
            return false;
        }
        for(int i = 0; i < arrA.length; i++) {
            if(!isEqual(arrA[i], arrB[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean compareList(List<?> a, List<?> b) {
        if(a.size() != b.size()) {
            return false;
        }
        if(ignoreListOrder) {
            Set<Integer> usedIndex = new HashSet<>();
            for(Object aE : a) {
                boolean matched = false;
                for(int i = 0; i < b.size(); i++) {
                    if(isEqual(aE, b.get(i)) && !usedIndex.contains(i)) {
                        usedIndex.add(i);
                        matched = true;
                        break;
                    }
                }
                if(!matched) {
                    return false;
                }
            }
        } else {
            for(int i = 0; i < a.size(); i++) {
                if(!isEqual(a.get(i), b.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean compareMap(Map<?,?> a, Map<?,?> b) {
        if(a.size() != b.size()) {
            return false;
        }
        for(Map.Entry<?,?> entry : a.entrySet()) {
            Object o = b.get(entry.getKey());
            if(o == null || !isEqual(entry.getValue(), o)) {
                return false;
            }
        }
        return true;
    }

    private boolean compareSet(Set<?> a, Set<?> b) {
        if(a.size() != b.size()) {
            return false;
        }
        for(Object aE : a) {
            boolean isMatch = false;
            for(Object bE : b) {
                if(isEqual(aE, bE)) {
                    isMatch = true;
                }
            }
            if(!isMatch) {
                return false;
            }
        }
        return true;
    }

    /**
     * 会忽略要忽略掉的属性
     */
    private List<Method> getAllGetterMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<>();
        do {
            for (Method m : clazz.getMethods()) {
                String methodName = m.getName();
                if (methodName.startsWith("get") && m.getParameterTypes().length == 0
                    && Modifier.isPublic(m.getModifiers()) && !methodName.equals("getClass")) {

                    String attName = methodName.substring(3).toLowerCase();
                    if(!ignoreAttrs.contains(attName)) {
                        methods.add(m);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != null);

        return methods;
    }

}
