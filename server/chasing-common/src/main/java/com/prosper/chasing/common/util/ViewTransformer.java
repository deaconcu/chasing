package com.prosper.chasing.common.util;

import java.util.LinkedList;
import java.util.List;

public class ViewTransformer<X, Y> {
    
    public static <X, Y> List<Y> transferList(List<X> list, Class<Y> clazz) {
        List<Y> convertedList = new LinkedList<Y>();
        for (X x: list) {
            convertedList.add(transferObject(x, clazz));
        }
        return convertedList;
    }
    
    public static <X, Y> Y transferObject(X x, Class<Y> clazz) {
        String s = JsonUtil.getString(x);
        Y y = JsonUtil.getObject(s, clazz);
        return y;
    }

}
