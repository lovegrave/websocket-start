package com.dhls.util.websocket.entity;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author hl
 * @desc
 * @since 2021/12/18
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserChannelEntity implements Serializable {

    private String userId;

    private String topic;

    private Channel channel;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserChannelEntity that = (UserChannelEntity) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(topic, that.topic) &&
                channel==that.channel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, topic, channel);
    }
}
