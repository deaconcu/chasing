package com.prosper.chasing.game.base;

import java.util.List;
import java.util.Map;

/**
 * Created by deacon on 2019/4/1.
 */
public interface PropGenerator {

    /**
     * 获取新的道具
     */
    List<EnvProp> getProp();
}
