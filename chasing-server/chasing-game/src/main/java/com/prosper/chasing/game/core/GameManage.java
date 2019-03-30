package com.prosper.chasing.game.core;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

import javax.annotation.PostConstruct;

import com.prosper.chasing.game.base.GameInfo;
import com.prosper.chasing.game.base.MetaGameAnno;
import com.prosper.chasing.game.base.User;
import com.prosper.chasing.game.map.MapCreator;
import com.prosper.chasing.game.map.MapSkeleton;
import com.prosper.chasing.game.message.*;
import com.prosper.chasing.game.navmesh.NavMeshGroup;
import com.prosper.chasing.game.util.ByteBuilder;
import com.prosper.chasing.game.util.Constant;
import org.apache.thrift.TException;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

import com.prosper.chasing.common.bean.client.ThriftClient;
import com.prosper.chasing.common.bean.client.ZkClient;
import com.prosper.chasing.common.interfaces.data.GameTr;
import com.prosper.chasing.common.interfaces.data.UserPropTr;
import com.prosper.chasing.common.interfaces.data.UserTr;
import com.prosper.chasing.common.util.ViewTransformer;
import com.prosper.chasing.common.util.CommonConstant.GameState;
import com.prosper.chasing.game.util.Config;
import redis.clients.jedis.Jedis;

@Component
public class GameManage {
}
