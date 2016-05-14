package com.prosper.chasing.common.util;

import java.util.LinkedList;
import java.util.List;

public class ViewTransformer<X, Y> {
    
    private static JsonUtil jsonUtil = new JsonUtil();
    
    public static <X, Y> List<Y> transferList(List<X> list, Class<Y> clazz) {
        List<Y> convertedList = new LinkedList<Y>();
        for (X x: list) {
            convertedList.add(transferObject(x, clazz));
        }
        return convertedList;
    }
    
    public static <X, Y> Y transferObject(X x, Class<Y> clazz) {
        String s = jsonUtil.getString(x);
        Y y = jsonUtil.getObject(s, clazz);
        return y;
    }

}
