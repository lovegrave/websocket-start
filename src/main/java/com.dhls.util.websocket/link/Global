package com.dhls.util.websocket.link;

import com.dhls.util.websocket.entity.UserChannelEntity;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Global {
    private Global() {
    }

//    public final static ConcurrentHashMap<String,Channel> channel = new ConcurrentHashMap<String,Channel>(32);

    /**
     * Map<topic,User>
     */
    public final static ConcurrentHashMap<String,List<UserChannelEntity>> topicUser = new ConcurrentHashMap<>(256);


    public final static ConcurrentHashMap<Channel,UserChannelEntity> channelMap = new ConcurrentHashMap<>();


    public static boolean hasChannel(Channel channel){
        return channelMap.containsKey(channel);
    }

    public static void put(UserChannelEntity user){
        if(user == null){
            return;
        }
        if(user.getTopic() == null){
            user.setTopic("default");
        }
        if(channelMap.containsKey(user.getChannel())){
            return;
        }
        topicUser.compute(user.getTopic(),(k,v)->{
            if(v == null){
                v= new ArrayList<>();
            }
            v.add(user);
            return v;
        });
        channelMap.put(user.getChannel(),user);
    }

    public static List<UserChannelEntity> getUserChannel(String topic){
        return topicUser.getOrDefault(topic, Collections.emptyList());
    }

    public static void remove(Channel channel){
        if(!channelMap.containsKey(channel)){
            return;
        }
        UserChannelEntity user = channelMap.get(channel);
        // 删除
        topicUser.compute(user.getTopic(),(k,v)->{
            v.remove(user);
            return v;
        });
        //
        channelMap.remove(channel);
    }

}
