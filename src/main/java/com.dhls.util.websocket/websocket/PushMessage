package com.dhls.util.websocket.websocket;

import com.alibaba.fastjson.JSONObject;
import com.dhls.util.websocket.entity.UserChannelEntity;
import com.dhls.util.websocket.link.Global;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PushMessage {

    /**
     * netty 推送
     * @param obj
     * @return
     */
    public void sendMessage(String topic,String userId,Object obj) {
        List<UserChannelEntity> list = Global.getUserChannel(topic);
        List<UserChannelEntity> channels = list.stream().filter(Objects::nonNull)
                .filter(a->userId!= null && a.getUserId() != null && a.getUserId().equals(userId))
                .collect(Collectors.toList());
        channels.stream().filter(Objects::nonNull)
                .filter(a->a.getChannel() != null)
                .forEach(a->{
                    TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSONObject.toJSONString(obj));
                    ChannelFuture channelFuture = a.getChannel().writeAndFlush(textWebSocketFrame);
                    channelFuture.addListener(new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            channelFuture.isSuccess();
                            log.info("用户:{},主题:{},消息:{},发送成功",userId,topic,obj);
                        }
                    });
                });

    }


    /**
     * netty 推送
     * @param obj
     * @return
     */
    public void pushAll(String topic,Object obj) {
        if(topic == null){
            topic = "default";
        }
        List<UserChannelEntity> channel = Global.getUserChannel(topic);
        if(null == channel || channel.size() ==0){
            return;
        }
        channel.stream().filter(a->a.getChannel() != null).forEach(a->{
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSONObject.toJSONString(obj));
            ChannelFuture channelFuture = a.getChannel().writeAndFlush(textWebSocketFrame);
            channelFuture.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    boolean success = channelFuture.isSuccess();
                }
            });
        });

    }

}
